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

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final OdontologoRepository odontologoRepository;

    public UsuarioService(UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder,
            OdontologoRepository odontologoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.odontologoRepository = odontologoRepository;
    }

    // CREAR usuario
    @Transactional
    public UsuarioResponseDto crearUsuario(CrearUsuarioRequestDto request) {

        if (usuarioRepository.findByUsernameUsuario(request.getUsernameUsuario()).isPresent())
            throw new RuntimeException("El username '" + request.getUsernameUsuario() + "' ya esta en uso.");

        if (usuarioRepository.findByEmailUsuario(request.getEmailUsuario()).isPresent())
            throw new RuntimeException("El email '" + request.getEmailUsuario() + "' ya esta registrado.");

        Rol rol = rolRepository.findById(request.getIdRol())
                .orElseThrow(() -> new RuntimeException("El rol con id " + request.getIdRol() + " no existe."));

        boolean esOdontologo = rol.getNombreRol() == NombreRol.ODONTOLOGO;

        if (esOdontologo) {
            if (request.getEspecialidadOdontologo() == null || request.getEspecialidadOdontologo().isBlank())
                throw new RuntimeException("La especialidad es obligatoria para el rol Odontologo.");
            if (request.getJvpoId() == null || request.getJvpoId().isBlank())
                throw new RuntimeException("El numero de JVPO es obligatorio para el rol Odontologo.");
            if (odontologoRepository.findByJvpoId(request.getJvpoId()).isPresent())
                throw new RuntimeException("El JVPO '" + request.getJvpoId() + "' ya esta registrado.");
        }

        Usuario nuevo = new Usuario();
        nuevo.setNombreUsuario(request.getNombreUsuario());
        nuevo.setApellidoUsuario(request.getApellidoUsuario());
        nuevo.setEmailUsuario(request.getEmailUsuario());
        nuevo.setUsernameUsuario(request.getUsernameUsuario());
        nuevo.setPassworUsuario(passwordEncoder.encode(request.getPassword()));
        nuevo.setEsActivo(true);
        nuevo.setRol(rol);
        Usuario usuario = usuarioRepository.save(nuevo);

        if (esOdontologo) {
            Odontologo odontologo = new Odontologo();
            odontologo.setUsuario(usuario);
            odontologo.setEspecialidadOdontologo(request.getEspecialidadOdontologo());
            odontologo.setJvpoId(request.getJvpoId());
            odontologoRepository.save(odontologo);
        }

        return toResponseDto(usuario);
    }

    // EDITAR usuario
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
            if (request.getEspecialidadOdontologo() == null || request.getEspecialidadOdontologo().isBlank())
                throw new RuntimeException("La especialidad es obligatoria para el rol Odontologo.");
            if (request.getJvpoId() == null || request.getJvpoId().isBlank())
                throw new RuntimeException("El numero de JVPO es obligatorio para el rol Odontologo.");

            // Permitimos que el propio odontologo conserve su JVPO sin chocar consigo mismo
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

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            usuario.setPassworUsuario(passwordEncoder.encode(request.getPassword()));
        }

        Usuario actualizado = usuarioRepository.save(usuario);

        // Sincronizamos el perfil de Odontologo si el rol resultante lo requiere
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
        // NOTA: si el usuario DEJA de ser Odontologo, su registro en `odontologo`
        // no se borra (para no romper citas ya asociadas a ese id_odontologo).
        // Queda huerfano pero invisible en el listado gracias al filtro
        // findByUsuario_EsActivoTrue si tambien lo desactivas; si solo cambias
        // el rol sin desactivarlo, seguiria apareciendo en el dropdown. Aviso
        // aparte si quieres que resolvamos ese caso tambien.

        return toResponseDto(actualizado);
    }

    // ELIMINAR usuario - en realidad lo desactiva si tiene dependencias
    public void eliminarUsuario(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario con id " + id + " no encontrado."));
        usuario.setEsActivo(false);
        usuarioRepository.save(usuario);
    }

    // LISTAR todos los usuarios
    public List<UsuarioResponseDto> listarUsuarios() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    // Convierte entidad a DTO de respuesta (sin exponer password)
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