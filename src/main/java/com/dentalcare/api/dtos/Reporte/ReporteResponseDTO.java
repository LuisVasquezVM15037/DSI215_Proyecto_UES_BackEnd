package com.dentalcare.api.dtos.Reporte;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * DTO contenedor completo del reporte analítico con KPIs, series para gráficos y tabla de datos.
 * DENTALCARE-74 (PBI-Gerencia-04)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteResponseDTO {
    private String tipoReporte;
    private String titulo;
    private String subtitulo;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Map<String, Object> filtrosAplicados;
    private ReporteKpisDTO kpis;
    private List<ReporteTendenciaDTO> graficoTendencia;
    private List<ReporteDistribucionDTO> graficoDistribucion;
    private List<ReporteDetalleItemDTO> tablaDetalle;
    private Integer totalRegistros;
}
