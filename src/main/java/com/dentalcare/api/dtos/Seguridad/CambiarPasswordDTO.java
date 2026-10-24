package com.dentalcare.api.dtos.Seguridad;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CambiarPasswordDTO {

    private String username;

    private String passwordActual;

    private String token;

    @NotBlank(message = "La nueva contraseña es obligatoria.")
    @Size(min = 6, message = "La nueva contraseña debe contener al menos 6 caracteres.")
    private String passwordNueva;

    private String confirmarPasswordNueva;
}
