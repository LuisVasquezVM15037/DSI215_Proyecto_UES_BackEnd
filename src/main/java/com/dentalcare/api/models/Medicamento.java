package com.dentalcare.api.models;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

/**
 * Propósito: Modela el catálogo de medicamentos e insumos farmacológicos disponibles en la clínica.
 * Mantiene la información terapéutica (principio activo, concentración), datos comerciales
 * (proveedor, costo) y control de existencias en bodega o farmacia (cantidad en inventario).
 * 
 * Ubicación y Rol: Capa de Persistencia / Modelo de Dominio Farmacológico y de Inventarios.
 * 
 * Trazabilidad (Referencias):
 * - Utilizado por: MedicamentoRepository, ConsultaService, ConsultaController.
 * - Relaciones:
 *   - Vinculado a Proveedor (@ManyToOne): Entidad comercial suministradora del fármaco.
 *   - Referenciado en DetallePrescripcion: Medicamentos recetados a pacientes.
 *   - Referenciado en MovimientoInventario: Auditoría de entradas y salidas de stock.
 */
@Data
@Entity
@Table(name = "medicamento")
public class Medicamento {

    /** Identificador único autoincremental del medicamento */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idMedicamento;

    /** Proveedor que distribuye y suministra el producto a la clínica */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proveedor", nullable = false)
    private Proveedor proveedor;

    /** Nombre comercial de la presentación farmacéutica */
    @Column(nullable = false)
    private String nombreMedicamento;

    /** Principio químico activo del medicamento (ej. Amoxicilina, Ibuprofeno) */
    @Column(nullable = false)
    private String componenteActivo;

    /** Concentración farmacológica declarada (ej. 500mg, 100mg/5ml) */
    @Column(nullable = false)
    private String concentracion;

    /** Precio o costo unitario de adquisición/venta del medicamento */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal costoMedicamento;

    /** Nivel actual de existencias en el inventario de la clínica */
    @Column(nullable = false)
    private Integer cantidadInventario;
}