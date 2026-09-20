package com.dentalcare.api.repositories;

import com.dentalcare.api.models.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * RolRepository
 *
 * <b>Propósito:</b>
 * Provee la capa de acceso a datos para la entidad {@link Rol}.
 * Permite la lectura del catálogo de roles del sistema para asignación en cuentas de usuario.
 *
 * <b>Ubicación y Rol en la Arquitectura:</b>
 * - Capa: Acceso a Datos / Capa de Persistencia (@Repository).
 * - Rol: Repositorio JPA para la tabla 'rol' en MySQL.
 *
 * <b>Trazabilidad (Referencias):</b>
 * - Invocado por: {@link com.dentalcare.api.services.UsuarioService} y {@link com.dentalcare.api.controllers.RolController}.
 * - Consume: Entidad {@link Rol}.
 */
@Repository
public interface RolRepository extends JpaRepository<Rol, Integer> {
}
