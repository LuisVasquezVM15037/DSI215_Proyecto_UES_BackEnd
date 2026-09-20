package com.dentalcare.api.dtos.Login;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Propósito: Objeto de transferencia de datos (DTO) emitido tras una autenticación exitosa.
 * Contiene el token JWT firmado para la autorización subsiguiente de peticiones HTTP,
 * junto con el nombre completo y rol del usuario para el control de vistas en el cliente.
 * 
 * Ubicación y Rol: Capa de Presentación / DTO de Respuesta (Data Transfer Object).
 * 
 * Trazabilidad (Referencias):
 * - Utilizado por: AutentificacionController.login(), AutentificacionService.autenticarUsuario().
 * - Consumido por: Almacenamiento local (localStorage / sesión) y enrutadores del frontend.
 */
@Data
@AllArgsConstructor
public class LoginResponseDto {

    /** Token JWT compacto que el cliente debe adjuntar en el encabezado 'Authorization: Bearer <token>' */
    private String token;

    /** Nombre y apellido del usuario autenticado para visualización en el perfil */
    private String nombreCompleto;

    /** Nombre del rol asignado (ej. ADMIN, RECEPCIONISTA, ODONTOLOGO) para control RBAC en UI */
    private String rol;
}