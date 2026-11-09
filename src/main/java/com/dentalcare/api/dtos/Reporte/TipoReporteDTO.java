package com.dentalcare.api.dtos.Reporte;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la presentación del catálogo de reportes institucionales.
 * DENTALCARE-70 (PBI-Gerencia-01: Seleccionar Reporte)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TipoReporteDTO {
    private String id;
    private String titulo;
    private String descripcion;
    private String icono;
    private String categoria;
    private String periodoRecomendado;
}
