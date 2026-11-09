package com.dentalcare.api.dtos.Reporte;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Fila tabular individual para el detalle del reporte.
 * DENTALCARE-74 (PBI-Gerencia-04)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteDetalleItemDTO {
    private Integer id;
    private String fecha;
    private String concepto; // Nombre del tratamiento o insumo
    private String subconcepto; // Código, dosis, presentación o especialidad
    private String categoria; // "MATERIAL", "MEDICAMENTO" o "ATENCION_CLINICA"
    private Integer cantidad; // Cantidad consumida o 1 si es atención
    private String persona1; // Paciente
    private String persona2; // Odontólogo o Usuario de registro
    private String estado; // Estado de la cita o del consumo
    private String observaciones;
}
