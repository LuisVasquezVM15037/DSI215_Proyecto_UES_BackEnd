package com.dentalcare.api.repositories;

import com.dentalcare.api.models.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * PacienteRepository
 *
 * <b>Propósito:</b>
 * Provee las operaciones de persistencia y consultas especializadas sobre la entidad {@link Paciente}.
 * Contiene métodos derivados para validación de unicidad de documentos y consultas JPQL
 * insensibles a mayúsculas/minúsculas para el buscador en tiempo real.
 *
 * <b>Ubicación y Rol en la Arquitectura:</b>
 * - Capa: Acceso a Datos / Capa de Persistencia (@Repository).
 * - Rol: Repositorio JPA para la tabla 'paciente' en MySQL.
 *
 * <b>Trazabilidad (Referencias):</b>
 * - Invocado por: {@link com.dentalcare.api.services.PacienteService}.
 * - Consume: Entidad {@link Paciente}.
 */
@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Integer> {

    /**
     * Localiza un paciente por su número de documento de identidad único.
     *
     * <b>Propósito:</b>
     * Comprobar colisiones antes de inserciones o actualizaciones de expedientes.
     *
     * @param numeroIdentidadPaciente Número de DUI o documento de identificación.
     * @return {@link Optional} con el paciente encontrado.
     */
    Optional<Paciente> findByNumeroIdentidadPaciente(String numeroIdentidadPaciente);

    /**
     * Realiza una búsqueda predictiva sobre nombre, apellido o documento de identidad.
     *
     * <b>Propósito:</b>
     * Soportar la búsqueda en tiempo real mediante coincidencia parcial (LIKE '%term%')
     * e insensibilidad a mayúsculas/minúsculas (LOWER).
     *
     * @param term Subcadena de búsqueda ingresada por el operador.
     * @return Lista de entidades {@link Paciente} que satisfacen los criterios.
     */
    @Query("SELECT p FROM Paciente p WHERE " +
           "LOWER(p.nombrePaciente) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(p.apellidoPaciente) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "p.numeroIdentidadPaciente LIKE CONCAT('%', :term, '%')")
    List<Paciente> buscarPorTermino(@Param("term") String term);
}