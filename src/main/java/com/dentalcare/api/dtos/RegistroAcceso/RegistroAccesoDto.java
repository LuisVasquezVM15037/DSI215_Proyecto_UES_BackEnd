package com.dentalcare.api.dtos.RegistroAcceso;

import java.time.LocalDateTime;

/**
 * Propósito: Objeto de transferencia de datos (DTO) de respuesta para la consulta y auditoría
 * de eventos de inicio de sesión. Transporta los datos de accesos exitosos o fallidos junto
 * con el identificador y correo electrónico del usuario involucrado.
 * 
 * Ubicación y Rol: Capa de Presentación / DTO de Respuesta de Auditoría.
 * 
 * Trazabilidad (Referencias):
 * - Utilizado por: RegistroAccesoController.listarRegistrosAcceso(), RegistroAccesoRepository.
 * - Consumido por: Módulo de auditoría y monitoreo de seguridad en el panel administrativo.
 */
public class RegistroAccesoDto {

    /** Identificador único del registro de auditoría */
    private Integer id_registro_acceso;

    /** Indica si el acceso fue autenticado con éxito (true) o si fue rechazado (false) */
    private Boolean es_exitoso;

    /** Marca temporal exacta (fecha y hora) del intento de autenticación */
    private LocalDateTime fecha_acceso;

    /** Identificador del usuario que intentó el acceso */
    private Integer id_usuario;

    /** Correo electrónico del usuario al momento de la autenticación */
    private String email_usuario;

    /**
     * Constructor completo para la instanciación de registros de acceso proyectados desde JPQL o Repository.
     * 
     * @param id_registro_acceso Identificador del registro
     * @param es_exitoso Resultado del intento de login
     * @param fecha_acceso Momento cronológico del evento
     * @param id_usuario ID del usuario
     * @param email_usuario Email del usuario
     */
    public RegistroAccesoDto(Integer id_registro_acceso, Boolean es_exitoso, LocalDateTime fecha_acceso, Integer id_usuario, String email_usuario) {
        this.id_registro_acceso = id_registro_acceso;
        this.es_exitoso = es_exitoso;
        this.fecha_acceso = fecha_acceso;
        this.id_usuario = id_usuario;
        this.email_usuario = email_usuario;
    }

    public Integer getId_registro_acceso() {
        return id_registro_acceso;
    }

    public Boolean getEs_exitoso() {
        return es_exitoso;
    }

    public LocalDateTime getFecha_acceso() {
        return fecha_acceso;
    }

    public Integer getId_usuario() {
        return id_usuario;
    }

    public String getEmail_usuario() {
        return email_usuario;
    }
}