package com.dentalcare.api.dtos.Odontologo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Propósito: Objeto de transferencia de datos (DTO) de respuesta para la entidad Odontologo.
 * Aplana la relación con Usuario para desacoplar el modelo relacional y simplificar el consumo en el frontend.
 * 
 * Ubicación y Rol: Capa de Presentación / DTO de Respuesta (Data Transfer Object).
 * 
 * Trazabilidad (Referencias):
 * - Utilizado por: OdontologoController, UsuarioService.
 * - Consumido por: Vistas de gestión de personal médico y asignación de doctores.
 */
@Data
@AllArgsConstructor
public class OdontologoResponseDto {

    /** Identificador primario del odontólogo */
    private Integer idOdontologo;

    /** Rama o especialidad odontológica */
    private String especialidadOdontologo;

    /** Identificador de registro en la JVPO */
    private String jvpoId;

    /** Nombre completo del usuario profesional asociado */
    private String nombreCompleto;
}