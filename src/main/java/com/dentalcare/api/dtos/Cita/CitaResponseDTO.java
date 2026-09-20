package com.dentalcare.api.dtos.Cita;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.dentalcare.api.models.enums.EstadoCita;
import lombok.Data;

/**
 * Propósito: Objeto de transferencia de datos (DTO) de salida para la visualización y consulta
 * de citas odontológicas. Aplana y desnormaliza información clave del paciente y del odontólogo
 * para evitar consultas adicionales (problema N+1) y optimizar el rendimiento de renderizado en el cliente.
 * 
 * Ubicación y Rol: Capa de Presentación / DTO de Respuesta (Data Transfer Object).
 * 
 * Trazabilidad (Referencias):
 * - Utilizado por: CitaController, CitaService.
 * - Consumido por: Vistas de agenda médica, calendarios y tablas de gestión de citas en el frontend.
 */
@Data
public class CitaResponseDTO {

    /** Identificador primario de la cita médica */
    private Integer idCitas;

    /** Fecha agendada para la cita */
    private LocalDate fechaCita;

    /** Hora de inicio pactada */
    private LocalDateTime horaInicioCita;

    /** Hora proyectada de culminación */
    private LocalDateTime horaFinCita;

    /** Estado actual de la cita (PROGRAMADA, FINALIZADA, CANCELADA, etc.) */
    private EstadoCita estadoCita;

    /** Justificación registrada en caso de que la cita haya sido cancelada */
    private String motivoCancelacion;
    
    /** Identificador del paciente atendido */
    private Integer idPaciente;

    /** Nombre y apellidos concatenados del paciente */
    private String nombreCompletoPaciente;

    /** Documento único de identidad (DUI u homólogo) del paciente */
    private String numeroIdentidadPaciente;
    
    /** Identificador del odontólogo facultativo asignado */
    private Integer idOdontologo;

    /** Nombre y apellidos concatenados del odontólogo */
    private String nombreCompletoOdontologo;

    /** Especialidad odontológica del profesional asignado */
    private String especialidadOdontologo;  
}

