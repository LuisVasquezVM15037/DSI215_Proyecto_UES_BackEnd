package com.dentalcare.api.dtos.Reporte;

import com.dentalcare.api.models.enums.OperadorComparacion;
import com.dentalcare.api.models.enums.PeriodoEvaluacion;
import com.dentalcare.api.models.enums.TipoMetricaRendimiento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Payload para creación o modificación de una regla de alerta de rendimiento gerencial.
 * DENTALCARE-95 (PBI-Gerencia-05)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReglaAlertaRendimientoRequestDTO {

    @NotBlank(message = "El nombre del indicador es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String nombreIndicador;

    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    private String descripcion;

    @NotNull(message = "El tipo de métrica es obligatorio")
    private TipoMetricaRendimiento tipoMetrica;

    @NotNull(message = "El operador de comparación es obligatorio")
    private OperadorComparacion operadorComparacion;

    @NotNull(message = "El valor umbral es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El valor umbral no puede ser negativo")
    private BigDecimal valorUmbral;

    @NotNull(message = "El período de evaluación es obligatorio")
    private PeriodoEvaluacion periodoEvaluacion;

    @Builder.Default
    private Boolean activo = true;
}
