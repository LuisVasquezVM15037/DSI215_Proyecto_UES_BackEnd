package com.dentalcare.api.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dentalcare.api.dtos.Cita.CitaCancelacionDTO;
import com.dentalcare.api.dtos.Cita.CitaRequestDTO;
import com.dentalcare.api.dtos.Cita.CitaResponseDTO;
import com.dentalcare.api.models.enums.EstadoCita;
import com.dentalcare.api.services.CitaService;
import jakarta.validation.Valid;

/**
 * CitaController
 *
 * <b>Propósito:</b>
 * Controlador REST que publica los endpoints para la gestión de la agenda clínica.
 * Proporciona operaciones de consulta cronológica, agendamiento, reprogramación,
 * actualización de estado y cancelación formal de citas odontológicas.
 *
 * <b>Ubicación y Rol en la Arquitectura:</b>
 * - Capa: Presentación / Controlador REST (@RestController).
 * - Rol: Punto de entrada HTTP para la agenda clínica. Transforma peticiones web en llamadas
 *   a la capa de servicios y emite códigos de estado HTTP semánticos (200, 201, 400).
 *
 * <b>Trazabilidad (Referencias):</b>
 * - Invocado por: Cliente frontend (servicios como cita.service.js) en rutas de agenda y calendario.
 * - Consume / Dependencias: {@link CitaService}.
 */
@RestController
@RequestMapping("/api/citas")
public class CitaController {

    private final CitaService citaService;

    /**
     * Constructor para inyección de dependencias.
     *
     * @param citaService Servicio de negocio para operaciones de citas.
     */
    public CitaController(CitaService citaService) {
        this.citaService = citaService;
    }

    /**
     * Lista todas las citas odontológicas ordenadas cronológicamente.
     *
     * <b>Propósito:</b>
     * Proveer el conjunto completo de citas para renderizado en tablas o calendarios.
     *
     * <b>Trazabilidad:</b>
     * - Endpoint: GET /api/citas
     *
     * @return {@link ResponseEntity} con estado 200 OK y la lista de {@link CitaResponseDTO}.
     */
    @GetMapping
    public ResponseEntity<List<CitaResponseDTO>> listarCitas() {
        List<CitaResponseDTO> citas = citaService.obtenerTodas();
        return ResponseEntity.ok(citas);
    }

    /**
     * Consulta una cita puntual mediante su identificador numérico.
     *
     * <b>Propósito:</b>
     * Obtener el detalle individual de una cita para consulta o preparación de edición.
     *
     * <b>Trazabilidad:</b>
     * - Endpoint: GET /api/citas/{id}
     *
     * @param id Identificador de la cita solicitada.
     * @return {@link ResponseEntity} con estado 200 OK y el DTO de la cita.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CitaResponseDTO> obtenerCitaPorId(@PathVariable Integer id) {
        CitaResponseDTO cita = citaService.obtenerPorId(id);
        return ResponseEntity.ok(cita);
    }

    /**
     * Registra una nueva cita clínica en la agenda.
     *
     * <b>Propósito:</b>
     * Validar y dar de alta una nueva cita vinculando al paciente y odontólogo indicados.
     *
     * <b>Trazabilidad:</b>
     * - Endpoint: POST /api/citas
     *
     * @param request DTO {@link CitaRequestDTO} con los datos de agenda.
     * @return {@link ResponseEntity} con estado 201 CREATED y el {@link CitaResponseDTO} creado.
     */
    @PostMapping
    public ResponseEntity<CitaResponseDTO> registrarCita(@RequestBody CitaRequestDTO request) {
        CitaResponseDTO nuevaCita = citaService.crearCita(request);
        return new ResponseEntity<>(nuevaCita, HttpStatus.CREATED);
    }

    /**
     * Modifica los datos de programación de una cita existente.
     *
     * <b>Propósito:</b>
     * Permitir la reprogramación de fecha, hora o profesionales asignados a una cita.
     *
     * <b>Trazabilidad:</b>
     * - Endpoint: PUT /api/citas/{id}
     *
     * @param id Clave primaria de la cita a modificar.
     * @param request DTO {@link CitaRequestDTO} con los valores actualizados.
     * @return {@link ResponseEntity} con estado 200 OK y el DTO actualizado.
     */
    @PutMapping("/{id}")
    public ResponseEntity<CitaResponseDTO> actualizarCita(@PathVariable Integer id,
                                                          @RequestBody CitaRequestDTO request) {
        return ResponseEntity.ok(citaService.actualizarCita(id, request));
    }

    /**
     * Ejecuta la cancelación justificada de una cita agendada.
     *
     * <b>Propósito:</b>
     * Registrar la baja administrativa de una cita requiriendo obligatoriamente un motivo
     * validado por Bean Validation mediante la anotación @Valid.
     *
     * <b>Trazabilidad:</b>
     * - Endpoint: PUT /api/citas/{id}/cancelar
     *
     * @param id Clave primaria de la cita a cancelar.
     * @param cancelacionRequest DTO {@link CitaCancelacionDTO} con el motivo validado con @NotBlank.
     * @return {@link ResponseEntity} con estado 200 OK y la cita con estado CANCELADA.
     */
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<CitaResponseDTO> cancelarCita(
            @PathVariable Integer id,
            @Valid @RequestBody CitaCancelacionDTO cancelacionRequest) {
        CitaResponseDTO citaCancelada = citaService.cancelarCita(id, cancelacionRequest);
        return ResponseEntity.ok(citaCancelada);
    }

    /**
     * Realiza la transición de estado de una cita médica.
     *
     * <b>Propósito:</b>
     * Modificar de forma atómica el estado (ej. PROGRAMADA -> EN_PROGRESO o FINALIZADA).
     *
     * <b>Trazabilidad:</b>
     * - Endpoint: PUT /api/citas/{id}/estado
     *
     * @param id Clave primaria de la cita.
     * @param body Mapa JSON con la propiedad "estado" en formato cadena.
     * @return {@link ResponseEntity} con estado 200 OK o 400 BAD REQUEST ante valores no reconocidos.
     */
    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstadoCita(
            @PathVariable Integer id,
            @RequestBody Map<String, String> body) {
        try {
            String estadoString = body.get("estado") != null ? body.get("estado").trim().toUpperCase() : "";
            EstadoCita nuevoEstado = EstadoCita.valueOf(estadoString);

            CitaResponseDTO actualizada = citaService.actualizarEstado(id, nuevoEstado);
            return ResponseEntity.ok(actualizada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Estado no válido: " + body.get("estado")));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
