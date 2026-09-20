package com.dentalcare.api.models;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Propósito: Modela el detalle individual o renglón de una prescripción farmacológica.
 * Especifica el medicamento prescrito, su posología (dosis, frecuencia y duración),
 * indicaciones especiales para el paciente y la vinculación opcional al plan de tratamiento.
 * 
 * Ubicación y Rol: Capa de Persistencia / Modelo de Dominio Farmacológico.
 * 
 * Trazabilidad (Referencias):
 * - Utilizado por: DetallePrescripcionRepository, ConsultaService, ConsultaController.
 * - Relaciones:
 *   - Vinculado a Prescripcion (@ManyToOne): Cabecera de la receta a la que pertenece.
 *   - Vinculado a Medicamento (@ManyToOne): Fármaco indicado del inventario clínico.
 *   - Vinculado opcionalmente a PlanTratamiento (@ManyToOne): Procedimiento dental que motivó el fármaco.
 */
@Data
@Entity
@Table(name = "detalle_prescripcion")
public class DetallePrescripcion {

    /** Identificador único autoincremental del renglón de prescripción */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idDetallePrescripcion;

    /** Prescripción general o cabecera a la que pertenece este detalle */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_prescripcion", nullable = false)
    private Prescripcion prescripcion;

    /** Fármaco o medicamento recetado */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_medicamento", nullable = false)
    private Medicamento medicamento;

    /** Cantidad dosificada (ej. 500mg, 1 tableta, 5ml) */
    @Column(nullable = false)
    private String dosisPrescripcion;

    /** Intervalo de consumo indicado (ej. Cada 8 horas, Una vez al día con alimentos) */
    @Column(nullable = false)
    private String frecuenciaPrescripcion;

    /** Número de días durante los cuales debe prolongarse la terapia farmacológica */
    @Column(nullable = false)
    private Integer duracionPrescripcion;

    /** Instrucciones médicas complementarias y advertencias para el paciente */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String indicacionesPrescripcion;

    /** 
     * Plan de tratamiento clínico específico al que responde este medicamento (opcional).
     * Permite ligar la medicación a una pieza o intervención específica.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_plan_tratamiento")
    private PlanTratamiento planTratamiento;
}