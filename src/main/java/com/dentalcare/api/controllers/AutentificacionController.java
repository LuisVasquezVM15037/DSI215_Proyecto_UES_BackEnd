package com.dentalcare.api.controllers;

import com.dentalcare.api.dtos.Login.LoginRequestDto;
import com.dentalcare.api.dtos.Login.LoginResponseDto;
import com.dentalcare.api.services.AutentificacionService;
import com.dentalcare.api.services.SeguridadMantenimientoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador REST para autenticacion de usuarios y recuperacion de claves.
 * Expone endpoints publicos para /api/auth.
 */
@RestController
@RequestMapping("/api/auth")
public class AutentificacionController {

    private final AutentificacionService authService;
    private final SeguridadMantenimientoService seguridadMantenimientoService;

    public AutentificacionController(AutentificacionService authService,
                                     SeguridadMantenimientoService seguridadMantenimientoService) {
        this.authService = authService;
        this.seguridadMantenimientoService = seguridadMantenimientoService;
    }

    /**
     * Autentica al usuario y retorna un JWT.
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto request) {
        try {
            LoginResponseDto response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", ex.getMessage()));
        }
    }

    /**
     * Solicita enlace de recuperacion de contrasena para auto-servicio.
     * POST /api/auth/solicitar-recuperacion
     * Body: { "identificador": "correo@clinica.com" o "usuario" }
     */
    @PostMapping("/solicitar-recuperacion")
    public ResponseEntity<?> solicitarRecuperacion(@RequestBody Map<String, String> body) {
        try {
            String identificador = body.get("identificador");
            Map<String, Object> resp = seguridadMantenimientoService.solicitarRecuperacionPassword(identificador);
            return ResponseEntity.ok(resp);
        } catch (RuntimeException ex) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", ex.getMessage()));
        }
    }
}
