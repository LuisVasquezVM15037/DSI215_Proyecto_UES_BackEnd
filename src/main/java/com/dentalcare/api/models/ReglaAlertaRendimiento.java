package com.dentalcare.api.models;

import com.dentalcare.api.models.enums.OperadorComparacion;
import com.dentalcare.api.models.enums.PeriodoEvaluacion;
import com.dentalcare.api.models.enums.TipoMetricaRendimiento;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad para la configuración de umbrales y reglas automáticas de control de calidad y rendimiento operativo.
 * DENTALCARE-95 (PBI-Gerencia-05: Configurar Alertas de Rendimiento)
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reglas_alerta_rendimiento")
public class ReglaAlertaRendimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre_indicador", nullable = false, length = 100)
    private String nombreIndicador;

    @Column(length = 255)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_metrica", nullable = false, length = 40)
    private TipoMetricaRendimiento tipoMetrica;

    @Enumerated(EnumType.STRING)
    @Column(name = "operador_comparacion", nullable = false, length = 30)
    private OperadorComparacion operadorComparacion;

    @Column(name = "valor_umbral", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorUmbral;

    @Enumerated(EnumType.STRING)
    @Column(name = "periodo_evaluacion", nullable = false, length = 20)
    @Builder.Default
    private PeriodoEvaluacion periodoEvaluacion = PeriodoEvaluacion.SEMANAL;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "ultima_evaluacion")
    private LocalDateTime ultimaEvaluacion;

    @Column(name = "ultimo_valor_detectado", precision = 10, scale = 2)
    private BigDecimal ultimoValorDetectado;

    @Column(name = "estado_alerta_disparada")
    @Builder.Default
    private Boolean estadoAlertaDisparada = false;

    @Column(name = "mensaje_ultima_alerta", length = 255)
    private String mensajeUltimaAlerta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_creador_id", nullable = false)
    private Usuario usuarioCreador;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @PrePersist
    protected void onCreate() {
        if (this.creadoEn == null) {
            this.creadoEn = LocalDateTime.now();
        }
        if (this.activo == null) {
            this.activo = true;
        }
        if (this.estadoAlertaDisparada == null) {
            this.estadoAlertaDisparada = false;
        }
    }
}
