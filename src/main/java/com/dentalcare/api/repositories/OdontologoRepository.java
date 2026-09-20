package com.dentalcare.api.repositories;

import com.dentalcare.api.models.Odontologo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * OdontologoRepository
 *
 * <b>Propósito:</b>
 * Provee los métodos de acceso a datos para la entidad {@link Odontologo}.
 * Permite la búsqueda por credencial profesional (JVPO), por la relación foránea
 * con Usuario y el filtrado de odontólogos en servicio activo.
 *
 * <b>Ubicación y Rol en la Arquitectura:</b>
 * - Capa: Acceso a Datos / Capa de Persistencia (@Repository).
 * - Rol: Repositorio JPA para la tabla 'odontologo' en MySQL.
 *
 * <b>Trazabilidad (Referencias):</b>
 * - Invocado por: {@link com.dentalcare.api.services.UsuarioService},
 *   {@link com.dentalcare.api.services.CitaService} y {@link com.dentalcare.api.controllers.OdontologoController}.
 * - Consume: Entidad {@link Odontologo}.
 */
@Repository
public interface OdontologoRepository extends JpaRepository<Odontologo, Integer> {

    /**
     * Localiza un odontólogo a través de su número de registro en la Junta de Vigilancia (JVPO).
     *
     * <b>Propósito:</b>
     * Validar unicidad de licencia médica en altas y modificaciones de odontólogos.
     *
     * @param jvpoId Número de registro profesional JVPO.
     * @return {@link Optional} con el odontólogo poseedor de la licencia.
     */
    Optional<Odontologo> findByJvpoId(String jvpoId);

    /**
     * Localiza la ficha profesional de odontólogo asociada a una cuenta de usuario.
     *
     * <b>Propósito:</b>
     * Cargar los datos específicos de especialidad y credencial médica al gestionar perfiles de usuario.
     *
     * @param idUsuario Identificador único de la cuenta de usuario vinculada.
     * @return {@link Optional} con el perfil de odontólogo correspondiente.
     */
    Optional<Odontologo> findByUsuario_IdUsuario(Integer idUsuario);

    /**
     * Recupera todos los odontólogos cuyo usuario vinculado se encuentre en estado activo (`esActivo = true`).
     *
     * <b>Propósito:</b>
     * Proveer el listado de profesionales habilitados para asignación en el agendamiento de citas.
     *
     * @return Lista de entidades {@link Odontologo} activas.
     */
    List<Odontologo> findByUsuario_EsActivoTrue();
}
