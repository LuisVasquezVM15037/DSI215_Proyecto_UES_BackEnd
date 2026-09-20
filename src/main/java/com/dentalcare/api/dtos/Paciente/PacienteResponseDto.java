package com.dentalcare.api.dtos.Paciente;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;

/**
 * Propósito: Objeto de transferencia de datos (DTO) de salida para la exposición de datos del paciente.
 * Controla la proyección de datos hacia el frontend sin exponer detalles internos o asociaciones circulares.
 * 
 * Ubicación y Rol: Capa de Presentación / DTO de Respuesta (Data Transfer Object).
 * 
 * Trazabilidad (Referencias):
 * - Utilizado por: PacienteController, PacienteService.
 * - Consumido por: Tablas de pacientes, vista de expediente y selectores en agendamiento de citas.
 */
@Data
@AllArgsConstructor
public class PacienteResponseDto {

    /** Identificador primario del paciente */
    private Integer idPaciente;

    /** Nombre de pila */
    private String nombrePaciente;

    /** Apellidos */
    private String apellidoPaciente;

    /** Número de teléfono registrado */
    private String telefonoPaciente;

    /** Fecha de nacimiento */
    private LocalDate fechaNacimientoPaciente;

    /** Documento único de identidad */
    private String numeroIdentidadPaciente;

    /** Correo electrónico */
    private String emailPaciente;

    /** Datos de contacto de emergencia */
    private String contactoEmergencia;

    /** Registro de alergias médicas reportadas */
    private String alergias;
}

