package com.dentalcare.api.models;

import com.dentalcare.api.models.enums.EstadoEvolucion;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Propósito: Modela el progreso, seguimiento y evolución clínica de un procedimiento o plan
 * de tratamiento a lo largo del tiempo. Registra las sesiones de avance realizadas en cada cita,
 * su estado clínico y las observaciones o notas médicas redactadas por el odontólogo tratante.
 * 
 * Ubicación y Rol: Capa de Persistencia / Modelo de Dominio Clínico y Seguimiento.
 * 
 * Trazabilidad (Referencias):
 * - Relaciones:
 *   - Vinculado a PlanTratamiento (@ManyToOne): El plan global al que se le realiza el seguimiento.
 *   - Vinculado a Cita (@ManyToOne): La sesión o cita médica en la que se aplicó el avance.
 */
@Data
@Entity
@Table(name = "evolucion_tratamiento")
public class EvolucionTratamiento {

    /** Identificador único autoincremental del registro de evolución */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idEvolucionTratamiento;

    /** Plan de tratamiento sobre el cual se realiza este reporte de avance */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_plan_tratamiento", nullable = false)
    private PlanTratamiento planTratamiento;

    /** Cita médica durante la cual tuvo lugar este progreso clínico */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cita", nullable = false)
    private Cita cita;

    /** Estado evolutivo del tratamiento (INICIADO, CONTINUACION, FINALIZADO, etc.) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoEvolucion estadoEvolucionTratamiento;

    /** Fecha y hora en la que se efectuó la sesión o intervención de tratamiento */
    @Column(nullable = false)
    private LocalDateTime fechaTratamiento;

    /** Anotaciones clínicas detalladas sobre el procedimiento efectuado y respuesta del paciente */
    @Column(columnDefinition = "TEXT")
    private String notasEvolucionTratamiento;
}