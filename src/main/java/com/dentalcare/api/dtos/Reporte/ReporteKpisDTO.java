package com.dentalcare.api.dtos.Reporte;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO con indicadores clave de rendimiento (KPIs) para la cabecera ejecutiva de reportes.
 * DENTALCARE-74 (PBI-Gerencia-04: Visualizar Resultados de Reportes)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteKpisDTO {
    private Integer totalAtenciones;
    private Integer totalConsumoItems;
    private Integer totalCitasProgramadas;
    private Integer totalCanceladas;
    private Integer totalNoAsistio;
    private Double tasaEfectividad; // % de citas completadas sobre programadas
    private String doctorMayorAtencion;
    private String tratamientoMasDemandado;
    private String insumoMasConsumido;
    private String categoriaMayorConsumo;
    private Double promedioAtencionesDiarias;
}
