package com.dentalcare.api.dtos.Cita;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CitaCancelacionDTO {
    @NotBlank(message = "El motivo de cancelación es obligatorio")
    private String motivoCancelacion;
}
