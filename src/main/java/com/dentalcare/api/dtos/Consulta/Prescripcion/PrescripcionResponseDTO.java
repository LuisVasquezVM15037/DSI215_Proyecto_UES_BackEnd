package com.dentalcare.api.dtos.Consulta.Prescripcion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Propósito: Objeto de transferencia de datos (DTO) de salida para la receta médica emitida.
 * Retorna los datos de cabecera de la prescripción junto con la lista de medicamentos y sus
 * detalles desnormalizados para permitir la impresión o visualización directa en el frontend.
 * 
 * Ubicación y Rol: Capa de Presentación / DTO de Respuesta Compuesto.
 * 
 * Trazabilidad (Referencias):
 * - Utilizado por: ConsultaController, ConsultaService.
 * - Consumido por: Vista de receta médica imprimible y expediente del paciente.
 */
@Data
@AllArgsConstructor
public class PrescripcionResponseDTO {

    /** Identificador primario de la prescripción */
    private Integer idPrescripcion;

    /** Identificador de la cita médica emisora */
    private Integer idCita;

    /** Marca temporal precisa de emisión de la receta */
    private LocalDateTime fechaPrescripcion;

    /** Lista de renglones o medicamentos detallados */
    private List<DetallePrescripcionResponseDTO> detalles;

    /**
     * Propósito: DTO estático interno que proyecta la información farmacológica y posológica
     * de cada fármaco recetado.
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DetallePrescripcionResponseDTO {

        /** Identificador del renglón de detalle */
        private Integer idDetalle;

        /** Identificador del medicamento en catálogo */
        private Integer idMedicamento;

        /** Nombre comercial del fármaco */
        private String nombreMedicamento;

        /** Principio activo químico */
        private String componenteActivo;

        /** Concentración declarada */
        private String concentracion;

        /** Cantidad o dosis prescrita */
        private String dosis;

        /** Pauta o frecuencia temporal de administración */
        private String frecuencia;

        /** Días de tratamiento indicados */
        private Integer duracion;

        /** Instrucciones médicas especiales */
        private String indicaciones;
        
        /** Identificador del plan de tratamiento vinculado (si aplica) */
        private Integer idPlanTratamiento;
    }
}

