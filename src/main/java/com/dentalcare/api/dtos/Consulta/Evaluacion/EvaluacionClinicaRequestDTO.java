package com.dentalcare.api.dtos.Consulta.Evaluacion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Propósito: Objeto de transferencia de datos (DTO) de entrada para registrar o actualizar
 * el diagnóstico y observaciones clínicas de una consulta odontológica.
 * 
 * Ubicación y Rol: Capa de Presentación / DTO de Entrada de Consulta Clínica.
 * 
 * Trazabilidad (Referencias):
 * - Utilizado por: ConsultaController.crearEvaluacion(), ConsultaService.guardarEvaluacion().
 * - Consumido por: Módulo clínico del odontograma en el frontend durante la atención.
 */
@Data
public class EvaluacionClinicaRequestDTO {

    /** Identificador de la cita médica en la cual se efectúa la evaluación */
    @NotNull(message = "El id de la cita es obligatorio.")
    private Integer idCita;

    /** Dictamen o diagnóstico clínico emitido por el odontólogo */
    @NotBlank(message = "El diagnostico es obligatorio.")
    private String diagnostico;

    /** Observaciones clínicas y notas médicas adicionales (opcional) */
    private String observaciones;
}

