package com.dentalcare.api.dtos.Consulta.Prescripcion;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

/**
 * Propósito: Objeto de transferencia de datos (DTO) de entrada compuesto para registrar una receta
 * o prescripción médica completa, incluyendo su cabecera vinculada a la cita y la colección
 * de medicamentos dosificados.
 * 
 * Ubicación y Rol: Capa de Presentación / DTO de Entrada Compuesto.
 * 
 * Trazabilidad (Referencias):
 * - Utilizado por: ConsultaController.crearPrescripcion(), ConsultaService.guardarPrescripcion().
 * - Consumido por: Formulario de emisión de recetas dentro de la consulta clínica.
 */
@Data
public class PrescripcionRequestDTO {

    /** Identificador de la cita médica donde se emite la prescripción */
    @NotNull(message = "El id de la cita es obligatorio.")
    private Integer idCita;

    /** Lista con los renglones o medicamentos individuales que componen la receta */
    @NotNull(message = "Debe incluir al menos un medicamento.")
    private List<DetallePrescripcionDTO> detalles;

    /**
     * Propósito: DTO estático interno que representa cada fármaco prescrito dentro de la receta,
     * detallando su posología e indicaciones terapéuticas.
     */
    @Data
    public static class DetallePrescripcionDTO {

        /** Identificador del medicamento en el inventario clínico */
        @NotNull(message = "El id del medicamento es obligatorio.")
        private Integer idMedicamento;

        /** Dosis recomendada (ej. 500mg cada toma) */
        @NotNull(message = "La dosis es obligatoria.")
        private String dosis;

        /** Frecuencia de administración (ej. Cada 8 horas) */
        @NotNull(message = "La frecuencia es obligatoria.")
        private String frecuencia;

        /** Duración en días del tratamiento farmacológico */
        @NotNull(message = "La duracion es obligatoria.")
        private Integer duracion;

        /** Instrucciones clínicas especiales para el paciente */
        @NotNull(message = "Las indicaciones son obligatorias.")
        private String indicaciones;

        /** Identificador opcional del plan de tratamiento al cual se vincula este fármaco */
        private Integer idPlanTratamiento;
    }
}

