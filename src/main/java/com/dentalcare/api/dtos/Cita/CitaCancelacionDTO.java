package com.dentalcare.api.dtos.Cita;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Propósito: Objeto de transferencia de datos (DTO) utilizado para solicitar la cancelación de una cita médica.
 * Exige la justificación obligatoria del motivo por el cual se anula la cita agendada.
 * 
 * Ubicación y Rol: Capa de Presentación / DTO de Entrada (Data Transfer Object).
 * 
 * Trazabilidad (Referencias):
 * - Utilizado por: CitaController.cancelarCita(), CitaService.cancelarCita().
 * - Consumido por: Modales de confirmación de cancelación en el calendario o lista de citas.
 */
@Data
public class CitaCancelacionDTO {

    /** Motivo explicativo de la anulación de la cita médica (obligatorio y no en blanco) */
    @NotBlank(message = "El motivo de cancelación es obligatorio")
    private String motivoCancelacion;
}

