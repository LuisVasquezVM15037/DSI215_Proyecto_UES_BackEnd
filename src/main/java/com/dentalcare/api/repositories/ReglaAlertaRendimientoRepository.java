package com.dentalcare.api.repositories;

import com.dentalcare.api.models.ReglaAlertaRendimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReglaAlertaRendimientoRepository extends JpaRepository<ReglaAlertaRendimiento, Integer> {

    List<ReglaAlertaRendimiento> findByActivoTrue();

    List<ReglaAlertaRendimiento> findAllByOrderByCreadoEnDesc();
}
