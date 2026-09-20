package com.dentalcare.api.repositories;

import com.dentalcare.api.models.Medicamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * MedicamentoRepository
 *
 * <b>Propósito:</b>
 * Provee la persistencia y lectura para el inventario y catálogo farmacéutico de la clínica.
 *
 * <b>Ubicación y Rol en la Arquitectura:</b>
 * - Capa: Acceso a Datos / Capa de Persistencia (@Repository).
 * - Rol: Repositorio JPA para la tabla 'medicamento' en MySQL.
 *
 * <b>Trazabilidad (Referencias):</b>
 * - Invocado por: {@link com.dentalcare.api.services.ConsultaService}.
 * - Consume: Entidad {@link Medicamento}.
 */
@Repository
public interface MedicamentoRepository extends JpaRepository<Medicamento, Integer> {
}
