package com.dentalcare.api.controllers;

import com.dentalcare.api.dtos.Cita.DisponibilidadHorarioDTO;
import com.dentalcare.api.services.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/agenda")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class AgendaController {

    @Autowired
    private CitaService citaService;

    /**
     * Endpoint directo de la guía de implementación Sprint 4:
     * GET /api/agenda/disponibilidad/?odontologo_id=4&fecha=2026-10-24
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
}
