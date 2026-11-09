package com.dentalcare.api.dtos.Reporte;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Punto de serie temporal para gráficos estadísticos de tendencia.
 * DENTALCARE-74 (PBI-Gerencia-04)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteTendenciaDTO {
    private String periodo; // Ej: "01/11", "Semana 1", "Nov 2026"
    private String fecha;
    private Integer atenciones;
    private Integer consumos;
    private Integer canceladas;
}
