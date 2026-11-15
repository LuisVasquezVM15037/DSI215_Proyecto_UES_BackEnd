package com.dentalcare.api.dtos.Consulta.Tratamiento;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO de respuesta para Tratamiento.
 * Usado para poblar el selector de tratamientos en el panel del odontograma.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TratamientoResponseDTO {

    private Integer idTratamiento;
    private String nombreTratamiento;
    private String descripcionTratamiento;
    private BigDecimal costoTratamiento;
    private Boolean esPorPieza;

    public TratamientoResponseDTO(Integer idTratamiento, String nombreTratamiento, String descripcionTratamiento, BigDecimal costoTratamiento) {
        this.idTratamiento = idTratamiento;
        this.nombreTratamiento = nombreTratamiento;
        this.descripcionTratamiento = descripcionTratamiento;
        this.costoTratamiento = costoTratamiento;
        this.esPorPieza = true;
    }
}
