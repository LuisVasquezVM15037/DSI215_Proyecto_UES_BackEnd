package com.dentalcare.api.dtos.Consulta.Evaluacion;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Propósito: Objeto de transferencia de datos (DTO) de salida para la evaluación clínica.
 * Retorna el identificador autogenerado y el diagnóstico para que el cliente pueda ligar
 * subsecuentes planes de tratamiento o recetas a dicha evaluación.
 * 
 * Ubicación y Rol: Capa de Presentación / DTO de Respuesta de Consulta Clínica.
 * 
 * Trazabilidad (Referencias):
 * - Utilizado por: ConsultaController, ConsultaService.
 * - Consumido por: Interfaz del odontograma e historial clínico.
 */
@Data
@AllArgsConstructor
public class EvaluacionClinicaResponseDTO {

    /** Identificador primario generado de la evaluación clínica */
    private Integer idEvaluacionClinica;

    /** Identificador de la cita vinculada */
    private Integer idCita;

    /** Diagnóstico clínico asentado */
    private String diagnostico;

    /** Observaciones médicas registradas */
    private String observaciones;
}

