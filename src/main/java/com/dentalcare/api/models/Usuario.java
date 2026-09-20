package com.dentalcare.api.models;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Usuario
 *
 * <b>Propósito:</b>
 * Entidad JPA que modela la tabla 'usuario' en la base de datos relacional.
 * Representa la cuenta de acceso de cualquier operador (administrador, recepcionista, odontólogo),
 * almacenando su identidad única, correo, estado de activación, hash de contraseña y rol asignado.
 *
 * <b>Ubicación y Rol en la Arquitectura:</b>
 * - Capa: Dominio / Modelo de Datos (JPA Entity).
 * - Rol: Entidad raíz de autenticación y autorización en el modelo relacional.
 *
 * <b>Trazabilidad (Referencias):</b>
 * - Referenciado por: {@link Rol}, {@link Odontologo}, {@link RegistroAcceso},
 *   {@link com.dentalcare.api.repositories.UsuarioRepository}.
 */
@Data
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idUsuario;

    @Column(nullable = false, unique = true)
    private String usernameUsuario;

    @Column(nullable = false)
    private String passworUsuario;

    @Column(nullable = false, unique = true)
    private String emailUsuario;

    @Column(nullable = false)
    private Boolean esActivo;

    @Column(nullable = false)
    private String nombreUsuario;

    @Column(nullable = false)
    private String apellidoUsuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;
}