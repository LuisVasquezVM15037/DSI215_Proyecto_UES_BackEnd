package com.dentalcare.api.dtos.Login;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Propósito: Objeto de transferencia de datos (DTO) de entrada para el endpoint de autenticación.
 * Transporta las credenciales ingresadas por el usuario (nombre de usuario o correo y contraseña en texto plano).
 * 
 * Ubicación y Rol: Capa de Presentación / DTO de Entrada (Data Transfer Object).
 * 
 * Trazabilidad (Referencias):
 * - Utilizado por: AutentificacionController.login(), AutentificacionService.autenticarUsuario().
 * - Consumido por: Formularios de Login en el cliente web/móvil.
 */
@Data
public class LoginRequestDto {

    /** Identificador único del usuario (admite nombre de usuario o dirección de correo electrónico) */
    @NotBlank(message = "El campo username no puede estar vacio.")
    private String username;

    /** Contraseña en texto plano para su contrastación criptográfica mediante BCrypt */
    @NotBlank(message = "El campo password no puede estar vacio.")
    private String password;
}