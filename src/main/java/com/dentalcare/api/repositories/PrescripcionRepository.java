package com.dentalcare.api.repositories;

import com.dentalcare.api.models.Prescripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * PrescripcionRepository
 *
 * <b>Propósito:</b>
 * Provee la persistencia y lectura de cabeceras de recetas y prescripciones médicas.
 *
 * <b>Ubicación y Rol en la Arquitectura:</b>
 * - Capa: Acceso a Datos / Capa de Persistencia (@Repository).
 * - Rol: Repositorio JPA para la tabla 'prescripcion' en MySQL.
 *
 * <b>Trazabilidad (Referencias):</b>
 * - Invocado por: {@link com.dentalcare.api.services.ConsultaService}.
 * - Consume: Entidad {@link Prescripcion}.
 */
@Repository
public interface PrescripcionRepository extends JpaRepository<Prescripcion, Integer> {

    /**
     * Localiza la prescripción médica generada para una cita médica.
     *
     * <b>Propósito:</b>
     * Comprobar la existencia previa de receta en una cita y recuperar su clave primaria.
     *
     * @param idCita Identificador de la cita médica.
     * @return {@link Optional} con la prescripción asociada.
     */
    Optional<Prescripcion> findByCita_IdCitas(Integer idCita);
}
