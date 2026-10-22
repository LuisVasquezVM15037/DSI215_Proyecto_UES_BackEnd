package com.dentalcare.api.models;

import com.dentalcare.api.models.enums.EstadoConsumo;
import com.dentalcare.api.models.enums.TipoItem;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "consumos")
public class Consumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_item", nullable = false, length = 20)
    private TipoItem tipoItem;

    @Column(name = "item_id", nullable = false)
    private Integer itemId;

    @Column(name = "item_codigo", nullable = false, length = 20)
    private String itemCodigo;

    @Column(name = "item_nombre", nullable = false, length = 100)
    private String itemNombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cita_id", referencedColumnName = "idCitas", nullable = true)
    private Cita cita;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "mes_consumo", nullable = false, length = 7)
    private String mesConsumo; // Formato 'YYYY-MM'

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoConsumo estado = EstadoConsumo.PENDIENTE;

    @Column(name = "fecha_consumo", nullable = false)
    private LocalDateTime fechaConsumo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_registro_id", referencedColumnName = "idUsuario", nullable = false)
    private Usuario usuarioRegistro;

    @Column(length = 255)
    private String notas;

    @PrePersist
    protected void onCreate() {
        if (this.fechaConsumo == null) {
            this.fechaConsumo = LocalDateTime.now();
        }
        if (this.estado == null) {
            this.estado = EstadoConsumo.PENDIENTE;
        }
    }
}
