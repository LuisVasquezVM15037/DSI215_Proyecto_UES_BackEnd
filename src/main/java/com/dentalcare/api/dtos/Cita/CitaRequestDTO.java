package com.dentalcare.api.dtos.Cita;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.dentalcare.api.models.enums.EstadoCita;
import lombok.Data;

/**
 * Propósito: Objeto de transferencia de datos (DTO) utilizado para la recepción de información
 * requerida en la creación o reprogramación de una cita odontológica.
 * 
 * Ubicación y Rol: Capa de Presentación / DTO de Entrada (Data Transfer Object).
 * 
 * Trazabilidad (Referencias):
 * - Utilizado por: CitaController.crearCita(), CitaService.crearCita().
 * - Consumido por: Formularios y modales de agendamiento en el frontend.
 */
@Data
public class CitaRequestDTO {

    /** Identificador del odontólogo facultativo asignado */
    private Integer idOdontologo;

    /** Identificador del paciente que recibirá la atención odontológica */
    private Integer idPaciente;

    /** Fecha en que se llevará a cabo la cita médica */
    private LocalDate fechaCita;

    /** Marca de tiempo exacta con la hora pactada para el inicio de la cita */
    private LocalDateTime horaInicioCita;

    /** Marca de tiempo estimada para la finalización de la cita médica */
    private LocalDateTime horaFinCita;

    /** Estado inicial o actualizado asignado a la cita (PROGRAMADA, PENDIENTE, etc.) */
    private EstadoCita estadoCita;
}

