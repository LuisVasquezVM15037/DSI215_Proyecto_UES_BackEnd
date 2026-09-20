package com.dentalcare.api.dtos.Usuario;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Propósito: Objeto de transferencia de datos (DTO) de salida para la visualización de usuarios.
 * Protege la confidencialidad de la aplicación al nunca exponer contraseñas ni hashes criptográficos
 * hacia el cliente, proyectando además la especialidad profesional si el rol es Odontólogo.
 * 
 * Ubicación y Rol: Capa de Presentación / DTO de Respuesta (Data Transfer Object).
 * 
 * Trazabilidad (Referencias):
 * - Utilizado por: UsuarioController, UsuarioService.
 * - Consumido por: Tablas de administración de usuarios y vistas de perfil en el frontend.
 */
@Data
@AllArgsConstructor
public class UsuarioResponseDto {

    /** Identificador primario del usuario */
    private Integer idUsuario;

    /** Nombre de pila */
    private String nombreUsuario;

    /** Apellidos */
    private String apellidoUsuario;

    /** Dirección de correo electrónico */
    private String emailUsuario;

    /** Nombre de usuario para login */
    private String usernameUsuario;

    /** Estado de la cuenta (activo / inactivo) */
    private Boolean esActivo;

    /** Nombre del rol de seguridad asignado (ej. ADMIN, ODONTOLOGO) */
    private String rol;

    /** Rama o especialidad odontológica (nulo si el usuario no es odontólogo) */
    private String especialidadOdontologo;

    /** Número de registro de la JVPO (nulo si el usuario no es odontólogo) */
    private String jvpoId;
}

