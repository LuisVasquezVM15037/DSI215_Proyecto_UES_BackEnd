package com.dentalcare.api.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Propósito: Modela la cabecera de una receta o prescripción médica extendida durante una cita.
 * Agrupa los distintos medicamentos recetados a través de la entidad DetallePrescripcion.
 * 
 * Ubicación y Rol: Capa de Persistencia / Modelo de Dominio Farmacológico y Clínico.
 * 
 * Trazabilidad (Referencias):
 * - Utilizado por: PrescripcionRepository, ConsultaService, ConsultaController.
 * - Relaciones:
 *   - Vinculado a Cita (@ManyToOne): Consulta odontológica en la que se extendió la receta.
 *   - Referenciado en DetallePrescripcion: Contiene cada fármaco individualmente dosificado.
 */
@Data
@Entity
@Table(name = "prescripcion")
public class Prescripcion {

    /** Identificador único autoincremental de la prescripción */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPrescripcion;

    /** Cita odontológica donde se generó la prescripción */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cita", nullable = false)
    private Cita cita;

    /** Fecha y hora exacta de emisión de la prescripción médica */
    @Column(nullable = false)
    private LocalDateTime fechaPrescripcion;
}