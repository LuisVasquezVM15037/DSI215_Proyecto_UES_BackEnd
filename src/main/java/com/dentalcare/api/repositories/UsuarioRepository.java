package com.dentalcare.api.repositories;

import com.dentalcare.api.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * UsuarioRepository
 *
 * <b>Propósito:</b>
 * Provee la interfaz de persistencia para la entidad {@link Usuario}.
 * Define consultas derivadas y una consulta JPQL personalizada con parámetros indexados
 * para la resolución de credenciales durante la autenticación.
 *
 * <b>Ubicación y Rol en la Arquitectura:</b>
 * - Capa: Acceso a Datos / Capa de Persistencia (@Repository).
 * - Rol: Repositorio JPA que interactúa con la tabla 'usuario' de MySQL.
 *
 * <b>Trazabilidad (Referencias):</b>
 * - Invocado por: {@link com.dentalcare.api.services.AutentificacionService} y {@link com.dentalcare.api.services.UsuarioService}.
 * - Consume: Entidad {@link Usuario}.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    /**
     * Localiza un usuario a partir de su nombre de usuario único.
     *
     * <b>Propósito:</b>
     * Validar unicidad durante altas y modificaciones de cuentas.
     *
     * @param usernameUsuario Nombre de usuario a buscar.
     * @return {@link Optional} con el usuario encontrado o vacío si no existe.
     */
    Optional<Usuario> findByUsernameUsuario(String usernameUsuario);

    /**
     * Localiza un usuario a partir de su dirección de correo electrónico única.
     *
     * <b>Propósito:</b>
     * Validar colisiones de correo electrónico corporativo o personal.
     *
     * @param emailUsuario Dirección de correo electrónico.
     * @return {@link Optional} con el usuario correspondiente.
     */
    Optional<Usuario> findByEmailUsuario(String emailUsuario);

    /**
     * Localiza un usuario evaluando coincidencia indistinta con su username o su correo.
     *
     * <b>Propósito:</b>
     * Permitir al usuario iniciar sesión indistintamente con su apodo de cuenta o su dirección de correo.
     *
     * @param identifier Cadena que representa el nombre de usuario o correo ingresado.
     * @return {@link Optional} con la entidad {@link Usuario} coincidente.
     */
    @Query("SELECT u FROM Usuario u WHERE u.usernameUsuario = :identifier OR u.emailUsuario = :identifier")
    Optional<Usuario> findByUsernameOrEmail(@Param("identifier") String identifier);
}
