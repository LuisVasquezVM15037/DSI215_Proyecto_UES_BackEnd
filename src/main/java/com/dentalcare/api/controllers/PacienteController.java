package com.dentalcare.api.controllers;

import com.dentalcare.api.dtos.Paciente.PacienteRequestDto;
import com.dentalcare.api.dtos.Paciente.PacienteResponseDto;
import com.dentalcare.api.services.PacienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * PacienteController
 *
 * <b>Propósito:</b>
 * Publica los servicios REST para la administración y búsqueda de pacientes.
 * Provee operaciones para listar, consultar con filtros en tiempo real, dar de alta fichas clínicas,
 * actualizar datos de contacto y realizar eliminaciones controladas.
 *
 * <b>Ubicación y Rol en la Arquitectura:</b>
 * - Capa: Presentación / Controlador REST (@RestController).
 * - Rol: Punto de entrada HTTP para la gestión de expedientes de pacientes.
 *
 * <b>Trazabilidad (Referencias):</b>
 * - Invocado por: Cliente frontend (servicios como paciente.service.js).
 * - Consume / Dependencias: {@link PacienteService}.
 */
@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {

    private final PacienteService pacienteService;

    /**
     * Constructor para inyección de dependencias.
     *
     * @param pacienteService Servicio de negocio que procesa las operaciones de pacientes.
     */
    public PacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    /**
     * Recupera el catálogo completo de pacientes registrados.
     *
     * <b>Propósito:</b>
     * Proveer el listado general de pacientes para la tabla del módulo de recepción.
     *
     * <b>Trazabilidad:</b>
     * - Endpoint: GET /api/pacientes
     *
     * @return {@link ResponseEntity} con estado 200 OK y la lista de {@link PacienteResponseDto}.
     */
    @GetMapping
    public ResponseEntity<List<PacienteResponseDto>> listarTodos() {
        return ResponseEntity.ok(pacienteService.listarTodos());
    }

    /**
     * Realiza búsquedas predictivas por término (nombre, apellido o documento).
     *
     * <b>Propósito:</b>
     * Responder a las consultas de filtrado en tiempo real disparadas desde la barra de búsqueda del frontend.
     *
     * <b>Trazabilidad:</b>
     * - Endpoint: GET /api/pacientes/buscar?term={term}
     *
     * @param term Subcadena de texto enviada como parámetro de consulta.
     * @return {@link ResponseEntity} con los pacientes que coinciden con el término.
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<PacienteResponseDto>> buscar(@RequestParam String term) {
        return ResponseEntity.ok(pacienteService.buscar(term));
    }

    /**
     * Da de alta un nuevo paciente en la clínica.
     *
     * <b>Propósito:</b>
     * Recibir y validar mediante Bean Validation los datos personales del paciente y persistirlos.
     *
     * <b>Trazabilidad:</b>
     * - Endpoint: POST /api/pacientes
     *
     * @param request DTO {@link PacienteRequestDto} validado con @Valid.
     * @return {@link ResponseEntity} con estado 201 CREATED y el {@link PacienteResponseDto} generado.
     */
    @PostMapping
    public ResponseEntity<?> crearPaciente(@Valid @RequestBody PacienteRequestDto request) {
        try {
            PacienteResponseDto response = pacienteService.crearPaciente(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", ex.getMessage()));
        }
    }

    /**
     * Actualiza la ficha de un paciente existente.
     *
     * <b>Propósito:</b>
     * Modificar datos demográficos, médicos o de contacto asociados a un identificador.
     *
     * <b>Trazabilidad:</b>
     * - Endpoint: PUT /api/pacientes/{id}
     *
     * @param id Clave primaria del paciente a modificar.
     * @param request DTO {@link PacienteRequestDto} validado.
     * @return {@link ResponseEntity} con estado 200 OK y el DTO actualizado.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarPaciente(@PathVariable Integer id,
                                                @Valid @RequestBody PacienteRequestDto request) {
        try {
            PacienteResponseDto response = pacienteService.actualizarPaciente(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", ex.getMessage()));
        }
    }

    /**
     * Remueve el registro de un paciente verificando restricciones de integridad.
     *
     * <b>Propósito:</b>
     * Eliminar el registro siempre que no posea citas asociadas; de existir dependencias,
     * retorna 400 BAD REQUEST con un mensaje explicativo.
     *
     * <b>Trazabilidad:</b>
     * - Endpoint: DELETE /api/pacientes/{id}
     *
     * @param id Clave primaria del paciente a eliminar.
     * @return {@link ResponseEntity} con estado 200 OK o código de error semántico.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarPaciente(@PathVariable Integer id) {
        try {
            pacienteService.eliminarPaciente(id);
            return ResponseEntity.ok(Map.of("message", "Paciente eliminado correctamente."));
        } catch (IllegalStateException ex) {
            // Se responde 400 Bad Request cuando la eliminación es rechazada por citas asociadas
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", ex.getMessage()));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", ex.getMessage()));
        }
    }
}