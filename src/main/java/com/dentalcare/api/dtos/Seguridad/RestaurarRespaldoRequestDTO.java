package com.dentalcare.api.dtos.Seguridad;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RestaurarRespaldoRequestDTO {

    @NotBlank(message = "La contraseña de administrador es obligatoria para confirmar la restauración.")
    private String adminPassword;

    @NotBlank(message = "Debe escribir la confirmación 'RESTAURAR' para proceder.")
    private String confirmacionTexto;

    private String adminUsername;
}
