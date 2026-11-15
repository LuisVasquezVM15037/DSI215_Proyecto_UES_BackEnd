package com.dentalcare.api.dtos.Consulta.Tratamiento;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para PlanTratamiento.
 * Aplana las relaciones para facilitar la renderizacion en el panel de hallazgos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanTratamientoResponseDTO {

    private Integer idPlanTratamiento;
    private Integer piezaDental;
    private String estadoPlan;

    // Datos del tratamiento aplanados para evitar objetos anidados
    private Integer idTratamiento;
    private String nombreTratamiento;
    private String descripcionTratamiento;
    private Float precioFloat;
    private Boolean esPorPieza;

    public PlanTratamientoResponseDTO(Integer idPlanTratamiento, Integer piezaDental, String estadoPlan,
                                    Integer idTratamiento, String nombreTratamiento,
                                    String descripcionTratamiento, Float precioFloat) {
        this.idPlanTratamiento = idPlanTratamiento;
        this.piezaDental = piezaDental;
        this.estadoPlan = estadoPlan;
        this.idTratamiento = idTratamiento;
        this.nombreTratamiento = nombreTratamiento;
        this.descripcionTratamiento = descripcionTratamiento;
        this.precioFloat = precioFloat;
        this.esPorPieza = true;
    }
}
