package com.dentalcare.api.dtos.Consulta.Tratamiento;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

/**
 * Propósito: Objeto de transferencia de datos (DTO) de entrada para la creación de un nuevo tratamiento
 * o procedimiento clínico dentro del catálogo odontológico.
 * 
 * Ubicación y Rol: Capa de Presentación / DTO de Entrada (Data Transfer Object).
 * 
 * Trazabilidad (Referencias):
 * - Utilizado por: ConsultaController.crearTratamiento(), ConsultaService.crearTratamiento().
 * - Consumido por: Modal de nuevo procedimiento en la vista del odontograma o configuración de tarifas.
 */
@Data
public class TratamientoRequestDTO {

    /** Nombre o denominación clínica del tratamiento */
    @NotBlank(message = "El nombre del tratamiento es obligatorio.")
    private String nombreTratamiento;

    /** Descripción de la técnica o procedimiento odontológico */
    @NotBlank(message = "La descripcion es obligatoria.")
    private String descripcionTratamiento;

    /** Costo monetario o arancel base del tratamiento */
    @NotNull(message = "El costo es obligatorio.")
    private BigDecimal costoTratamiento;
}