package com.dentalcare.api.repositories;

import com.dentalcare.api.models.RegistroAcceso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * RegistroAccesoRepository
 *
 * <b>Propósito:</b>
 * Provee las operaciones de persistencia y consulta para la pista de auditoría {@link RegistroAcceso}.
 * Permite registrar de forma inmutable cada intento de inicio de sesión y recuperar el historial
 * completo para labores de inspección de seguridad.
 *
 * <b>Ubicación y Rol en la Arquitectura:</b>
 * - Capa: Acceso a Datos / Capa de Persistencia (@Repository).
 * - Rol: Repositorio JPA para la tabla 'registro_acceso' en MySQL.
 *
 * <b>Trazabilidad (Referencias):</b>
 * - Invocado por: {@link com.dentalcare.api.services.AutentificacionService} (para insertar accesos)
 *   y {@link com.dentalcare.api.controllers.RegistroAccesoController} (para consulta administrativa).
 * - Consume: Entidad {@link RegistroAcceso}.
 */
@Repository
public interface RegistroAccesoRepository extends JpaRepository<RegistroAcceso, Integer> {
}