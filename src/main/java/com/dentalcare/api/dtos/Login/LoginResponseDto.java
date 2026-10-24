package com.dentalcare.api.dtos.Login;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta tras autenticacion exitosa.
 * Contiene el JWT y datos basicos del usuario para el frontend.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {

    // Token JWT que el cliente debe enviar en el header Authorization: Bearer <token>
    private String token;

    private String nombreCompleto;

    // Nombre del rol (ej. "ADMIN", "DOCTOR", "SECRETARIA")
    private String rol;

    private Boolean debeCambiarPassword = false;

    private String username;

    public LoginResponseDto(String token, String nombreCompleto, String rol) {
        this.token = token;
        this.nombreCompleto = nombreCompleto;
        this.rol = rol;
        this.debeCambiarPassword = false;
        this.username = "";
    }
}
