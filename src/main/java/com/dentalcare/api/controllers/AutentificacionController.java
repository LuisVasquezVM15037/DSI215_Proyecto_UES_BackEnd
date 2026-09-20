package com.dentalcare.api.controllers;

import com.dentalcare.api.dtos.Login.LoginRequestDto;
import com.dentalcare.api.dtos.Login.LoginResponseDto;
import com.dentalcare.api.services.AutentificacionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AutentificacionController
 *
 * <b>Propósito:</b>
 * Expone los puntos de entrada HTTP públicos para el inicio de sesión y emisión de credenciales
 * de acceso en formato JSON Web Token (JWT).
 *
 * <b>Ubicación y Rol en la Arquitectura:</b>
 * - Capa: Presentación / Controlador REST (@RestController).
 * - Rol: Punto de entrada a la API para la autenticación de usuarios. Desacoplado de la lógica
 *   de seguridad interna, delega la validación en {@link AutentificacionService}.
 *
 * <b>Trazabilidad (Referencias):</b>
 * - Invocado por: Cliente frontend (servicios como auth.service.js) mediante solicitudes HTTP POST.
 * - Consume / Dependencias: {@link AutentificacionService}.
 */
@RestController
@RequestMapping("/api/auth")
public class AutentificacionController {

    private final AutentificacionService authService;

    /**
     * Constructor para inyección de dependencias.
     *
     * @param authService Servicio de negocio que procesa la autenticación.
     */
    public AutentificacionController(AutentificacionService authService) {
        this.authService = authService;
    }

    /**
     * Procesa la solicitud de inicio de sesión de un usuario.
     *
     * <b>Propósito:</b>
     * Recibir credenciales en el cuerpo de la petición, verificar su validez a través de la capa
     * de servicio y responder con un token JWT firmado o un código 401 si las credenciales fallan.
     *
     * <b>Trazabilidad:</b>
     * - Endpoint: POST /api/auth/login
     *
     * @param request DTO {@link LoginRequestDto} validado con las anotaciones de Bean Validation.
     * @return {@link ResponseEntity} con estado 200 OK y el {@link LoginResponseDto} en caso de éxito,
     *         o estado 401 UNAUTHORIZED con mensaje estructurado en JSON ante fallas de autenticación.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto request) {
        try {
            LoginResponseDto response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            // Se responde explícitamente con HTTP 401 en JSON para que el cliente web active la redirección
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", ex.getMessage()));
        }
    }
}