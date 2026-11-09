package com.dentalcare.api.dtos.Inventario;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para la representación de alertas de stock de materiales y medicamentos.
 * DENTALCARE-49 (PBI-AsisDent-05) & DENTALCARE-73 (PBI-Gerencia-03)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaStockDTO {

    private Integer id;
    private String tipo; // "MATERIAL" o "MEDICAMENTO"
    private String codigo;
    private String nombre;
    private String unidadMedida; // Unidad de medida o presentación
    private Integer stockActual;
    private Integer stockMinimo;
    private Integer deficit; // stockMinimo - stockActual
    private Double porcentajeCobertura; // (stockActual / stockMinimo) * 100
    private String nivelUrgencia; // "CRITICO", "ALTA", "MEDIA"
    private Integer diasEstimadosAgotamiento; // Días proyectados antes del quiebre
    private String lote;
    private LocalDate fechaVencimiento;
}
