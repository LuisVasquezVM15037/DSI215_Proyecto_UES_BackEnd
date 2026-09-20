package com.dentalcare.api.controllers;

import com.dentalcare.api.dtos.Usuario.CrearUsuarioRequestDto;
import com.dentalcare.api.dtos.Usuario.ActualizarUsuarioRequestDto;
import com.dentalcare.api.dtos.Usuario.UsuarioResponseDto;
import com.dentalcare.api.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * UsuarioController
 *
 * <b>Propósito:</b>
 * Controlador REST para la gestión y administración de usuarios y cuentas de acceso.
 * Provee endpoints para la creación de operadores clínicos y administrativos, consulta del
 * directorio de usuarios, actualización de perfiles y desactivación lógica de cuentas.
 *
 * <b>Ubicación y Rol en la Arquitectura:</b>
 * - Capa: Presentación / Controlador REST (@RestController).
 * - Rol: Punto de entrada HTTP protegido por Spring Security para la gestión de usuarios.
 *
 * <b>Trazabilidad (Referencias):</b>
 * - Invocado por: Cliente frontend (servicios como usuario.service.js) en rutas de administración.
 * - Consume / Dependencias: {@link UsuarioService}.
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    /**
     * Constructor para inyección de dependencias.
     *
     * @param usuarioService Servicio de negocio que procesa las cuentas de usuario.
     */
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * Da de alta un nuevo usuario en la plataforma.
     *
     * <b>Propósito:</b>
     * Recibir credenciales y perfil, validar restricciones y persistir la cuenta con contraseña cifrada.
     *
     * <b>Trazabilidad:</b>
     * - Endpoint: POST /api/usuarios
     *
     * @param request DTO {@link CrearUsuarioRequestDto} validado con Bean Validation.
     * @return {@link ResponseEntity} con estado 201 CREATED y el {@link UsuarioResponseDto} creado.
     */
    @PostMapping
    public ResponseEntity<?> crearUsuario(@Valid @RequestBody CrearUsuarioRequestDto request) {
        try {
            UsuarioResponseDto response = usuarioService.crearUsuario(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", ex.getMessage()));
        }
    }

    /**
     * Recupera el listado completo de usuarios registrados.
     *
     * <b>Propósito:</b>
     * Proveer el directorio de operadores para visualización en el panel administrativo.
     *
     * <b>Trazabilidad:</b>
     * - Endpoint: GET /api/usuarios
     *
     * @return {@link ResponseEntity} con estado 200 OK y la lista de {@link UsuarioResponseDto}.
     */
    @GetMapping
    public ResponseEntity<List<UsuarioResponseDto>> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.listarUsuarios());
    }

    /**
     * Actualiza la información de una cuenta de usuario existente.
     *
     * <b>Propósito:</b>
     * Modificar datos personales, rol o estado activo de un usuario identificado por su ID.
     *
     * <b>Trazabilidad:</b>
     * - Endpoint: PUT /api/usuarios/{id}
     *
     * @param id Clave primaria del usuario a modificar.
     * @param request DTO {@link ActualizarUsuarioRequestDto} validado.
     * @return {@link ResponseEntity} con estado 200 OK y el DTO actualizado.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarUsuario(@PathVariable Integer id,
                                               @Valid @RequestBody ActualizarUsuarioRequestDto request) {
        try {
            UsuarioResponseDto response = usuarioService.actualizarUsuario(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", ex.getMessage()));
        }
    }

    /**
     * Ejecuta la baja lógica de un usuario del sistema.
     *
     * <b>Propósito:</b>
     * Desactivar el acceso del operador sin suprimir los registros históricos asociados.
     *
     * <b>Trazabilidad:</b>
     * - Endpoint: DELETE /api/usuarios/{id}
     *
     * @param id Clave primaria del usuario a desactivar.
     * @return {@link ResponseEntity} con estado 200 OK y mensaje de confirmación o 404 si no existe.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarUsuario(@PathVariable Integer id) {
        try {
            usuarioService.eliminarUsuario(id);
            return ResponseEntity.ok(Map.of("message", "Usuario eliminado correctamente."));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", ex.getMessage()));
        }
    }
}
