package com.dentalcare.api.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AppConfig
 *
 * <b>Propósito:</b>
 * Declara y provee beans de utilidades y librerías transversales al contenedor de inversión
 * de control de Spring Framework, facilitando la conversión y mapeo entre entidades del dominio y DTOs.
 *
 * <b>Ubicación y Rol en la Arquitectura:</b>
 * - Capa: Configuración / Infraestructura de Aplicación.
 * - Rol: Proveedor de beans utilitarios (@Configuration) para desacoplar modelos de persistencia y contratos de API.
 *
 * <b>Trazabilidad (Referencias):</b>
 * - Invocado por: Contexto de Spring durante el arranque.
 * - Consumido por: Servicios y controladores que requieran transformaciones automatizadas de objetos.
 */
@Configuration
public class AppConfig {

    /**
     * Registra el bean singleton de ModelMapper para mapeo reflexivo de propiedades entre clases.
     *
     * <b>Propósito:</b>
     * Reducir el código repetitivo de asignación manual de getters y setters al transformar
     * entidades JPA a objetos de transferencia de datos (DTO).
     *
     * @return Nueva instancia configurada de {@link ModelMapper}.
     */
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
