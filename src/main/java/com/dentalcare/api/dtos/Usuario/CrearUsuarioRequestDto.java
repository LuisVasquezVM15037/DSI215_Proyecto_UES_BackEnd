package com.dentalcare.api.dtos.Usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Propósito: Objeto de transferencia de datos (DTO) de entrada para el registro de un nuevo usuario.
 * Reúne de forma obligatoria las credenciales iniciales, información personal y rol.
 * Si el rol especificado es Odontólogo, captura los campos complementarios requeridos para dicha especialidad.
 * 
 * Ubicación y Rol: Capa de Presentación / DTO de Entrada (Data Transfer Object).
 * 
 * Trazabilidad (Referencias):
 * - Utilizado por: UsuarioController.crearUsuario(), UsuarioService.crearUsuario().
 * - Consumido por: Modal o pantalla de creación de usuarios en el módulo de administración.
 */
@Data
public class CrearUsuarioRequestDto {

    /** Nombre de pila del nuevo usuario */
    @NotBlank(message = "El nombre es obligatorio.")
    private String nombreUsuario;

    /** Apellidos del nuevo usuario */
    @NotBlank(message = "El apellido es obligatorio.")
    private String apellidoUsuario;

    /** Dirección de correo electrónico corporativo o personal */
    @NotBlank(message = "El email es obligatorio.")
    @Email(message = "El formato del email no es valido.")
    private String emailUsuario;

    /** Nombre de usuario único para inicio de sesión */
    @NotBlank(message = "El username es obligatorio.")
    private String usernameUsuario;

    /** Contraseña inicial en texto plano (será hasheada con BCrypt antes de persistir) */
    @NotBlank(message = "La contrasena es obligatoria.")
    private String password;

    /** Identificador del rol de acceso asignado */
    @NotNull(message = "El rol es obligatorio.")
    private Integer idRol;

    /** Especialidad médica si el usuario a crear desempeñará el rol de ODONTOLOGO */
    private String especialidadOdontologo;

    /** Código de Junta de Vigilancia (JVPO) requerido si el rol es ODONTOLOGO */
    private String jvpoId;
}

