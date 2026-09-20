package com.dentalcare.api.repositories;

import com.dentalcare.api.models.EvaluacionClinica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * EvaluacionClinicaRepository
 *
 * <b>Propósito:</b>
 * Provee la persistencia y lectura de diagnósticos y evaluaciones clínicas asociadas a citas odontológicas.
 *
 * <b>Ubicación y Rol en la Arquitectura:</b>
 * - Capa: Acceso a Datos / Capa de Persistencia (@Repository).
 * - Rol: Repositorio JPA para la tabla 'evaluacion_clinica' en MySQL.
 *
 * <b>Trazabilidad (Referencias):</b>
 * - Invocado por: {@link com.dentalcare.api.services.ConsultaService}.
 * - Consume: Entidad {@link EvaluacionClinica}.
 */
@Repository
public interface EvaluacionClinicaRepository extends JpaRepository<EvaluacionClinica, Integer> {

    /**
     * Localiza la evaluación clínica única asociada a una cita médica.
     *
     * <b>Propósito:</b>
     * Determinar la existencia de diagnóstico previo para aplicar lógica de actualización (upsert).
     *
     * @param idCita Identificador de la cita médica.
     * @return {@link Optional} con la evaluación clínica si existe.
     */
    Optional<EvaluacionClinica> findByCita_IdCitas(Integer idCita);
}
