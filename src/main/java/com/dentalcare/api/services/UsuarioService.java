package com.dentalcare.api.services;

import com.dentalcare.api.dtos.Usuario.CrearUsuarioRequestDto;
import com.dentalcare.api.dtos.Usuario.ActualizarUsuarioRequestDto;
import com.dentalcare.api.dtos.Usuario.UsuarioResponseDto;
import com.dentalcare.api.models.Odontologo;
import com.dentalcare.api.models.Rol;
import com.dentalcare.api.models.Usuario;
import com.dentalcare.api.models.enums.NombreRol;
import com.dentalcare.api.repositories.OdontologoRepository;
import com.dentalcare.api.repositories.RolRepository;
import com.dentalcare.api.repositories.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * UsuarioService
 *
 * <b>Propósito:</b>
 * Administra las cuentas de usuario y el control de acceso al sistema.
 * Gestiona el alta con cifrado de credenciales mediante BCrypt, la asignación de roles,
 * la sincronización polimórfica del perfil profesional de odontólogo (JVPO y especialidad),
 * la actualización de contraseñas y la baja lógica (soft delete).
 *
 * <b>Ubicación y Rol en la Arquitectura:</b>
 * - Capa: Lógica de Negocio / Capa de Servicios (@Service).
 * - Rol: Gestor de identidad y autorización que centraliza las reglas de seguridad
 *   de credenciales y la relación 1:1 entre Usuario y Odontólogo.
 *
 * <b>Trazabilidad (Referencias):</b>
 * - Invocado por: {@link com.dentalcare.api.controllers.UsuarioController}.
 * - Consume / Dependencias: {@link UsuarioRepository}, {@link RolRepository},
 *   {@link PasswordEncoder}, {@link OdontologoRepository}.
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final OdontologoRepository odontologoRepository;

    /**
     * Constructor para inyección de dependencias por inversión de control.
     *
     * @param usuarioRepository Repositorio para entidades de usuario.
     * @param rolRepository Repositorio para consulta y validación de roles.
     * @param passwordEncoder Componente para hashing unidireccional de contraseñas.
     * @param odontologoRepository Repositorio para la extensión de datos del odontólogo.
     */
    public UsuarioService(UsuarioRepository usuarioRepository,
                          RolRepository rolRepository,
                          PasswordEncoder passwordEncoder,
                          OdontologoRepository odontologoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.odontologoRepository = odontologoRepository;
    }

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * <b>Propósito:</b>
     * Validar unicidad de username y email, hashear la contraseña inicial, asociar el rol
     * correspondiente y, si el rol es ODONTOLOGO, crear simultáneamente su registro profesional.
     *
     * <b>Trazabilidad:</b>
     * - Invocado por: POST /api/usuarios.
     *
     * @param request DTO {@link CrearUsuarioRequestDto} con credenciales y datos personales.
     * @return DTO {@link UsuarioResponseDto} con la cuenta creada (sin exponer hash de contraseña).
     * @throws RuntimeException Si el username, email o JVPO ya existen, o si faltan datos requeridos para odontólogo.
     */
    @Transactional
    public UsuarioResponseDto crearUsuario(CrearUsuarioRequestDto request) {

        if (usuarioRepository.findByUsernameUsuario(request.getUsernameUsuario()).isPresent()) {
            throw new RuntimeException("El username '" + request.getUsernameUsuario() + "' ya esta en uso.");
        }

        if (usuarioRepository.findByEmailUsuario(request.getEmailUsuario()).isPresent()) {
            throw new RuntimeException("El email '" + request.getEmailUsuario() + "' ya esta registrado.");
        }

        Rol rol = rolRepository.findById(request.getIdRol())
                .orElseThrow(() -> new RuntimeException("El rol con id " + request.getIdRol() + " no existe."));

        boolean esOdontologo = rol.getNombreRol() == NombreRol.ODONTOLOGO;

        // Se exige acreditación profesional (JVPO) para todo usuario con privilegios clínicos de odontólogo
        if (esOdontologo) {
            if (request.getEspecialidadOdontologo() == null || request.getEspecialidadOdontologo().isBlank()) {
                throw new RuntimeException("La especialidad es obligatoria para el rol Odontologo.");
            }
            if (request.getJvpoId() == null || request.getJvpoId().isBlank()) {
                throw new RuntimeException("El numero de JVPO es obligatorio para el rol Odontologo.");
            }
            if (odontologoRepository.findByJvpoId(request.getJvpoId()).isPresent()) {
                throw new RuntimeException("El JVPO '" + request.getJvpoId() + "' ya esta registrado.");
            }
        }

        Usuario nuevo = new Usuario();
        nuevo.setNombreUsuario(request.getNombreUsuario());
        nuevo.setApellidoUsuario(request.getApellidoUsuario());
        nuevo.setEmailUsuario(request.getEmailUsuario());
        nuevo.setUsernameUsuario(request.getUsernameUsuario());
        // Se aplica BCrypt antes de persistir para asegurar que ninguna contraseña viva en texto plano
        nuevo.setPassworUsuario(passwordEncoder.encode(request.getPassword()));
        nuevo.setEsActivo(true);
        nuevo.setRol(rol);
        Usuario usuario = usuarioRepository.save(nuevo);

        // Si corresponde al rol médico, se extiende la entidad vinculando la clave foránea del usuario
        if (esOdontologo) {
            Odontologo odontologo = new Odontologo();
            odontologo.setUsuario(usuario);
            odontologo.setEspecialidadOdontologo(request.getEspecialidadOdontologo());
            odontologo.setJvpoId(request.getJvpoId());
            odontologoRepository.save(odontologo);
        }

        return toResponseDto(usuario);
    }

    /**
     * Actualiza la información personal, rol y estado de una cuenta de usuario.
     *
     * <b>Propósito:</b>
     * Permitir la edición controlada de perfiles, comprobando colisiones de identificadores
     * únicos y actualizando la contraseña únicamente si se suministra un nuevo valor.
     *
     * <b>Trazabilidad:</b>
     * - Invocado por: PUT /api/usuarios/{id}.
     *
     * @param id Clave primaria del usuario a modificar.
     * @param request DTO {@link ActualizarUsuarioRequestDto} con los nuevos parámetros.
     * @return DTO {@link UsuarioResponseDto} con los datos actualizados.
     * @throws RuntimeException Si el usuario no existe o se vulnera la unicidad de credenciales.
     */
    @Transactional
    public UsuarioResponseDto actualizarUsuario(Integer id, ActualizarUsuarioRequestDto request) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario con id " + id + " no encontrado."));

        if (!usuario.getUsernameUsuario().equals(request.getUsernameUsuario()) &&
                usuarioRepository.findByUsernameUsuario(request.getUsernameUsuario()).isPresent()) {
            throw new RuntimeException("El username '" + request.getUsernameUsuario() + "' ya esta en uso.");
        }

        if (!usuario.getEmailUsuario().equals(request.getEmailUsuario()) &&
                usuarioRepository.findByEmailUsuario(request.getEmailUsuario()).isPresent()) {
            throw new RuntimeException("El email '" + request.getEmailUsuario() + "' ya esta registrado.");
        }

        Rol rol = rolRepository.findById(request.getIdRol())
                .orElseThrow(() -> new RuntimeException("El rol con id " + request.getIdRol() + " no existe."));

        boolean esOdontologo = rol.getNombreRol() == NombreRol.ODONTOLOGO;

        if (esOdontologo) {
            if (request.getEspecialidadOdontologo() == null || request.getEspecialidadOdontologo().isBlank()) {
                throw new RuntimeException("La especialidad es obligatoria para el rol Odontologo.");
            }
            if (request.getJvpoId() == null || request.getJvpoId().isBlank()) {
                throw new RuntimeException("El numero de JVPO es obligatorio para el rol Odontologo.");
            }

            // Se permite al usuario conservar su propio número de JVPO sin generar falso positivo de duplicidad
            Optional<Odontologo> conMismoJvpo = odontologoRepository.findByJvpoId(request.getJvpoId());
            if (conMismoJvpo.isPresent() && !conMismoJvpo.get().getUsuario().getIdUsuario().equals(id)) {
                throw new RuntimeException("El JVPO '" + request.getJvpoId() + "' ya esta registrado.");
            }
        }

        usuario.setNombreUsuario(request.getNombreUsuario());
        usuario.setApellidoUsuario(request.getApellidoUsuario());
        usuario.setEmailUsuario(request.getEmailUsuario());
        usuario.setUsernameUsuario(request.getUsernameUsuario());
        usuario.setEsActivo(request.getEsActivo());
        usuario.setRol(rol);

        // La contraseña solo se recalcula y cifra si el cliente envía un valor no vacío en la petición
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            usuario.setPassworUsuario(passwordEncoder.encode(request.getPassword()));
        }

        Usuario actualizado = usuarioRepository.save(usuario);

        // Sincronización del perfil clínico si el rol actual o actualizado corresponde a Odontólogo
        if (esOdontologo) {
            Odontologo odontologo = odontologoRepository.findByUsuario_IdUsuario(id)
                    .orElseGet(() -> {
                        Odontologo nuevo = new Odontologo();
                        nuevo.setUsuario(actualizado);
                        return nuevo;
                    });
            odontologo.setEspecialidadOdontologo(request.getEspecialidadOdontologo());
            odontologo.setJvpoId(request.getJvpoId());
            odontologoRepository.save(odontologo);
        }

        return toResponseDto(actualizado);
    }

    /**
     * Realiza la baja lógica (soft delete) de un usuario en el sistema.
     *
     * <b>Propósito:</b>
     * Inhabilitar el acceso al usuario (`esActivo = false`) sin eliminar físicamente el registro,
     * preservando la integridad de auditoría histórica en citas, recetas y registros de acceso.
     *
     * <b>Trazabilidad:</b>
     * - Invocado por: DELETE /api/usuarios/{id}.
     *
     * @param id Clave primaria del usuario a desactivar.
     * @throws RuntimeException Si el usuario no existe.
     */
    public void eliminarUsuario(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario con id " + id + " no encontrado."));
        // Se adopta borrado lógico para no romper la trazabilidad histórica de los registros asociados
        usuario.setEsActivo(false);
        usuarioRepository.save(usuario);
    }

    /**
     * Recupera el catálogo completo de usuarios registrados.
     *
     * <b>Propósito:</b>
     * Proveer al módulo de administración de usuarios la lista para visualización y gestión de accesos.
     *
     * <b>Trazabilidad:</b>
     * - Invocado por: GET /api/usuarios.
     *
     * @return Lista de {@link UsuarioResponseDto}.
     */
    public List<UsuarioResponseDto> listarUsuarios() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Convierte una entidad Usuario en su DTO de respuesta seguro.
     *
     * <b>Propósito:</b>
     * Descartar el hash de la contraseña y enriquecer la respuesta con datos profesionales
     * de especialidad y JVPO si el rol es ODONTOLOGO.
     *
     * @param usuario Entidad cargada desde la base de datos.
     * @return DTO {@link UsuarioResponseDto} sin información confidencial de autenticación.
     */
    private UsuarioResponseDto toResponseDto(Usuario usuario) {
        String especialidad = null;
        String jvpoId = null;

        if (usuario.getRol().getNombreRol() == NombreRol.ODONTOLOGO) {
            Optional<Odontologo> odontologo = odontologoRepository.findByUsuario_IdUsuario(usuario.getIdUsuario());
            if (odontologo.isPresent()) {
                especialidad = odontologo.get().getEspecialidadOdontologo();
                jvpoId = odontologo.get().getJvpoId();
            }
        }

        return new UsuarioResponseDto(
                usuario.getIdUsuario(),
                usuario.getNombreUsuario(),
                usuario.getApellidoUsuario(),
                usuario.getEmailUsuario(),
                usuario.getUsernameUsuario(),
                usuario.getEsActivo(),
                usuario.getRol().getNombreRol().name(),
                especialidad,
                jvpoId);
    }
}