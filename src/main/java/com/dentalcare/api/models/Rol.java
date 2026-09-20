package com.dentalcare.api.models;

import com.dentalcare.api.models.enums.NombreRol;
import jakarta.persistence.*;
import lombok.Data;

/**
 * Propósito: Representa la entidad de persistencia 'rol' para el modelo de control de acceso
 * basado en roles (RBAC). Almacena los roles del sistema asignables a las cuentas de usuario.
 * 
 * Ubicación y Rol: Capa de Persistencia / Modelo de Dominio de Seguridad y Autorización.
 * 
 * Trazabilidad (Referencias):
 * - Utilizado por: RolRepository, RolController, UsuarioService, AutentificacionService.
 * - Relaciones:
 *   - Referenciado en Usuario (@ManyToOne) para establecer el perfil de permisos.
 */
@Data
@Entity
@Table(name = "rol")
public class Rol {

    /** Identificador único autoincremental del rol */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idRol;

    /** Nombre tipificado del rol representado mediante el enumerador NombreRol */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NombreRol nombreRol;
}