package com.dentalcare.api.models;

import com.dentalcare.api.models.enums.TipoMovimiento;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Propósito: Modela las transacciones de entrada, salida, ajuste o devolución de medicamentos
 * e insumos en el inventario de la clínica odontológica (kardex de inventario).
 * Registra el responsable del movimiento, cantidad afectada, fecha y motivo operativo.
 * 
 * Ubicación y Rol: Capa de Persistencia / Modelo de Dominio de Auditoría de Inventarios.
 * 
 * Trazabilidad (Referencias):
 * - Relaciones:
 *   - Vinculado a Usuario (@ManyToOne): Empleado o usuario que ejecutó la operación de inventario.
 *   - Vinculado a Medicamento (@ManyToOne): Fármaco cuyo stock físico fue alterado.
 */
@Data
@Entity
@Table(name = "movimiento_inventario")
public class MovimientoInventario {

    /** Identificador único autoincremental del movimiento de inventario */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idMovimiento;

    /** Usuario del sistema que registró o autorizó la transacción */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    /** Medicamento o insumo sobre el cual se realiza el movimiento */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_medicamento", nullable = false)
    private Medicamento medicamento;

    /** Tipo de transacción de inventario (INGRESO, SALIDA, AJUSTE, etc.) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimiento tipoMovimiento;

    /** Cantidad de unidades involucradas en la transacción (positiva) */
    @Column(nullable = false)
    private Integer cantidad;

    /** Fecha y hora exacta en que se registró la transacción en el sistema */
    @Column(nullable = false)
    private LocalDateTime fechaMovimiento;

    /** Justificación u orden administrativa que respalda el movimiento de existencias */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String motivo;
}