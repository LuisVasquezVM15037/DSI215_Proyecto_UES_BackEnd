package com.dentalcare.api.dtos.Odontologo;

import lombok.Data;

/**
 * Propósito: Objeto de transferencia de datos (DTO) compacto para selectores y listas desplegables
 * de odontólogos, proporcionando el identificador profesional, nombre completo concatenado y especialidad.
 * 
 * Ubicación y Rol: Capa de Presentación / DTO de Respuesta Ligero.
 * 
 * Trazabilidad (Referencias):
 * - Utilizado por: OdontologoController, OdontologoService.
 * - Consumido por: Selectores de asignación de doctor en formularios de citas y consultas.
 */
@Data
public class OdontologoDTO {

    /** Identificador primario del odontólogo */
    private Integer idOdontologo;

    /** Nombre y apellidos concatenados del usuario profesional */
    private String nombreCompleto;

    /** Especialidad clínica del odontólogo */
    private String especialidadOdontologo;
}