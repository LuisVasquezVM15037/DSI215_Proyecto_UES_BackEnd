package com.dentalcare.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * JwtAuthenticationFilter
 *
 * <b>Propósito:</b>
 * Intercepta de manera síncrona cada petición HTTP entrante para extraer, decodificar
 * y verificar la validez del token JWT transmitido en la cabecera 'Authorization'.
 * Si el token es legítimo, establece la identidad y los roles del usuario en el contexto
 * de seguridad de Spring (SecurityContextHolder).
 *
 * <b>Ubicación y Rol en la Arquitectura:</b>
 * - Capa: Seguridad / Filtro de Infraestructura Web.
 * - Rol: Filtro de procesamiento único por petición (OncePerRequestFilter) registrado en la
 *   cadena de filtros antes de UsernamePasswordAuthenticationFilter.
 *
 * <b>Trazabilidad (Referencias):</b>
 * - Invocado por: Cadena de filtros de Spring Security ante peticiones hacia cualquier endpoint.
 * - Consume / Dependencias: {@link JwtUtil} para parsing y verificación criptográfica del token.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    /**
     * Constructor para inyección de dependencias.
     *
     * @param jwtUtil Componente utilitario para validación y extracción de información de tokens JWT.
     */
    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * Ejecuta la lógica interna de filtrado para cada solicitud HTTP.
     *
     * <b>Propósito:</b>
     * Evaluar la existencia del prefijo 'Bearer ' en la cabecera Authorization, comprobar
     * la firma del token y configurar el objeto Authentication con las autoridades correspondientes.
     *
     * <b>Trazabilidad:</b>
     * - Llamado por: Marco de Servlets durante el ciclo de vida del despacho de la petición.
     *
     * @param request Petición HTTP recibida del cliente.
     * @param response Respuesta HTTP enviada al cliente.
     * @param filterChain Cadena de filtros subsecuentes a ejecutar.
     * @throws ServletException Si ocurre una falla en el procesamiento de servlets.
     * @throws IOException Si ocurre un error de entrada/salida durante el flujo de red.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // Si la cabecera no existe o no sigue el estándar Bearer, se transfiere el control al siguiente filtro
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Se extrae la cadena que representa el token omitiendo el prefijo 'Bearer ' (7 caracteres)
        final String token = authHeader.substring(7);

        try {
            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.extractUsername(token);

                // Se valida que el usuario no cuente ya con autenticación previa en el hilo de ejecución actual
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    String rol = jwtUtil.extractRole(token);

                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    if (rol != null && !rol.trim().isEmpty()) {
                        String cleanRol = rol.trim().toUpperCase();
                        // Se registran ambas variantes para garantizar compatibilidad con hasRole() y hasAuthority()
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + cleanRol));
                        authorities.add(new SimpleGrantedAuthority(cleanRol));
                    }

                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);

                    // Se asocian metadatos de la conexión de red (IP, sesión) al token de autenticación
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
        } catch (Exception ex) {
            // En caso de firma corrupta o expiración, se garantiza que el contexto de seguridad quede purgado
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
