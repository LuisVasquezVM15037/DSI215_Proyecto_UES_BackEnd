package com.dentalcare.api.repositories;

import com.dentalcare.api.models.PlanTratamiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanTratamientoRepository extends JpaRepository<PlanTratamiento, Integer> {

    // Obtiene todos los planes de tratamiento de una evaluacion clinica especifica
    // Usado para cargar el historial de hallazgos registrados en el odontograma
    List<PlanTratamiento> findByEvaluacionClinica_IdEvaluacionClinica(Integer idEvaluacion);

    @Query("SELECT p FROM PlanTratamiento p " +
           "WHERE p.evaluacionClinica.cita.paciente.idPaciente = :idPaciente " +
           "AND p.estadoPlan IN (com.dentalcare.api.models.enums.EstadoPlan.PENDIENTE, com.dentalcare.api.models.enums.EstadoPlan.PROGRAMADO) " +
           "ORDER BY p.idPlanTratamiento DESC")
    List<PlanTratamiento> findPendientesPorPaciente(@Param("idPaciente") Integer idPaciente);
}
