package com.dentalcare.api.dtos.Inventario;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO con el consolidado analítico de alertas de stock para widgets y métricas KPI.
 * DENTALCARE-49 & DENTALCARE-73
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaStockResumenDTO {

    private Integer totalAlertas;
    private Integer criticos; // stockActual <= 0 o cobertura <= 20%
    private Integer altos;    // 20% < cobertura <= 50%
    private Integer medios;   // 50% < cobertura <= 100%
    private List<AlertaStockDTO> top3;
    private List<AlertaStockDTO> alertas;
}
