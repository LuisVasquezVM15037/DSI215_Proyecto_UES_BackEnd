package com.dentalcare.api.models;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

/**
 * Propósito: Entidad que representa el catálogo de tratamientos o procedimientos clínicos
 * odontológicos ofrecidos por la clínica (ej. Profilaxis, Endodoncia, Extracción, Obturación),
 * incluyendo sus descripciones clínicas y aranceles/costos monetarios base.
 * 
 * Ubicación y Rol: Capa de Persistencia / Modelo de Dominio Clínico (Catálogo Maestro).
 * 
 * Trazabilidad (Referencias):
 * - Utilizado por: TratamientoRepository, ConsultaService, ConsultaController.
 * - Relaciones:
 *   - Referenciado en PlanTratamiento (@OneToMany tácito): Como tipo de procedimiento seleccionado.
 */
@Data
@Entity
@Table(name = "tratamiento")
public class Tratamiento {

    /** Identificador único autoincremental del tratamiento */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idTratamiento;

    /** Nombre comercial o clínico del procedimiento odontológico */
    @Column(nullable = false)
    private String nombreTratamiento;

    /** Descripción detallada del procedimiento clínico y alcances */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcionTratamiento;

    /** Arancel o costo financiero fijado para el tratamiento */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal costoTratamiento;
}