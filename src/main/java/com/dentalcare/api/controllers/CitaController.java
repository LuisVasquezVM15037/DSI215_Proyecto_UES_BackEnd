package com.dentalcare.api.controllers;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.dentalcare.api.dtos.Cita.CitaCancelacionDTO;
import com.dentalcare.api.dtos.Cita.CitaRequestDTO;
import com.dentalcare.api.dtos.Cita.CitaResponseDTO;
import com.dentalcare.api.dtos.Cita.DisponibilidadHorarioDTO;
import com.dentalcare.api.models.enums.EstadoCita;
import com.dentalcare.api.services.CitaService;

@RestController
@RequestMapping("/api/citas")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class CitaController {

    @Autowired
    private CitaService citaService;

    @GetMapping
    public ResponseEntity<List<CitaResponseDTO>> listarCitas() {
        List<CitaResponseDTO> citas = citaService.obtenerTodas();
        return ResponseEntity.ok(citas);
    }

    @PostMapping
    public ResponseEntity<CitaResponseDTO> registrarCita(@RequestBody CitaRequestDTO request) {
        CitaResponseDTO nuevaCita = citaService.crearCita(request);
        return new ResponseEntity<>(nuevaCita, HttpStatus.CREATED);
    }

    /**
     * IDENTIFICAR HORARIOS DISPONIBLES (DENTALCARE-62 / PBI-Secret-09)
     * GET /api/citas/disponibilidad?idOdontologo=X&fecha=YYYY-MM-DD
     */
    @GetMapping("/disponibilidad")
    public ResponseEntity<DisponibilidadHorarioDTO> obtenerDisponibilidad(
            @RequestParam(name = "idOdontologo", required = false) Integer idOdontologo,
            @RequestParam(name = "odontologo_id", required = false) Integer odontologoId,
            @RequestParam(name = "fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        Integer doctorId = (idOdontologo != null) ? idOdontologo : odontologoId;
        if (doctorId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe especificar el ID del odontólogo.");
        }
        return ResponseEntity.ok(citaService.obtenerDisponibilidad(doctorId, fecha));
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<CitaResponseDTO> cancelarCita(
            @PathVariable Integer id,
            @RequestBody CitaCancelacionDTO cancelacionRequest) {
        CitaResponseDTO citaCancelada = citaService.cancelarCita(id, cancelacionRequest);
        return ResponseEntity.ok(citaCancelada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CitaResponseDTO> actualizarCita(
            @PathVariable Integer id,
            @RequestBody CitaRequestDTO request) {
        return ResponseEntity.ok(citaService.actualizarCita(id, request));
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstadoCita(
            @PathVariable Integer id,
            @RequestBody Map<String, String> body) {
        try {
            String estadoString = body.get("estado").toUpperCase();
            if ("FINALIZADA".equals(estadoString)) {
                estadoString = "COMPLETADA";
            }
            EstadoCita nuevoEstado = EstadoCita.valueOf(estadoString);
            CitaResponseDTO actualizada = citaService.actualizarEstado(id, nuevoEstado);
            return ResponseEntity.ok(actualizada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Estado no válido: " + body.get("estado")));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage() != null ? e.getMessage() : "Error al actualizar estado"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<CitaResponseDTO> obtenerCitaPorId(@PathVariable Integer id) {
        CitaResponseDTO cita = citaService.obtenerPorId(id);
        return ResponseEntity.ok(cita);
    }
}
