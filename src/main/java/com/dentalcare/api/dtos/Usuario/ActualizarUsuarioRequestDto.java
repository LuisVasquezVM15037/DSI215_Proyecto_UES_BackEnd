package com.dentalcare.api.dtos.Usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Propósito: Objeto de transferencia de datos (DTO) de entrada para la actualización de un usuario existente.
 * Permite la modificación selectiva de los datos de perfil y rol. La contraseña es de carácter opcional:
 * si se recibe nula o en blanco, se conserva la contraseña preexistente sin re-hashear.
 * 
 * Ubicación y Rol: Capa de Presentación / DTO de Entrada (Data Transfer Object).
 * 
 * Trazabilidad (Referencias):
 * - Utilizado por: UsuarioController.actualizarUsuario(), UsuarioService.actualizarUsuario().
 * - Consumido por: Formulario de edición de usuario en el panel administrativo.
 */
@Data
public class ActualizarUsuarioRequestDto {

    /** Nombre de pila del usuario */
    @NotBlank(message = "El nombre es obligatorio.")
    private String nombreUsuario;

    /** Apellidos del usuario */
    @NotBlank(message = "El apellido es obligatorio.")
    private String apellidoUsuario;

    /** Dirección de correo electrónico único */
    @NotBlank(message = "El email es obligatorio.")
    @Email(message = "El formato del email no es valido.")
    private String emailUsuario;

    /** Nombre de usuario único para credenciales de acceso */
    @NotBlank(message = "El username es obligatorio.")
    private String usernameUsuario;

    /** 
     * Nueva contraseña en texto plano (opcional).
     * Si no se envía o viene vacía, el servicio conserva el hash anterior.
     */
    private String password;

    /** Identificador del rol de seguridad a asignar (RBAC) */
    @NotNull(message = "El rol es obligatorio.")
    private Integer idRol;

    /** Estado de activación de la cuenta (true: habilitado, false: deshabilitado) */
    @NotNull(message = "El estado es obligatorio.")
    private Boolean esActivo;

    /** Especialidad médica si el rol corresponde a ODONTOLOGO */
    private String especialidadOdontologo;

    /** Número de JVPO si el rol asignado es ODONTOLOGO */
    private String jvpoId;
}

