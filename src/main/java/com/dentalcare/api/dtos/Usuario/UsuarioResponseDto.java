package com.dentalcare.api.dtos.Usuario;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para operaciones sobre Usuario.
 * Nunca expone la password, ni siquiera el hash.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponseDto {

    private Integer idUsuario;
    private String nombreUsuario;
    private String apellidoUsuario;
    private String emailUsuario;
    private String usernameUsuario;
    private Boolean esActivo;
    private String rol;
    private String especialidadOdontologo; // null si el rol no es Odontologo
    private String jvpoId;                 // null si el rol no es Odontologo
    private String fotoPerfilUrl;

    public UsuarioResponseDto(
            Integer idUsuario,
            String nombreUsuario,
            String apellidoUsuario,
            String emailUsuario,
            String usernameUsuario,
            Boolean esActivo,
            String rol,
            String especialidadOdontologo,
            String jvpoId) {
        this(idUsuario, nombreUsuario, apellidoUsuario, emailUsuario, usernameUsuario, esActivo, rol, especialidadOdontologo, jvpoId, null);
    }
}
