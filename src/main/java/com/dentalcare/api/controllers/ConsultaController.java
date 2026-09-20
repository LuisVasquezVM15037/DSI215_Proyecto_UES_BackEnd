package com.dentalcare.api.controllers;

import com.dentalcare.api.dtos.Consulta.Evaluacion.EvaluacionClinicaRequestDTO;
import com.dentalcare.api.dtos.Consulta.Evaluacion.EvaluacionClinicaResponseDTO;
import com.dentalcare.api.dtos.Consulta.Medicamento.MedicamentoResponseDTO;
import com.dentalcare.api.dtos.Consulta.Prescripcion.PrescripcionRequestDTO;
import com.dentalcare.api.dtos.Consulta.Prescripcion.PrescripcionResponseDTO;
import com.dentalcare.api.dtos.Consulta.Tratamiento.PlanTratamientoRequestDTO;
import com.dentalcare.api.dtos.Consulta.Tratamiento.PlanTratamientoResponseDTO;
import com.dentalcare.api.dtos.Consulta.Tratamiento.TratamientoRequestDTO;
import com.dentalcare.api.dtos.Consulta.Tratamiento.TratamientoResponseDTO;
import com.dentalcare.api.services.ConsultaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ConsultaController
 *
 * <b>Propósito:</b>
 * Publica los servicios web para el flujo clínico de atención odontológica.
 * Agrupa los endpoints correspondientes a catálogos clínicos (tratamientos, medicamentos),
 * registro de evaluación preliminar, odontograma digital con hallazgos por pieza y
 * emisión de prescripciones farmacológicas.
 *
 * <b>Ubicación y Rol en la Arquitectura:</b>
 * - Capa: Presentación / Controlador REST (@RestController).
 * - Rol: Fachada HTTP que comunica la interfaz del odontólogo con el orquestador {@link ConsultaService}.
 *
 * <b>Trazabilidad (Referencias):</b>
 * - Invocado por: Módulo clínico del frontend (consulta.service.js, usePrescripcion.js).
 * - Consume / Dependencias: {@link ConsultaService}.
 */
@RestController
@RequestMapping("/api/consulta")
public class ConsultaController {

    private final ConsultaService consultaService;

    /**
     * Constructor para inyección de dependencias.
     *
     * @param consultaService Servicio que orquesta las reglas de la consulta clínica.
     */
    public ConsultaController(ConsultaService consultaService) {
        this.consultaService = consultaService;
    }

    /**
     * Obtiene el catálogo de tratamientos disponibles.
     *
     * <b>Propósito:</b>
     * Proveer la lista de procedimientos clínicos para selección en odontograma.
     *
     * <b>Trazabilidad:</b>
     * - Endpoint: GET /api/consulta/tratamientos
     *
     * @return {@link ResponseEntity} con la lista de {@link TratamientoResponseDTO}.
     */
    @GetMapping("/tratamientos")
    public ResponseEntity<List<TratamientoResponseDTO>> listarTratamientos() {
        return ResponseEntity.ok(consultaService.listarTratamientos());
    }

