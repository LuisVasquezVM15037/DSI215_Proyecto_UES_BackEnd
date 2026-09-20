package com.dentalcare.api.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JwtAuthEntryPoint
 *
 * <b>Propósito:</b>
 * Gestiona el rechazo de peticiones HTTP no autenticadas dirigidas a recursos protegidos.
 * Intercepta las excepciones de tipo {@link AuthenticationException} generadas por la cadena
 * de seguridad y emite una respuesta HTTP con código 401 (Unauthorized) en formato JSON estandarizado,
 * evitando redirecciones por defecto a páginas HTML.
 *
 * <b>Ubicación y Rol en la Arquitectura:</b>
 * - Capa: Seguridad / Manejador de Excepciones Web.
 * - Rol: Implementación personalizada de {@link AuthenticationEntryPoint} inyectada en
 *   {@link com.dentalcare.api.config.SecurityConfig}.
 *
 * <b>Trazabilidad (Referencias):</b>
 * - Invocado por: ExceptionTranslationFilter de Spring Security cuando un cliente anónimo o con token inválido intenta acceder a una ruta que requiere autenticación.
 * - Consumido por: Clientes HTTP (frontend SPA como React / Axios / Fetch) para disparar la redirección al login tras recibir el código 401.
 */
@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    /**
     * Inicia el esquema de respuesta cuando ocurre una falla de autenticación.
     *
     * <b>Propósito:</b>
     * Escribir directamente en el flujo de salida de la respuesta HTTP el código 401 y una carga útil JSON.
     *
     * <b>Trazabilidad:</b>
     * - Llamado por: Spring Security al interceptar un intento no autorizado.
     *
     * @param request Solicitud HTTP que originó la excepción de autenticación.
     * @param response Respuesta HTTP donde se configura el estado y el cuerpo JSON.
     * @param authException Excepción lanzada durante el proceso de autenticación.
     * @throws IOException Si ocurre un error al escribir en el flujo de salida de la respuesta.
     * @throws ServletException Si ocurre una falla a nivel de servlet.
     */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        // Se define el tipo de contenido explícito para asegurar que el cliente web procese la respuesta como JSON
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("{\"message\": \"No autorizado. Se requiere un token válido para acceder a este recurso.\"}");
    }
}
