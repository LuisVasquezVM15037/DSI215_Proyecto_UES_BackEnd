package com.dentalcare.api.models;

import com.dentalcare.api.models.enums.TipoNotificacion;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entidad de persistencia para el Centro Central de Notificaciones.
 * Soporta destinatarios por usuario específico o dirigidos a un rol en general.
 * DENTALCARE-71 (PBI-Secret-16)
 */
@Data
@Entity
@Table(name = "notificaciones")
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = true)
    private Usuario usuario;

    @Column(name = "rol_destinatario", length = 50, nullable = true)
    private String rolDestinatario;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoNotificacion tipo;

    @Column(nullable = false)
    private Boolean leida = false;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "referencia_id", nullable = true)
    private Integer referenciaId;

    @Column(name = "ruta_redireccion", length = 255, nullable = true)
    private String rutaRedireccion;
}
