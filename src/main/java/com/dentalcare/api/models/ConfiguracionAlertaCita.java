package com.dentalcare.api.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entidad de persistencia para las preferencias de recordatorios de citas.
 * DENTALCARE-96 (PBI-Secret-15)
 */
@Data
@Entity
@Table(name = "configuracion_alerta_cita")
public class ConfiguracionAlertaCita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "alertas_activas", nullable = false)
    private Boolean alertasActivas = true;

    @Column(name = "tiempo_anticipacion_minutos", nullable = false)
    private Integer tiempoAnticipacionMinutos = 30;

    @Column(name = "sonido_activo", nullable = false)
    private Boolean sonidoActivo = true;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn = LocalDateTime.now();

    @PreUpdate
    @PrePersist
    public void preUpdate() {
        this.actualizadoEn = LocalDateTime.now();
    }
}
