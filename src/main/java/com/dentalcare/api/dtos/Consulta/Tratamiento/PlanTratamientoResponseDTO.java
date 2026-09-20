package com.dentalcare.api.dtos.Consulta.Tratamiento;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Propósito: Objeto de transferencia de datos (DTO) de salida para planes de tratamiento (hallazgos).
 * Aplana y desnormaliza los atributos del procedimiento (Tratamiento) para que el frontend pueda renderizar
 * directamente cada hallazgo en el odontograma sin requerir búsquedas adicionales en el catálogo.
 * 
 * Ubicación y Rol: Capa de Presentación / DTO de Respuesta (Data Transfer Object).
 * 
 * Trazabilidad (Referencias):
 * - Utilizado por: ConsultaController, ConsultaService.
 * - Consumido por: Componentes de odontograma y tabla de presupuestos del paciente.
 */
@Data
@AllArgsConstructor
public class PlanTratamientoResponseDTO {

    /** Identificador primario del plan de tratamiento */
    private Integer idPlanTratamiento;

    /** Número de pieza dental intervenida (norma FDI) */
    private Integer piezaDental;

    /** Estado actual del plan (PENDIENTE, EN_PROGRESO, COMPLETADO, etc.) */
    private String estadoPlan;

    /** Identificador del tratamiento base */
    private Integer idTratamiento;

    /** Nombre del procedimiento clínico */
    private String nombreTratamiento;

    /** Descripción detallada del procedimiento */
    private String descripcionTratamiento;

    /** Precio en punto flotante para renderizado en frontend */
    private Float precioFloat;
}

