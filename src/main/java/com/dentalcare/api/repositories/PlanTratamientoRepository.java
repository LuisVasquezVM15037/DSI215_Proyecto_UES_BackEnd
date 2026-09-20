package com.dentalcare.api.repositories;

import com.dentalcare.api.models.PlanTratamiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * PlanTratamientoRepository
 *
 * <b>Propósito:</b>
 * Provee los métodos de acceso a datos para la entidad {@link PlanTratamiento}.
 * Permite consultar los hallazgos clínicos y procedimientos asignados a cada pieza dental
 * dentro de una evaluación clínica específica.
 *
 * <b>Ubicación y Rol en la Arquitectura:</b>
 * - Capa: Acceso a Datos / Capa de Persistencia (@Repository).
 * - Rol: Repositorio JPA para la tabla 'plan_tratamiento' en MySQL.
 *
 * <b>Trazabilidad (Referencias):</b>
 * - Invocado por: {@link com.dentalcare.api.services.ConsultaService}.
 * - Consume: Entidad {@link PlanTratamiento}.
 */
@Repository
public interface PlanTratamientoRepository extends JpaRepository<PlanTratamiento, Integer> {

    /**
     * Recupera todos los planes de tratamiento y hallazgos vinculados a una evaluación clínica.
     *
     * <b>Propósito:</b>
     * Poblar la vista gráfica del odontograma y el panel de procedimientos pendientes/completados.
     *
     * @param idEvaluacion Identificador único de la evaluación clínica.
     * @return Lista de entidades {@link PlanTratamiento}.
     */
    List<PlanTratamiento> findByEvaluacionClinica_IdEvaluacionClinica(Integer idEvaluacion);
}
