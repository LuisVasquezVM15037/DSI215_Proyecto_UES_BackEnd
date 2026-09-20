package com.dentalcare.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JwtUtil
 *
 * <b>Propósito:</b>
 * Proporciona las operaciones criptográficas centrales para la gestión de JSON Web Tokens (JWT).
 * Se encarga de la generación, firmado digital (HMAC-SHA), extracción de reclamos (claims)
 * y validación de integridad y expiración temporal de los tokens.
 *
 * <b>Ubicación y Rol en la Arquitectura:</b>
 * - Capa: Seguridad / Servicio de Utilidad Criptográfica.
 * - Rol: Componente (@Component) inyectable que encapsula el uso de la biblioteca JJWT (io.jsonwebtoken).
 *
 * <b>Trazabilidad (Referencias):</b>
 * - Invocado por: {@link com.dentalcare.api.services.AutentificacionService} (para emitir tokens al autenticarse)
 *   y {@link JwtAuthenticationFilter} (para validar tokens en cada petición HTTP).
 * - Consume / Dependencias: Propiedades de configuración `jwt.secret` y `jwt.expiration.ms`.
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration.ms}")
    private long expirationMs;

    /**
     * Construye la clave criptográfica a partir del secreto configurado en las propiedades del sistema.
     *
     * <b>Propósito:</b>
     * Convertir los bytes del secreto textual en una instancia segura de {@link SecretKey} para HMAC-SHA.
     *
     * @return {@link SecretKey} válida para el algoritmo HMAC-SHA256 o superior.
     */
    private SecretKey getSigningKey() {
        // Se transforma la cadena de texto a bytes para generar una clave simétrica compatible con JJWT
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Genera un JWT firmado con los datos de identidad y autorización del usuario.
     *
     * <b>Propósito:</b>
     * Crear el token compacto con el identificador del usuario como subject, el rol en los claims
     * personalizados y las marcas temporales de emisión y expiración.
     *
     * <b>Trazabilidad:</b>
     * - Llamado por: {@link com.dentalcare.api.services.AutentificacionService#login} tras verificar contraseñas.
     *
     * @param username Identificador único o nombre de usuario del sujeto del token.
     * @param rol Nombre del rol asignado (ej. ADMIN, ODONTOLOGO, RECEPCIONISTA).
     * @return Cadena JWT compacta codificada en Base64Url (header.payload.signature).
     */
    public String generateToken(String username, String rol) {
        return Jwts.builder()
                .subject(username)
                .claim("rol", rol)
                .issuedAt(new Date())
                // Se fija la fecha límite sumando el tiempo de expiración configurado al timestamp actual
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extrae el identificador del usuario (subject) contenido en el cuerpo del token.
     *
     * <b>Propósito:</b>
     * Obtener el username para asociarlo al contexto de seguridad o buscar al usuario en la base de datos.
     *
     * <b>Trazabilidad:</b>
     * - Llamado por: {@link JwtAuthenticationFilter#doFilterInternal}.
     *
     * @param token Cadena JWT compacta.
     * @return Nombre de usuario extraído del payload del token.
     */
    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Extrae el rol asignado al usuario desde los claims personalizados del token.
     *
     * <b>Propósito:</b>
     * Conceder autoridades y permisos dentro de Spring Security sin necesidad de consultar
     * la base de datos en cada petición entrante.
     *
     * <b>Trazabilidad:</b>
     * - Llamado por: {@link JwtAuthenticationFilter#doFilterInternal}.
     *
     * @param token Cadena JWT compacta.
     * @return Nombre del rol como {@link String}, o null si el token es inválido o no contiene el reclamo.
     */
    public String extractRole(String token) {
        try {
            return parseClaims(token).get("rol", String.class);
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Valida la firma criptográfica y la vigencia temporal del token proporcionado.
     *
     * <b>Propósito:</b>
     * Garantizar que el token no haya sido alterado por terceros y que no haya superado su ciclo de vida.
     *
     * <b>Trazabilidad:</b>
     * - Llamado por: {@link JwtAuthenticationFilter#doFilterInternal}.
     *
     * @param token Cadena JWT compacta a verificar.
     * @return true si la firma es válida y el token no ha expirado; false en caso contrario.
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // Se captura cualquier alteración de firma, expiración o formato erróneo sin propagar error al cliente
            return false;
        }
    }

    /**
     * Parsea y verifica criptográficamente el token contra la clave de firma.
     *
     * <b>Propósito:</b>
     * Validar la firma y desempaquetar la carga útil de reclamos (Claims) del JWT.
     *
     * @param token Cadena JWT compacta.
     * @return Objeto {@link Claims} con todos los atributos descifrados del payload.
     * @throws JwtException Si la firma no coincide, el token expiró o la estructura está corrupta.
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}