package com.dentalcare.api.controllers;

import com.dentalcare.api.dtos.Seguridad.*;
import com.dentalcare.api.services.SeguridadMantenimientoService;
import jakarta.validation.Valid;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class SeguridadMantenimientoController {

    private final SeguridadMantenimientoService seguridadMantenimientoService;

    public SeguridadMantenimientoController(SeguridadMantenimientoService seguridadMantenimientoService) {
        this.seguridadMantenimientoService = seguridadMantenimientoService;
    }

    // =========================================================================
    //  ENDPOINTS DE SEGURIDAD / RESTABLECIMIENTO (DENTALCARE-93)
    // =========================================================================

    @PostMapping("/seguridad/restablecer-password/{idUsuario}")
    public ResponseEntity<?> restablecerPassword(@PathVariable Integer idUsuario, Principal principal) {
        try {
            String adminUsername = principal != null ? principal.getName() : "admin";
            RestablecerPasswordResponseDTO response = seguridadMantenimientoService.restablecerPassword(idUsuario, adminUsername);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Contraseña restablecida exitosamente. Se ha generado clave provisional y token de recuperación.",
                    "data", response
            ));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "status", "error",
                    "message", ex.getMessage()
            ));
        }
    }

    @GetMapping("/seguridad/validar-token/{token}")
    public ResponseEntity<?> validarToken(@PathVariable String token) {
        try {
            Map<String, Object> data = seguridadMantenimientoService.validarToken(token);
            return ResponseEntity.ok(Map.of("status", "success", "data", data));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "status", "error",
                    "message", ex.getMessage()
            ));
        }
    }

    @PostMapping("/seguridad/cambiar-password-obligatorio")
    public ResponseEntity<?> cambiarPasswordObligatorio(@Valid @RequestBody CambiarPasswordDTO dto) {
        try {
            seguridadMantenimientoService.cambiarPasswordObligatorio(dto);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Contraseña actualizada exitosamente. Ahora puede iniciar sesión con su nueva clave."
            ));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "status", "error",
                    "message", ex.getMessage()
            ));
        }
    }

    // =========================================================================
    //  ENDPOINTS DE RESPALDOS Y RESTAURACIÓN (DENTALCARE-53, DENTALCARE-64)
    // =========================================================================

    @PostMapping("/mantenimiento/respaldos/generar")
    public ResponseEntity<?> generarRespaldo(Principal principal) {
        try {
            String adminUsername = principal != null ? principal.getName() : "admin";
            RespaldoResponseDTO response = seguridadMantenimientoService.generarRespaldo(adminUsername);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "status", "success",
                    "message", "Respaldo completado exitosamente.",
                    "data", response
            ));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "error",
                    "message", ex.getMessage()
            ));
        }
    }

    @GetMapping("/mantenimiento/respaldos")
    public ResponseEntity<?> listarRespaldos() {
        try {
            List<RespaldoResponseDTO> respaldos = seguridadMantenimientoService.listarRespaldos();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "data", respaldos
            ));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "error",
                    "message", ex.getMessage()
            ));
        }
    }

    @GetMapping("/mantenimiento/respaldos/{id}/descargar")
    public ResponseEntity<?> descargarRespaldo(@PathVariable Integer id) {
        try {
            File file = seguridadMantenimientoService.obtenerArchivoRespaldo(id);
            Resource resource = new FileSystemResource(file);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(file.length())
                    .body(resource);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "status", "error",
                    "message", ex.getMessage()
            ));
        }
    }

    @PostMapping("/mantenimiento/respaldos/{id}/restaurar")
    public ResponseEntity<?> restaurarRespaldo(@PathVariable Integer id,
                                               @Valid @RequestBody RestaurarRespaldoRequestDTO dto,
                                               Principal principal) {
        try {
            String adminUsername = principal != null ? principal.getName() : null;
            seguridadMantenimientoService.restaurarRespaldo(id, dto, adminUsername);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Base de datos restaurada exitosamente a partir del respaldo seleccionado."
            ));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "status", "error",
                    "message", ex.getMessage()
            ));
        }
    }

    @PostMapping(value = "/mantenimiento/respaldos/restaurar-archivo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> restaurarDesdeArchivo(@RequestParam("file") MultipartFile file,
                                                  @RequestParam("adminPassword") String adminPassword,
                                                  @RequestParam("confirmacionTexto") String confirmacionTexto,
                                                  @RequestParam(value = "adminUsername", required = false) String adminUsername,
                                                  Principal principal) {
        try {
            String user = adminUsername != null ? adminUsername : (principal != null ? principal.getName() : null);
            seguridadMantenimientoService.restaurarDesdeArchivo(file, adminPassword, confirmacionTexto, user);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Base de datos restaurada exitosamente desde el archivo cargado."
            ));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "status", "error",
                    "message", ex.getMessage()
            ));
        }
    }
}
