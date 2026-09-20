package com.dentalcare.api.models.enums;

/**
 * Propósito: Define el ciclo de vida y los estados válidos por los que transita una cita odontológica.
 * Permite gestionar la agenda clínica, el flujo de atención en consultorio y auditoría de asistencias.
 * 
 * Ubicación y Rol: Capa de Persistencia / Enumerador de Dominio Clínico.
 * 
 * Trazabilidad (Referencias):
 * - Utilizado por: Entidad Cita, CitaService, CitaController, CitaRequestDTO, CitaResponseDTO.
 */
public enum EstadoCita {
    /** Cita agendada para una fecha y hora futura confirmada */
    PROGRAMADA, 
    
    /** Cita en la cual el paciente fue atendido y la consulta culminó */
    FINALIZADA, 
    
    /** Estado histórico conservado para retrocompatibilidad con registros existentes en base de datos */
    COMPLETADA, 
    
    /** Cita anulada por el paciente o por la clínica, requiriendo motivo de cancelación */
    CANCELADA, 
    
    /** Cita solicitada en espera de confirmación de agenda */
    PENDIENTE, 
    
    /** Cita en desarrollo activo dentro del consultorio */
    EN_PROGRESO,
    
    /** Cita cuya fecha/hora original fue modificada */
    REPROGRAMADA,
    
    /** Cita a la que el paciente no se presentó sin previo aviso */
    NO_ASISTIO,
    
    /** Estado comodín para casos clínicos especiales no categorizados */
    OTRO
}

