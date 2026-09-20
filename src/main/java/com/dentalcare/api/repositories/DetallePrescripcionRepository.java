package com.dentalcare.api.repositories;

import com.dentalcare.api.models.DetallePrescripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * DetallePrescripcionRepository
 *
 * <b>Propósito:</b>
 * Provee la persistencia y lectura para las líneas de detalle de medicamentos dosificados en una prescripción.
 *
 * <b>Ubicación y Rol en la Arquitectura:</b>
 * - Capa: Acceso a Datos / Capa de Persistencia (@Repository).
 * - Rol: Repositorio JPA para la tabla 'detalle_prescripcion' en MySQL.
 *
 * <b>Trazabilidad (Referencias):</b>
 * - Invocado por: {@link com.dentalcare.api.services.ConsultaService}.
 * - Consume: Entidad {@link DetallePrescripcion}.
 */
@Repository
public interface DetallePrescripcionRepository extends JpaRepository<DetallePrescripcion, Integer> {

    /**
     * Recupera todas las líneas de medicamentos asignadas a una prescripción médica.
     *
     * <b>Propósito:</b>
     * Ensamblar la receta completa con sus medicamentos, dosis, frecuencias y duraciones.
     *
     * @param idPrescripcion Identificador de la prescripción cabecera.
     * @return Lista de entidades {@link DetallePrescripcion}.
     */
    List<DetallePrescripcion> findByPrescripcion_IdPrescripcion(Integer idPrescripcion);
}
