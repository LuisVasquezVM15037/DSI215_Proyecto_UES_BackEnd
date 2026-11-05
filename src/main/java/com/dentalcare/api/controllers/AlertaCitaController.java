package com.dentalcare.api.controllers;

import com.dentalcare.api.dtos.Cita.CitaProximaDto;
import com.dentalcare.api.dtos.Cita.CitaRecordatorioDto;
import com.dentalcare.api.dtos.Cita.ConfiguracionAlertaCitaDto;
import com.dentalcare.api.services.AlertaCitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para recordatorios y alertas de citas.
 * DENTALCARE-96 (PBI-Secret-15)
 */
@RestController
@RequestMapping("/api/agenda/alertas-citas")
@CrossOrigin(origins = "*")
public class AlertaCitaController {

    @Autowired
    private AlertaCitaService alertaCitaService;

    @GetMapping("/configuracion")
    public ResponseEntity<ConfiguracionAlertaCitaDto> obtenerConfiguracion(
            @RequestParam(required = false) Integer usuarioId) {
        ConfiguracionAlertaCitaDto config = alertaCitaService.obtenerConfiguracion(usuarioId);
        return ResponseEntity.ok(config);
    }

    @PutMapping("/configuracion")
    public ResponseEntity<ConfiguracionAlertaCitaDto> guardarConfiguracion(
            @RequestParam(required = false) Integer usuarioId,
            @RequestBody ConfiguracionAlertaCitaDto dto) {
        ConfiguracionAlertaCitaDto guardada = alertaCitaService.guardarConfiguracion(usuarioId, dto);
        return ResponseEntity.ok(guardada);
    }

    @GetMapping("/proximas")
    public ResponseEntity<List<CitaProximaDto>> obtenerCitasProximas(
            @RequestParam(required = false) Integer usuarioId) {
        List<CitaProximaDto> proximas = alertaCitaService.obtenerCitasProximas(usuarioId);
        return ResponseEntity.ok(proximas);
    }

    /**
     * Endpoint para consultar las citas que requieren envío de recordatorio (ej. el día de mañana).
     */
    @GetMapping("/recordatorios")
    public ResponseEntity<List<CitaRecordatorioDto>> obtenerCitasParaRecordatorio(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        List<CitaRecordatorioDto> citas = alertaCitaService.obtenerCitasParaRecordatorio(fecha);
        return ResponseEntity.ok(citas);
    }

    /**
     * Registra o simula el envío del recordatorio (WhatsApp o Correo) y genera trazabilidad.
     */
    @PostMapping("/{idCita}/enviar-recordatorio")
    public ResponseEntity<Map<String, Object>> enviarRecordatorio(
            @PathVariable Integer idCita,
            @RequestParam(required = false, defaultValue = "WHATSAPP") String canal) {
        alertaCitaService.registrarEnvioRecordatorio(idCita, canal);
        Map<String, Object> resp = new HashMap<>();
        resp.put("status", "success");
        resp.put("mensaje", "Recordatorio registrado y enviado exitosamente por " + canal);
        return ResponseEntity.ok(resp);
    }

    /**
     * Registra o simula el envío masivo de recordatorios para una fecha o conjunto de citas.
     */
    @PostMapping("/enviar-recordatorio-masivo")
    public ResponseEntity<Map<String, Object>> enviarRecordatorioMasivo(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false, defaultValue = "CORREO") String canal,
            @RequestBody(required = false) List<Integer> idsCitas) {
        Map<String, Object> resp = alertaCitaService.registrarEnvioRecordatorioMasivo(fecha, canal, idsCitas);
        return ResponseEntity.ok(resp);
    }
}
