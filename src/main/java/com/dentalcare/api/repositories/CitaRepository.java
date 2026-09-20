package com.dentalcare.api.repositories;

import com.dentalcare.api.models.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * CitaRepository
 *
 * <b>Propósito:</b>
 * Provee la capa de acceso a datos para la entidad {@link Cita}.
 * Expone operaciones de persistencia relacional estándar y métodos de consulta derivados
 * basados en convenciones de nomenclatura de Spring Data JPA.
 *
 * <b>Ubicación y Rol en la Arquitectura:</b>
 * - Capa: Acceso a Datos / Capa de Persistencia (@Repository).
 * - Rol: Abstracción de acceso a la tabla 'cita' en MySQL que aísla las consultas SQL/JPQL
 *   de las capas superiores de servicio.
 *
 * <b>Trazabilidad (Referencias):</b>
 * - Invocado por: {@link com.dentalcare.api.services.CitaService},
 *   {@link com.dentalcare.api.services.PacienteService} y {@link com.dentalcare.api.services.ConsultaService}.
 * - Consume: Entidad {@link Cita}.
 */
@Repository
public interface CitaRepository extends JpaRepository<Cita, Integer> {

    /**
     * Recupera todas las citas ordenadas cronológicamente por fecha y hora de inicio de forma ascendente.
     *
     * <b>Propósito:</b>
     * Servir a la agenda médica con el orden secuencial natural de los turnos de atención.
     *
     * @return Lista de entidades {@link Cita} ordenadas ascendentemente.
     */
    List<Cita> findAllByOrderByFechaCitaAscHoraInicioCitaAsc();

    /**
     * Verifica la existencia de al menos una cita vinculada a un paciente específico.
     *
     * <b>Propósito:</b>
     * Comprobar restricciones de clave foránea antes de una operación de eliminación de paciente.
     *
     * @param idPaciente Clave primaria del paciente a comprobar.
     * @return true si el paciente posee citas en su historial; false si no tiene registros dependientes.
     */
    boolean existsByPaciente_IdPaciente(Integer idPaciente);
}