    /**
     * Agrega un nuevo tratamiento al catálogo de procedimientos.
     *
     * <b>Propósito:</b>
     * Permitir la ampliación de procedimientos y aranceles odontológicos.
     *
     * <b>Trazabilidad:</b>
     * - Endpoint: POST /api/consulta/tratamientos
     *
     * @param request DTO {@link TratamientoRequestDTO} con nombre, descripción y costo.
     * @return {@link ResponseEntity} con estado 201 CREATED y el tratamiento creado.
     */
    @PostMapping("/tratamientos")
    public ResponseEntity<?> crearTratamiento(@Valid @RequestBody TratamientoRequestDTO request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(consultaService.crearTratamiento(request));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", ex.getMessage()));
        }
    }

    /**
     * Recupera el catálogo de medicamentos disponibles para prescripción.
     *
     * <b>Propósito:</b>
     * Proveer información de concentraciones, componentes activos y stock para el recetario.
     *
     * <b>Trazabilidad:</b>
     * - Endpoint: GET /api/consulta/medicamentos
     *
     * @return {@link ResponseEntity} con la lista de {@link MedicamentoResponseDTO}.
     */
    @GetMapping("/medicamentos")
    public ResponseEntity<List<MedicamentoResponseDTO>> listarMedicamentos() {
        return ResponseEntity.ok(consultaService.listarMedicamentos());
    }

    /**
     * Registra o actualiza la evaluación clínica general de una cita (Paso 1).
     *
     * <b>Propósito:</b>
     * Guardar el diagnóstico y observaciones iniciales emitidas por el odontólogo.
     *
     * <b>Trazabilidad:</b>
     * - Endpoint: POST /api/consulta/evaluacion
     *
     * @param request DTO {@link EvaluacionClinicaRequestDTO} con datos clínicos.
     * @return {@link ResponseEntity} con la evaluación guardada o 400 ante error.
     */
    @PostMapping("/evaluacion")
    public ResponseEntity<?> guardarEvaluacion(@Valid @RequestBody EvaluacionClinicaRequestDTO request) {
        try {
            return ResponseEntity.ok(consultaService.guardarEvaluacion(request));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
        }
    }

    /**
     * Consulta la evaluación clínica previamente guardada para una cita.
     *
     * <b>Propósito:</b>
     * Cargar los antecedentes de diagnóstico en la vista clínica del frontend.
     *
     * <b>Trazabilidad:</b>
     * - Endpoint: GET /api/consulta/evaluacion/cita/{idCita}
     *
     * @param idCita Clave primaria de la cita médica.
     * @return {@link ResponseEntity} con estado 200 OK y el DTO, o 204 NO_CONTENT si no existe.
     */
    @GetMapping("/evaluacion/cita/{idCita}")
    public ResponseEntity<?> obtenerEvaluacion(@PathVariable Integer idCita) {
        EvaluacionClinicaResponseDTO resultado = consultaService.obtenerEvaluacionPorCita(idCita);
        if (resultado == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(resultado);
    }

    /**
     * Registra un hallazgo dental en el odontograma interactivo (Paso 2).
     *
     * <b>Propósito:</b>
     * Vincular una pieza dental y un tratamiento a la evaluación clínica activa.
     *
     * <b>Trazabilidad:</b>
     * - Endpoint: POST /api/consulta/hallazgo
     *
     * @param request DTO {@link PlanTratamientoRequestDTO} con los detalles del hallazgo.
     * @return {@link ResponseEntity} con estado 201 CREATED y el plan de tratamiento generado.
     */
    @PostMapping("/hallazgo")
    public ResponseEntity<?> registrarHallazgo(@Valid @RequestBody PlanTratamientoRequestDTO request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(consultaService.registrarHallazgo(request));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
        }
    }

    /**
     * Consulta los hallazgos dentales vinculados a una evaluación clínica.
     *
     * <b>Propósito:</b>
     * Renderizar el odontograma gráfico con el estado de cada pieza tratada.
     *
     * <b>Trazabilidad:</b>
     * - Endpoint: GET /api/consulta/hallazgos/{idEvaluacion}
     *
     * @param idEvaluacion Identificador de la evaluación clínica.
     * @return {@link ResponseEntity} con la lista de {@link PlanTratamientoResponseDTO}.
     */
    @GetMapping("/hallazgos/{idEvaluacion}")
    public ResponseEntity<List<PlanTratamientoResponseDTO>> obtenerHallazgos(@PathVariable Integer idEvaluacion) {
        return ResponseEntity.ok(consultaService.obtenerHallazgosPorEvaluacion(idEvaluacion));
    }

    /**
     * Remueve un hallazgo dental del odontograma.
     *
     * <b>Propósito:</b>
     * Descartar tratamientos o diagnósticos asignados erróneamente.
     *
     * <b>Trazabilidad:</b>
     * - Endpoint: DELETE /api/consulta/hallazgo/{id}
     *
     * @param id Identificador del plan de tratamiento a suprimir.
     * @return {@link ResponseEntity} con mensaje confirmatorio o 404 si no existe.
     */
    @DeleteMapping("/hallazgo/{id}")
    public ResponseEntity<?> eliminarHallazgo(@PathVariable Integer id) {
        try {
            consultaService.eliminarHallazgo(id);
            return ResponseEntity.ok(Map.of("message", "Hallazgo eliminado."));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
        }
    }

    /**
     * Actualiza el estado de ejecución de un hallazgo dental.
     *
     * <b>Propósito:</b>
     * Cambiar el estado del tratamiento (ej. PENDIENTE a EN_PROCESO o COMPLETADO).
     *
     * <b>Trazabilidad:</b>
     * - Endpoint: PATCH /api/consulta/hallazgo/{id}/estado
     *
     * @param id Identificador del hallazgo dental.
     * @param payload Mapa JSON con la propiedad "estado".
     * @return {@link ResponseEntity} con el {@link PlanTratamientoResponseDTO} actualizado o código 400/404 ante error.
     */
    @PatchMapping("/hallazgo/{id}/estado")
    public ResponseEntity<?> actualizarEstadoHallazgo(
            @PathVariable Integer id,
            @RequestBody Map<String, String> payload) {
        try {
            String nuevoEstado = (payload != null) ? payload.get("estado") : null;
            PlanTratamientoResponseDTO resultado = consultaService.actualizarEstadoHallazgo(id, nuevoEstado);
            return ResponseEntity.ok(resultado);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
        }
    }

    /**
     * Emite una receta médica y sus líneas de detalle para la cita (Paso 3).
     *
     * <b>Propósito:</b>
     * Generar la prescripción formal con dosis, frecuencia, duración y relación a planes clínicos.
     *
     * <b>Trazabilidad:</b>
     * - Endpoint: POST /api/consulta/prescripcion
     *
     * @param request DTO {@link PrescripcionRequestDTO} validado.
     * @return {@link ResponseEntity} con estado 201 CREATED y el {@link PrescripcionResponseDTO}.
     */
    @PostMapping("/prescripcion")
    public ResponseEntity<?> crearPrescripcion(@Valid @RequestBody PrescripcionRequestDTO request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(consultaService.crearPrescripcion(request));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
        }
    }

    /**
     * Consulta la prescripción médica generada para una cita.
     *
     * <b>Propósito:</b>
     * Cargar el recetario para vista del paciente o reimpresión de fórmula médica.
     *
     * <b>Trazabilidad:</b>
     * - Endpoint: GET /api/consulta/prescripcion/cita/{idCita}
     *
     * @param idCita Clave primaria de la cita.
     * @return {@link ResponseEntity} con el {@link PrescripcionResponseDTO} o 204 NO_CONTENT si no hay receta.
     */
    @GetMapping("/prescripcion/cita/{idCita}")
    public ResponseEntity<?> obtenerPrescripcion(@PathVariable Integer idCita) {
        PrescripcionResponseDTO resultado = consultaService.obtenerPrescripcionPorCita(idCita);
        if (resultado == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(resultado);
    }
}
