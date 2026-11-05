package com.dentalcare.api.controllers;

import com.dentalcare.api.dtos.Notificacion.NotificacionResponseDto;
import com.dentalcare.api.dtos.Notificacion.ResumenNotificacionesDto;
import com.dentalcare.api.models.enums.TipoNotificacion;
import com.dentalcare.api.services.NotificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST para el Centro Unificado de Notificaciones.
 * DENTALCARE-71 (PBI-Secret-16)
 */
@RestController
@RequestMapping("/api/notificaciones")
@CrossOrigin(origins = "*")
public class NotificacionController {

    @Autowired
    private NotificacionService notificacionService;

    @GetMapping
    public ResponseEntity<ResumenNotificacionesDto> obtenerNotificaciones(
            @RequestParam(required = false) Integer usuarioId,
            @RequestParam(required = false) String rol,
            @RequestParam(required = false, defaultValue = "false") Boolean soloNoLeidas,
            @RequestParam(required = false, defaultValue = "20") Integer limit) {
        ResumenNotificacionesDto resumen = notificacionService.obtenerResumen(usuarioId, rol, soloNoLeidas, limit);
        return ResponseEntity.ok(resumen);
    }

    @GetMapping("/resumen-no-leidas")
    public ResponseEntity<Map<String, Object>> obtenerConteoNoLeidas(
            @RequestParam(required = false) Integer usuarioId,
            @RequestParam(required = false) String rol) {
        long total = notificacionService.contarNoLeidas(usuarioId, rol);
        Map<String, Object> response = new HashMap<>();
        response.put("contador", total);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/marcar-leida")
    public ResponseEntity<NotificacionResponseDto> marcarComoLeida(@PathVariable Integer id) {
        NotificacionResponseDto dto = notificacionService.marcarComoLeida(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/marcar-todas-leidas")
    public ResponseEntity<Map<String, Object>> marcarTodasComoLeidas(
            @RequestParam(required = false) Integer usuarioId,
            @RequestParam(required = false) String rol) {
        int afectadas = notificacionService.marcarTodasComoLeidas(usuarioId, rol);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("marcadas", afectadas);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/crear")
    public ResponseEntity<NotificacionResponseDto> crearManual(
            @RequestParam(required = false) Integer usuarioId,
            @RequestParam(required = false) String rolDestinatario,
            @RequestParam String titulo,
            @RequestParam String mensaje,
            @RequestParam(required = false) TipoNotificacion tipo,
            @RequestParam(required = false) Integer referenciaId,
            @RequestParam(required = false) String rutaRedireccion) {
        NotificacionResponseDto notif = notificacionService.crearNotificacion(
                usuarioId, rolDestinatario, titulo, mensaje, tipo, referenciaId, rutaRedireccion
        );
        return ResponseEntity.ok(notif);
    }
}
