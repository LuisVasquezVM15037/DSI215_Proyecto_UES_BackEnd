package com.dentalcare.api.dtos.Consulta.Tratamiento;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Propósito: Objeto de transferencia de datos (DTO) de entrada para registrar un hallazgo clínico
 * o plan de tratamiento sobre una pieza dental en el odontograma.
 * Asocia la evaluación clínica con el procedimiento a realizar y su pieza dental correspondiente.
 * 
 * Ubicación y Rol: Capa de Presentación / DTO de Entrada (Data Transfer Object).
 * 
 * Trazabilidad (Referencias):
 * - Utilizado por: ConsultaController.crearPlanTratamiento(), ConsultaService.guardarPlanTratamiento().
 * - Consumido por: Interfaz interactiva del odontograma digital en el frontend.
 */
@Data
public class PlanTratamientoRequestDTO {

    /** Identificador de la evaluación clínica que sustenta el tratamiento */
    @NotNull(message = "El id de la evaluacion clinica es obligatorio.")
    private Integer idEvaluacionClinica;

    /** Identificador del tratamiento seleccionado del catálogo */
    @NotNull(message = "El id del tratamiento es obligatorio.")
    private Integer idTratamiento;

    /** Número de pieza dental intervenida (según norma FDI, ej. 11, 21, 36) */
    @NotNull(message = "La pieza dental es obligatoria.")
    private Integer piezaDental;

    /** Estado inicial asignado al plan (por defecto PENDIENTE si es omitido) */
    private String estadoPlan;

    /** Precio pactado o cotizado en punto flotante para visualización en frontend */
    private Float precioFloat;
}

