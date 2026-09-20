package com.dentalcare.api.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * CorsConfig
 *
 * <b>Propósito:</b>
 * Establece la política de Intercambio de Recursos de Origen Cruzado (CORS) para la API REST.
 * Habilita a las aplicaciones frontend alojadas en dominios o puertos diferentes
 * (ej. cliente Vite / React en localhost:5173) realizar peticiones HTTP hacia el backend sin ser
 * bloqueadas por las políticas de seguridad del mismo origen (Same-Origin Policy) del navegador.
 *
 * <b>Ubicación y Rol en la Arquitectura:</b>
 * - Capa: Configuración / Infraestructura Web.
 * - Rol: Declaración del bean {@link CorsConfigurationSource} consumido por {@link SecurityConfig}.
 *
 * <b>Trazabilidad (Referencias):</b>
 * - Invocado por: {@link SecurityConfig#filterChain} durante la inicialización de la cadena de filtros HTTP.
 * - Consumido por: Navegador web del cliente durante la negociación de peticiones preflight OPTIONS.
 */
@Configuration
public class CorsConfig {

    /**
     * Define y registra las reglas de orígenes, métodos y cabeceras permitidos para CORS.
     *
     * <b>Propósito:</b>
     * Configurar una fuente de configuración basada en patrones de URL que admita las operaciones
     * REST necesarias para el cliente web.
     *
     * @return Instancia de {@link CorsConfigurationSource} con la política aplicada a todas las rutas ("/**").
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Se configuran los orígenes admitidos para el intercambio entre frontend y backend
        config.setAllowedOrigins(List.of("*"));

        // Se declaran explícitamente los verbos HTTP permitidos para operaciones CRUD y preflight
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // Se admiten todas las cabeceras estándar y personalizadas, incluyendo 'Authorization' y 'Content-Type'
        config.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Se mapea la política a todos los endpoints del backend
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}