package com.dentalcare.api.models;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Propósito: Modela las casas farmacéuticas, distribuidores y proveedores comerciales
 * que abastecen insumos médicos y fármacos a la clínica odontológica.
 * Almacena información tributaria (NIT, NRC), comercial y datos de contacto directo.
 * 
 * Ubicación y Rol: Capa de Persistencia / Modelo de Dominio de Compras y Proveeduría.
 * 
 * Trazabilidad (Referencias):
 * - Utilizado por: Medicamento (relación @ManyToOne hacia Proveedor).
 * - Relaciones:
 *   - Posee Medicamentos asociados que la clínica le adquiere regularmente.
 */
@Data
@Entity
@Table(name = "proveedor")
public class Proveedor {

    /** Identificador único autoincremental del proveedor */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idProveedor;

    /** Razón social o denominación legal registrada de la empresa proveedora */
    @Column(nullable = false)
    private String razonSocial;

    /** 
     * Número de Identificación Tributaria (NIT) del proveedor.
     * Definido como String para admitir guiones y ceros a la izquierda en formatos tributarios.
     */
    @Column(nullable = false)
    private String nit;

    /** Número de Registro de Contribuyente (NRC) para efectos de IVA */
    @Column(nullable = false)
    private String nrc;

    /** Teléfono corporativo o de atención a clientes del proveedor */
    @Column(nullable = false)
    private String telefono;

    /** Correo electrónico comercial para cotizaciones y pedidos */
    @Column(nullable = false)
    private String email;

    /** Nombre y apellidos del ejecutivo de cuenta o contacto principal */
    @Column(nullable = false)
    private String nombreContacto;

    /** Estado lógico de operatividad comercial del proveedor (activo / inactivo) */
    @Column(nullable = false)
    private Boolean esActivo;
}