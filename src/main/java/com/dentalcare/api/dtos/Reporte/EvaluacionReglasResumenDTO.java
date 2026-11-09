package com.dentalcare.api.dtos.Reporte;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Resumen del proceso de evaluación de umbrales e indicadores de calidad.
 * DENTALCARE-95 (PBI-Gerencia-05)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluacionReglasResumenDTO {
    private Integer totalReglasEvaluadas;
    private Integer alertasDisparadas;
    private Integer alertasNormales;
    private LocalDateTime fechaEvaluacion;
    private List<ReglaAlertaRendimientoResponseDTO> detalles;
}
