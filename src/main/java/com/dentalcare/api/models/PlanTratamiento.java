package com.dentalcare.api.models;

import com.dentalcare.api.models.enums.EstadoPlan;
import jakarta.persistence.*;
import lombok.Data;

/**
 * Propósito: Modela un plan de tratamiento individualizado para una pieza dental específica.
 * Asocia un procedimiento odontológico estandarizado (Tratamiento) con el diagnóstico
 * previo (EvaluacionClinica), gestionando su ciclo de vida y avance mediante Estados de Plan.
 * 
 * Ubicación y Rol: Capa de Persistencia / Modelo de Dominio Clínico.
 * 
 * Trazabilidad (Referencias):
 * - Utilizado por: PlanTratamientoRepository, ConsultaService, ConsultaController.
 * - Relaciones:
 *   - Vinculado a Tratamiento (@ManyToOne): Procedimiento clínico a realizar.
 *   - Vinculado a EvaluacionClinica (@ManyToOne): Diagnóstico clínico origen.
 *   - Referenciado en DetallePrescripcion y EvolucionTratamiento.
 */
@Data
@Entity
@Table(name = "plan_tratamiento")
public class PlanTratamiento {

    /** Identificador único autoincremental del plan de tratamiento */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPlanTratamiento;

    /** Tratamiento o procedimiento odontológico seleccionado */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tratamiento", nullable = false)
    private Tratamiento tratamiento;

    /** Evaluación clínica y diagnóstico que originó este plan */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_evaluacion_clinica", nullable = false)
    private EvaluacionClinica evaluacionClinica;

    /** Número identificador de la pieza dental (según nomenclatura dental estándar internacional) */
    @Column(nullable = false)
    private Integer piezaDental;

    /** Estado actual del plan de tratamiento (PENDIENTE, EN_PROGRESO, COMPLETADO, etc.) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPlan estadoPlan;
}