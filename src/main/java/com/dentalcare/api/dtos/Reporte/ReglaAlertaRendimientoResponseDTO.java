package com.dentalcare.api.dtos.Reporte;

import com.dentalcare.api.models.enums.OperadorComparacion;
import com.dentalcare.api.models.enums.PeriodoEvaluacion;
import com.dentalcare.api.models.enums.TipoMetricaRendimiento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representación de salida de una regla de alerta de rendimiento.
 * DENTALCARE-95 (PBI-Gerencia-05)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReglaAlertaRendimientoResponseDTO {
    private Integer id;
    private String nombreIndicador;
    private String descripcion;
    private TipoMetricaRendimiento tipoMetrica;
    private OperadorComparacion operadorComparacion;
    private BigDecimal valorUmbral;
    private PeriodoEvaluacion periodoEvaluacion;
    private Boolean activo;
    private LocalDateTime ultimaEvaluacion;
    private BigDecimal ultimoValorDetectado;
    private Boolean estadoAlertaDisparada;
    private String mensajeUltimaAlerta;
    private Integer usuarioCreadorId;
    private String usuarioCreadorNombre;
    private LocalDateTime creadoEn;
}
