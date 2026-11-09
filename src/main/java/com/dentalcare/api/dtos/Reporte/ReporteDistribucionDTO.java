package com.dentalcare.api.dtos.Reporte;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Segmento para gráficos de dona / distribución porcentual (por doctor, especialidad o categoría).
 * DENTALCARE-74 (PBI-Gerencia-04)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteDistribucionDTO {
    private String etiqueta;
    private Integer cantidad;
    private Double porcentaje;
    private String color;
}
