package com.dentalcare.api.services;

import com.dentalcare.api.dtos.Login.LoginRequestDto;
import com.dentalcare.api.dtos.Login.LoginResponseDto;
import com.dentalcare.api.models.RegistroAcceso;
import com.dentalcare.api.models.Usuario;
import com.dentalcare.api.repositories.RegistroAccesoRepository;
import com.dentalcare.api.repositories.UsuarioRepository;
import com.dentalcare.api.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * AutentificacionService
 *
 * <b>Propósito:</b>
 * Encapsula la lógica de negocio del proceso de autenticación de usuarios.
 * Valida credenciales, comprueba el estado activo de la cuenta, audita intentos
 * de acceso exitosos y fallidos, y orquesta la emisión del token JWT.
 *
 * <b>Ubicación y Rol en la Arquitectura:</b>
 * - Capa: Lógica de Negocio / Capa de Servicios (@Service).
 * - Rol: Coordinador de seguridad a nivel de aplicación que desacopla la persistencia
 *   y las utilidades criptográficas del controlador web.
 *
 * <b>Trazabilidad (Referencias):</b>
 * - Invocado por: {@link com.dentalcare.api.controllers.AutentificacionController#login}.
 * - Consume / Dependencias: {@link UsuarioRepository}, {@link PasswordEncoder}, {@link JwtUtil}, {@link RegistroAccesoRepository}.
 */
@Service
public class AutentificacionService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RegistroAccesoRepository registroAccesoRepository;

    /**
     * Constructor para inyección de dependencias por inversión de control.
     *
     * @param usuarioRepository Repositorio para consulta de entidades de usuario.
     * @param passwordEncoder Componente para cotejo de resúmenes criptográficos BCrypt.
     * @param jwtUtil Utilidad para generación y firmado de tokens JWT.
     * @param registroAccesoRepository Repositorio para persistir eventos de auditoría de acceso.
     */
    public AutentificacionService(UsuarioRepository usuarioRepository,
                                  PasswordEncoder passwordEncoder,
                                  JwtUtil jwtUtil,
                                  RegistroAccesoRepository registroAccesoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.registroAccesoRepository = registroAccesoRepository;
    }

    /**
     * Registra un evento de intento de acceso en la pista de auditoría.
     *
     * <b>Propósito:</b>
     * Almacenar de forma inmutable la marca temporal y el resultado del inicio de sesión
     * para trazabilidad de seguridad y cumplimiento normativo.
     *
     * @param usuario Instancia del usuario que intentó autenticarse.
     * @param exitoso Indicador booleano que señala si la autenticación fue correcta o fallida.
     */
    private void registrarAcceso(Usuario usuario, boolean exitoso) {
        RegistroAcceso registroAcceso = new RegistroAcceso();
        registroAcceso.setUsuario(usuario);
        registroAcceso.setEsExitoso(exitoso);
        registroAcceso.setFechaAcceso(LocalDateTime.now());

        registroAccesoRepository.save(registroAcceso);
    }

    /**
     * Ejecuta el flujo completo de autenticación de un usuario.
     *
     * <b>Propósito:</b>
     * Localizar al usuario por nombre de cuenta o correo, verificar que no esté suspendido,
     * comparar la contraseña en texto plano contra el hash BCrypt y emitir el JWT correspondiente.
     *
     * <b>Trazabilidad:</b>
     * - Invocado por: POST /api/auth/login.
     *
     * @param request DTO que contiene el identificador (username/email) y la contraseña ingresada.
     * @return DTO {@link LoginResponseDto} con el token JWT, nombre completo y rol del usuario.
     * @throws RuntimeException Si el usuario no existe, la cuenta está inactiva o la contraseña es errónea.
     */
    public LoginResponseDto login(LoginRequestDto request) {

        // Se permite la autenticación mediante username o email indistintamente para flexibilidad del usuario
        Usuario usuario = usuarioRepository
                .findByUsernameOrEmail(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Credenciales incorrectas."));

        // Se valida el estado lógico antes de procesar la contraseña para impedir el acceso a cuentas dadas de baja
        if (!usuario.getEsActivo()) {
            registrarAcceso(usuario, false);
            throw new RuntimeException("La cuenta se encuentra desactivada. Contacta al administrador.");
        }

        // Se utiliza passwordEncoder.matches para evitar ataques de temporización (timing attacks)
        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassworUsuario())) {
            registrarAcceso(usuario, false);
            // Mensaje intencionalmente genérico para mitigar la enumeración de cuentas válidas
            throw new RuntimeException("Credenciales incorrectas.");
        }

        // Registro de auditoría positivo tras superar todas las validaciones de seguridad
        registrarAcceso(usuario, true);

        // Se obtiene el valor textual del enum del rol para incluirlo como claim de autorización
        String nombreRol = usuario.getRol().getNombreRol().name();

        String token = jwtUtil.generateToken(usuario.getUsernameUsuario(), nombreRol);

        // Se construye el nombre compuesto para visualización inmediata en la interfaz de usuario
        String nombreCompleto = usuario.getNombreUsuario() + " " + usuario.getApellidoUsuario();

        return new LoginResponseDto(token, nombreCompleto, nombreRol);
    }
}