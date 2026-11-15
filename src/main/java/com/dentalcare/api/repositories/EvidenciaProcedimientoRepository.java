package com.dentalcare.api.repositories;

import com.dentalcare.api.models.EvidenciaProcedimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvidenciaProcedimientoRepository extends JpaRepository<EvidenciaProcedimiento, Integer> {

    List<EvidenciaProcedimiento> findByPlanTratamiento_IdPlanTratamientoOrderByFechaSubidaDesc(Integer idPlanTratamiento);

    long countByPlanTratamiento_IdPlanTratamiento(Integer idPlanTratamiento);
}
