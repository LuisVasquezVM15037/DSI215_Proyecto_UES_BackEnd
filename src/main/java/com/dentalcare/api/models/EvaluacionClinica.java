package com.dentalcare.api.models;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Propósito: Modela la entidad 'evaluacion_clinica', que representa el diagnóstico odontológico
 * y las observaciones clínicas registradas durante la atención de una cita médica específica.
 * Sirve como punto de partida clínico para la elaboración de planes de tratamiento.
 * 
 * Ubicación y Rol: Capa de Persistencia / Modelo de Dominio Clínico.
 * 
 * Trazabilidad (Referencias):
 * - Utilizado por: EvaluacionClinicaRepository, ConsultaService, ConsultaController.
 * - Relaciones:
 *   - Vinculado a Cita (@ManyToOne): Consulta o cita donde se efectuó la revisión médica.
 *   - Referenciado en PlanTratamiento: Como base clínica que justifica los procedimientos prescritos.
 */
@Data
@Entity
@Table(name = "evaluacion_clinica")
public class EvaluacionClinica {

    /** Identificador único autoincremental de la evaluación clínica */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idEvaluacionClinica;

    /** Cita médica asociada en la que se efectuó esta evaluación */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cita", nullable = false)
    private Cita cita;

    /** Diagnóstico clínico dental emitido por el odontólogo */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String diagnostico;

    /** Observaciones y notas clínicas adicionales tomadas durante el examen bucal */
    @Column(columnDefinition = "TEXT")
    private String observaciones;
}