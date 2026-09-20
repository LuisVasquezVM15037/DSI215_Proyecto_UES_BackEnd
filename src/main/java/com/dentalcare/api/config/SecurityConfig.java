package com.dentalcare.api.config;

import com.dentalcare.api.security.JwtAuthEntryPoint;
import com.dentalcare.api.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * SecurityConfig
 *
 * <b>Propósito:</b>
 * Configura de manera centralizada la seguridad de la aplicación mediante Spring Security.
 * Define la política de gestión de sesiones, la codificación criptográfica de contraseñas,
 * las reglas de autorización por ruta y roles, y el registro del filtro de autenticación JWT.
 *
 * <b>Ubicación y Rol en la Arquitectura:</b>
 * - Capa: Configuración / Infraestructura de Seguridad.
 * - Rol: Clase de configuración (@Configuration) que declara la cadena de filtros de seguridad
 *   (SecurityFilterChain) y los beans de cifrado utilizados transversalmente por el contenedor de Spring.
 *
 * <b>Trazabilidad (Referencias):</b>
 * - Invocado por: Contenedor de Spring Boot durante el arranque inicial del contexto de aplicación.
 * - Consume / Dependencias: {@link JwtAuthenticationFilter}, {@link JwtAuthEntryPoint}, {@link CorsConfigurationSource}.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final JwtAuthEntryPoint jwtAuthEntryPoint;
    private final CorsConfigurationSource corsConfigurationSource;

    /**
     * Constructor para inyección de dependencias de los componentes de seguridad.
     *
     * @param jwtAuthFilter Filtro personalizado para validación e intercepción de tokens JWT.
     * @param jwtAuthEntryPoint Punto de entrada para manejo de respuestas de error 401 en formato JSON.
     * @param corsConfigurationSource Fuente de configuración para intercambio de recursos de origen cruzado (CORS).
     */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter,
                          JwtAuthEntryPoint jwtAuthEntryPoint,
                          CorsConfigurationSource corsConfigurationSource) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.jwtAuthEntryPoint = jwtAuthEntryPoint;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    /**
     * Registra el componente encargado del hashing y verificación de contraseñas.
     *
     * <b>Propósito:</b>
     * Proporcionar un PasswordEncoder basado en BCrypt a nivel de bean global para su consumo en
     * los servicios de autenticación y gestión de usuarios.
     *
     * @return Instancia de {@link BCryptPasswordEncoder} con costo computacional predeterminado de 10.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Se utiliza factor de costo 10 para equilibrar el retardo defensivo contra ataques de fuerza bruta y el uso de CPU
        return new BCryptPasswordEncoder();
    }

    /**
     * Construye y personaliza la cadena de filtros de seguridad HTTP (SecurityFilterChain).
     *
     * <b>Propósito:</b>
     * Aplicar las políticas de autorización, desactivar CSRF para esquemas stateless, configurar
     * el manejo de sesiones y anteponer la inspección de credenciales JWT a la cadena por defecto.
     *
     * <b>Trazabilidad:</b>
     * - Llamado por: Servlet FilterChain de Spring Security ante cada petición HTTP entrante.
     *
     * @param http Constructor de seguridad HTTP de Spring Security.
     * @return {@link SecurityFilterChain} configurada para la aplicación.
     * @throws Exception Si ocurre un error al ensamblar los filtros de seguridad.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Se enlaza CorsConfigurationSource definido en CorsConfig para admitir peticiones desde el cliente web
            .cors(cors -> cors.configurationSource(corsConfigurationSource))

            // Se deshabilita CSRF debido a que la API es de tipo REST y no utiliza cookies ni sesiones persistentes de navegador
            .csrf(csrf -> csrf.disable())

            // Se delega el manejo de excepciones de autenticación al punto de entrada JSON
            .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthEntryPoint))

            // La API es sin estado (stateless): el estado de autenticación viaja encapsulado en el token JWT
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .authorizeHttpRequests(auth -> auth
                // Se autorizan las peticiones preflight OPTIONS para no romper el protocolo CORS del navegador
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // El endpoint de autenticación debe ser público para permitir el inicio de sesión
                .requestMatchers("/api/auth/**").permitAll()

                // Endpoint de error por defecto de Spring Boot
                .requestMatchers("/error").permitAll()

                // Restricciones de acceso basadas en roles administrativos
                .requestMatchers("/api/usuarios/**").hasAnyRole("ADMIN", "GERENTE")
                .requestMatchers("/api/registros-acceso/**").hasRole("ADMIN")

                // Todo recurso no explícitamente público requiere un token JWT válido
                .anyRequest().authenticated()
            )

            // Se intercala el filtro JWT antes del filtro de usuario/contraseña estándar de Spring
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}