package com.dentalcare.api.models.enums;

/**
 * Propósito: Define los estados de gestión y ciclo de vida de un plan de tratamiento dental.
 * Permite controlar si una pieza dental tiene procedimientos pendientes, en curso, finalizados o cancelados.
 * 
 * Ubicación y Rol: Capa de Persistencia / Enumerador de Dominio Clínico.
 * 
 * Trazabilidad (Referencias):
 * - Utilizado por: Entidad PlanTratamiento, ConsultaService, ConsultaController,
 *   PlanTratamientoRequestDTO, PlanTratamientoResponseDTO.
 */
public enum EstadoPlan {
    /** Plan diagnosticado y registrado, aún no iniciado en consulta */
    PENDIENTE, 
    
    /** Plan con sesiones iniciadas o en desarrollo activo */
    EN_PROGRESO, 
    
    /** Plan completado a satisfacción y dado de alta */
    COMPLETADO, 
    
    /** Plan desistido o cancelado por indicación médica o decisión del paciente */
    CANCELADO, 
    
    /** Plan con fecha futura de intervención agendada */
    PROGRAMADO,
    
    /** Estado comodín para situaciones clínicas excepcionales */
    OTRO
}

