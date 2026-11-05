package com.dentalcare.api.controllers;

import com.dentalcare.api.dtos.Cita.CitaProximaDto;
import com.dentalcare.api.dtos.Cita.ConfiguracionAlertaCitaDto;
import com.dentalcare.api.services.AlertaCitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para recordatorios y alertas de citas próximas (30 minutos).
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
}
