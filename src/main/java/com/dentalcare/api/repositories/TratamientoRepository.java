package com.dentalcare.api.repositories;

import com.dentalcare.api.models.Tratamiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * TratamientoRepository
 *
 * <b>Propósito:</b>
 * Provee la persistencia y lectura para el catálogo clínico de procedimientos y aranceles odontológicos.
 *
 * <b>Ubicación y Rol en la Arquitectura:</b>
 * - Capa: Acceso a Datos / Capa de Persistencia (@Repository).
 * - Rol: Repositorio JPA para la tabla 'tratamiento' en MySQL.
 *
 * <b>Trazabilidad (Referencias):</b>
 * - Invocado por: {@link com.dentalcare.api.services.ConsultaService}.
 * - Consume: Entidad {@link Tratamiento}.
 */
@Repository
public interface TratamientoRepository extends JpaRepository<Tratamiento, Integer> {
}
