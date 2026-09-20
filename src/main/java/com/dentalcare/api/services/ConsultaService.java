package com.dentalcare.api.services;

import com.dentalcare.api.dtos.Consulta.Medicamento.MedicamentoResponseDTO;
import com.dentalcare.api.dtos.Consulta.Evaluacion.EvaluacionClinicaRequestDTO;
import com.dentalcare.api.dtos.Consulta.Evaluacion.EvaluacionClinicaResponseDTO;
import com.dentalcare.api.dtos.Consulta.Prescripcion.PrescripcionRequestDTO;
import com.dentalcare.api.dtos.Consulta.Prescripcion.PrescripcionResponseDTO;
import com.dentalcare.api.dtos.Consulta.Tratamiento.PlanTratamientoRequestDTO;
import com.dentalcare.api.dtos.Consulta.Tratamiento.PlanTratamientoResponseDTO;
import com.dentalcare.api.dtos.Consulta.Tratamiento.TratamientoRequestDTO;
import com.dentalcare.api.dtos.Consulta.Tratamiento.TratamientoResponseDTO;
import com.dentalcare.api.models.*;
import com.dentalcare.api.models.enums.EstadoPlan;
import com.dentalcare.api.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ConsultaService
 *
 * <b>Propósito:</b>
 * Orquesta el flujo clínico integral de una consulta odontológica en sus tres etapas consecutivas:
 * 1. Evaluación clínica general y diagnóstico del paciente.
 * 2. Registro de hallazgos en el odontograma y formulación de planes de tratamiento.
 * 3. Emisión de prescripciones farmacológicas asociadas a la cita y a tratamientos específicos.
 *
 * <b>Ubicación y Rol en la Arquitectura:</b>
 * - Capa: Lógica de Negocio / Capa de Servicios (@Service).
 * - Rol: Orquestador transaccional del expediente clínico que garantiza la atomicidad de las operaciones
 *   compuestas mediante {@link Transactional}.
 *
 * <b>Trazabilidad (Referencias):</b>
 * - Invocado por: {@link com.dentalcare.api.controllers.ConsultaController}.
 * - Consume / Dependencias: {@link CitaRepository}, {@link EvaluacionClinicaRepository},
 *   {@link PlanTratamientoRepository}, {@link TratamientoRepository}, {@link PrescripcionRepository},
 *   {@link DetallePrescripcionRepository}, {@link MedicamentoRepository}.
 */
@Service
public class ConsultaService {

    private final CitaRepository citaRepository;
    private final EvaluacionClinicaRepository evaluacionRepository;
    private final PlanTratamientoRepository planTratamientoRepository;
    private final TratamientoRepository tratamientoRepository;
    private final PrescripcionRepository prescripcionRepository;
    private final DetallePrescripcionRepository detalleRepository;
    private final MedicamentoRepository medicamentoRepository;

    /**
     * Constructor para inyección de dependencias de los repositorios del dominio clínico.
     *
     * @param citaRepository Repositorio para verificación de citas.
     * @param evaluacionRepository Repositorio de evaluaciones clínicas.
     * @param planTratamientoRepository Repositorio de planes de tratamiento y hallazgos.
     * @param tratamientoRepository Repositorio del catálogo de tratamientos.
     * @param prescripcionRepository Repositorio de recetas y prescripciones.
     * @param detalleRepository Repositorio de líneas de detalle de medicamentos prescritos.
     * @param medicamentoRepository Repositorio del catálogo e inventario de medicamentos.
     */
    public ConsultaService(CitaRepository citaRepository,
                           EvaluacionClinicaRepository evaluacionRepository,
                           PlanTratamientoRepository planTratamientoRepository,
                           TratamientoRepository tratamientoRepository,
                           PrescripcionRepository prescripcionRepository,
                           DetallePrescripcionRepository detalleRepository,
                           MedicamentoRepository medicamentoRepository) {
        this.citaRepository = citaRepository;
        this.evaluacionRepository = evaluacionRepository;
        this.planTratamientoRepository = planTratamientoRepository;
        this.tratamientoRepository = tratamientoRepository;
        this.prescripcionRepository = prescripcionRepository;
        this.detalleRepository = detalleRepository;
        this.medicamentoRepository = medicamentoRepository;
    }

    /**
     * Registra o actualiza la evaluación clínica asociada a una cita médica.
     *
     * <b>Propósito:</b>
     * Almacenar el diagnóstico y observaciones preliminares de la cita. Aplica una estrategia
     * de upsert (actualización si ya existe, inserción si es nueva) para evitar duplicados.
     *
     * <b>Trazabilidad:</b>
     * - Invocado por: POST /api/consulta/evaluacion.
     *
     * @param request DTO {@link EvaluacionClinicaRequestDTO} con id de cita, diagnóstico y observaciones.
     * @return DTO {@link EvaluacionClinicaResponseDTO} con la evaluación persistida.
     * @throws RuntimeException Si la cita referenciada no existe.
     */
    @Transactional
    public EvaluacionClinicaResponseDTO guardarEvaluacion(EvaluacionClinicaRequestDTO request) {
        Cita cita = citaRepository.findById(request.getIdCita())
                .orElseThrow(() -> new RuntimeException("Cita no encontrada con id: " + request.getIdCita()));

        // Se busca evaluación previa para reutilizar la misma entidad y evitar duplicidad de diagnósticos en la misma cita
        EvaluacionClinica evaluacion = evaluacionRepository.findByCita_IdCitas(request.getIdCita())
                .orElse(new EvaluacionClinica());

        evaluacion.setCita(cita);
        evaluacion.setDiagnostico(request.getDiagnostico());
        evaluacion.setObservaciones(request.getObservaciones());

        EvaluacionClinica guardada = evaluacionRepository.save(evaluacion);
        return new EvaluacionClinicaResponseDTO(
                guardada.getIdEvaluacionClinica(),
                guardada.getCita().getIdCitas(),
                guardada.getDiagnostico(),
                guardada.getObservaciones());
    }

    /**
     * Obtiene la evaluación clínica de una cita médica.
     *
     * <b>Propósito:</b>
     * Proveer al odontólogo los antecedentes diagnósticos registrados en la sesión actual al abrir el expediente.
     *
     * <b>Trazabilidad:</b>
     * - Invocado por: GET /api/consulta/evaluacion/cita/{idCita}.
     *
     * @param idCita Identificador de la cita médica.
     * @return DTO {@link EvaluacionClinicaResponseDTO} o null si aún no ha sido registrada.
     */
    public EvaluacionClinicaResponseDTO obtenerEvaluacionPorCita(Integer idCita) {
        return evaluacionRepository.findByCita_IdCitas(idCita)
                .map(e -> new EvaluacionClinicaResponseDTO(
                        e.getIdEvaluacionClinica(),
                        e.getCita().getIdCitas(),
                        e.getDiagnostico(),
                        e.getObservaciones()))
                .orElse(null);
    }

    /**
     * Registra un hallazgo dental en el odontograma como un plan de tratamiento.
     *
     * <b>Propósito:</b>
     * Vincular un tratamiento específico a una pieza dental identificada durante la evaluación clínica.
     *
     * <b>Trazabilidad:</b>
     * - Invocado por: POST /api/consulta/hallazgo.
     *
     * @param request DTO {@link PlanTratamientoRequestDTO} con evaluación, tratamiento y pieza dental.
     * @return DTO {@link PlanTratamientoResponseDTO} con los detalles clínicos y de costo.
     * @throws RuntimeException Si la evaluación clínica o el tratamiento no existen.
     */
    @Transactional
    public PlanTratamientoResponseDTO registrarHallazgo(PlanTratamientoRequestDTO request) {
        EvaluacionClinica evaluacion = evaluacionRepository.findById(request.getIdEvaluacionClinica())
                .orElseThrow(() -> new RuntimeException("Evaluacion clinica no encontrada."));

        Tratamiento tratamiento = tratamientoRepository.findById(request.getIdTratamiento())
                .orElseThrow(() -> new RuntimeException("Tratamiento no encontrado."));

        PlanTratamiento plan = new PlanTratamiento();
        plan.setEvaluacionClinica(evaluacion);
        plan.setTratamiento(tratamiento);
        plan.setPiezaDental(request.getPiezaDental());

        // Se define PENDIENTE como estado inicial seguro en caso de valores no especificados o erróneos
        try {
            plan.setEstadoPlan(request.getEstadoPlan() != null ? EstadoPlan.valueOf(request.getEstadoPlan())
                    : EstadoPlan.PENDIENTE);
        } catch (IllegalArgumentException e) {
            plan.setEstadoPlan(EstadoPlan.PENDIENTE);
        }

        PlanTratamiento guardado = planTratamientoRepository.save(plan);
        return toResponseDTO(guardado);
    }

    /**
     * Lista todos los hallazgos y planes asociados a una evaluación clínica.
     *
     * <b>Propósito:</b>
     * Cargar el historial gráfico de piezas intervenidas o por intervenir en el odontograma interactivo.
     *
     * <b>Trazabilidad:</b>
     * - Invocado por: GET /api/consulta/hallazgos/{idEvaluacion}.
     *
     * @param idEvaluacion Identificador de la evaluación clínica.
     * @return Lista de {@link PlanTratamientoResponseDTO} con cada procedimiento planificado.
     */
    public List<PlanTratamientoResponseDTO> obtenerHallazgosPorEvaluacion(Integer idEvaluacion) {
        return planTratamientoRepository
                .findByEvaluacionClinica_IdEvaluacionClinica(idEvaluacion)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Elimina un hallazgo u orden de tratamiento del odontograma.
     *
     * <b>Propósito:</b>
     * Permitir la rectificación de diagnósticos erróneos antes de su ejecución clínica.
     *
     * <b>Trazabilidad:</b>
     * - Invocado por: DELETE /api/consulta/hallazgo/{id}.
     *
     * @param idPlan Identificador del plan de tratamiento a remover.
     * @throws RuntimeException Si el plan no existe.
     */
    @Transactional
    public void eliminarHallazgo(Integer idPlan) {
        if (!planTratamientoRepository.existsById(idPlan)) {
            throw new RuntimeException("Plan de tratamiento no encontrado.");
        }
        planTratamientoRepository.deleteById(idPlan);
    }

    /**
     * Actualiza el estado de ejecución de un hallazgo dental.
     *
     * <b>Propósito:</b>
     * Controlar la máquina de estados del plan (PENDIENTE, EN_PROCESO, COMPLETADO, CANCELADO).
     * Aplica la regla de negocio que prohíbe reactivar o modificar tratamientos ya finalizados o cancelados.
     *
     * <b>Trazabilidad:</b>
     * - Invocado por: PATCH /api/consulta/hallazgo/{id}/estado.
     *
     * @param idPlan Identificador del plan de tratamiento.
     * @param nuevoEstado Nombre del estado destino enviado desde el cliente web.
     * @return DTO {@link PlanTratamientoResponseDTO} con la entidad actualizada.
     * @throws IllegalStateException Si se intenta alterar un plan en estado terminal (COMPLETADO/CANCELADO).
     * @throws IllegalArgumentException Si el estado recibido no coincide con ningún valor del enum.
     * @throws RuntimeException Si el identificador del plan no existe.
     */
    @Transactional
    public PlanTratamientoResponseDTO actualizarEstadoHallazgo(Integer idPlan, String nuevoEstado) {
        PlanTratamiento plan = planTratamientoRepository.findById(idPlan)
                .orElseThrow(() -> new RuntimeException("No se encontró el hallazgo con ID: " + idPlan));

        try {
            EstadoPlan estadoEnum = EstadoPlan
                    .valueOf(nuevoEstado != null ? nuevoEstado.trim().toUpperCase() : "");

            EstadoPlan estadoActual = plan.getEstadoPlan();

            // Regla de inmutabilidad: los estados terminales no pueden regresar a estados previos
            if ((estadoActual == EstadoPlan.COMPLETADO || estadoActual == EstadoPlan.CANCELADO)
                    && estadoActual != estadoEnum) {
                throw new IllegalStateException(
                        "No se puede modificar un hallazgo que ya está " + estadoActual);
            }

            plan.setEstadoPlan(estadoEnum);
            plan = planTratamientoRepository.save(plan);

            return toResponseDTO(plan);

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Estado no válido. Los estados permitidos son: "
                    + Arrays.toString(EstadoPlan.values()));
        }
    }

    /**
     * Emite y persiste una receta médica completa vinculada a una cita.
     *
     * <b>Propósito:</b>
     * Crear la cabecera de la prescripción y cada uno de los renglones de medicamentos dosificados.
     * Vincula opcionalmente cada medicamento con un plan de tratamiento específico para justificación clínica.
     *
     * <b>Trazabilidad:</b>
     * - Invocado por: POST /api/consulta/prescripcion.
     *
     * @param request DTO {@link PrescripcionRequestDTO} con la cita y la lista de medicamentos prescritos.
     * @return DTO {@link PrescripcionResponseDTO} estructurado con los detalles guardados.
     * @throws RuntimeException Si la cita no existe, ya cuenta con receta o un medicamento referenciado no es válido.
     */
    @Transactional
    public PrescripcionResponseDTO crearPrescripcion(PrescripcionRequestDTO request) {
        Cita cita = citaRepository.findById(request.getIdCita())
                .orElseThrow(() -> new RuntimeException("Cita no encontrada."));

        // Se impide duplicación de recetas sobre una misma cita médica
        if (prescripcionRepository.findByCita_IdCitas(request.getIdCita()).isPresent()) {
            throw new RuntimeException("Ya existe una prescripcion para esta cita.");
        }

        Prescripcion prescripcion = new Prescripcion();
        prescripcion.setCita(cita);
        prescripcion.setFechaPrescripcion(LocalDateTime.now());
        Prescripcion prescripcionGuardada = prescripcionRepository.save(prescripcion);

        // Se procesan las líneas de detalle asociando medicamentos y planes de tratamiento
        List<DetallePrescripcion> detalles = request.getDetalles().stream().map(dto -> {
            Medicamento medicamento = medicamentoRepository.findById(dto.getIdMedicamento())
                    .orElseThrow(() -> new RuntimeException("Medicamento no encontrado: " + dto.getIdMedicamento()));

            DetallePrescripcion detalle = new DetallePrescripcion();
            detalle.setPrescripcion(prescripcionGuardada);
            detalle.setMedicamento(medicamento);
            detalle.setDosisPrescripcion(dto.getDosis());
            detalle.setFrecuenciaPrescripcion(dto.getFrecuencia());
            detalle.setDuracionPrescripcion(dto.getDuracion());
            detalle.setIndicacionesPrescripcion(dto.getIndicaciones());

            // Si el medicamento responde a un plan de tratamiento particular, se preserva la relación foránea
            if (dto.getIdPlanTratamiento() != null) {
                PlanTratamiento plan = planTratamientoRepository.findById(dto.getIdPlanTratamiento())
                        .orElseThrow(() -> new RuntimeException("Plan de tratamiento no encontrado: " + dto.getIdPlanTratamiento()));
                detalle.setPlanTratamiento(plan);
            }

            return detalle;
        }).collect(Collectors.toList());

        detalleRepository.saveAll(detalles);

        return mapearPrescripcion(prescripcionGuardada, detalles);
    }

    /**
     * Consulta la prescripción médica generada para una cita específica.
     *
     * <b>Propósito:</b>
     * Permitir la visualización e impresión de la receta médica en el frontend al revisar el historial clínico.
     *
     * <b>Trazabilidad:</b>
     * - Invocado por: GET /api/consulta/prescripcion/cita/{idCita}.
     *
     * @param idCita Identificador de la cita médica.
     * @return DTO {@link PrescripcionResponseDTO} o null si la cita no tuvo medicamentos prescritos.
     */
    public PrescripcionResponseDTO obtenerPrescripcionPorCita(Integer idCita) {
        return prescripcionRepository.findByCita_IdCitas(idCita)
                .map(p -> {
                    List<DetallePrescripcion> detalles = detalleRepository
                            .findByPrescripcion_IdPrescripcion(p.getIdPrescripcion());
                    return mapearPrescripcion(p, detalles);
                })
                .orElse(null);
    }

    /**
     * Recupera el catálogo completo de tratamientos clínicos disponibles.
     *
     * <b>Propósito:</b>
     * Alimentar los selectores de procedimientos en los formularios de odontograma y presupuestos.
     *
     * <b>Trazabilidad:</b>
     * - Invocado por: GET /api/consulta/tratamientos.
     *
     * @return Lista de {@link TratamientoResponseDTO}.
     */
    public List<TratamientoResponseDTO> listarTratamientos() {
        return tratamientoRepository.findAll().stream()
                .map(t -> new TratamientoResponseDTO(
                        t.getIdTratamiento(),
                        t.getNombreTratamiento(),
                        t.getDescripcionTratamiento(),
                        t.getCostoTratamiento()))
                .collect(Collectors.toList());
    }

    /**
     * Da de alta un nuevo procedimiento odontológico en el catálogo general.
     *
     * <b>Propósito:</b>
     * Expandir los servicios que la clínica ofrece con sus respectivos aranceles.
     *
     * <b>Trazabilidad:</b>
     * - Invocado por: POST /api/consulta/tratamientos.
     *
     * @param request DTO {@link TratamientoRequestDTO} con nombre, descripción y costo monetario.
     * @return DTO {@link TratamientoResponseDTO} del tratamiento dado de alta.
     */
    @Transactional
    public TratamientoResponseDTO crearTratamiento(TratamientoRequestDTO request) {
        Tratamiento nuevo = new Tratamiento();
        nuevo.setNombreTratamiento(request.getNombreTratamiento());
        nuevo.setDescripcionTratamiento(request.getDescripcionTratamiento());
        nuevo.setCostoTratamiento(request.getCostoTratamiento());

        Tratamiento guardado = tratamientoRepository.save(nuevo);

        return new TratamientoResponseDTO(
                guardado.getIdTratamiento(),
                guardado.getNombreTratamiento(),
                guardado.getDescripcionTratamiento(),
                guardado.getCostoTratamiento());
    }

    /**
     * Recupera el inventario activo de medicamentos para prescripción.
     *
     * <b>Propósito:</b>
     * Cargar las opciones de fármacos, presentaciones y concentraciones disponibles en el recetario.
     *
     * <b>Trazabilidad:</b>
     * - Invocado por: GET /api/consulta/medicamentos.
     *
     * @return Lista de {@link MedicamentoResponseDTO}.
     */
    public List<MedicamentoResponseDTO> listarMedicamentos() {
        return medicamentoRepository.findAll().stream()
                .map(m -> new MedicamentoResponseDTO(
                        m.getIdMedicamento(),
                        m.getNombreMedicamento(),
                        m.getComponenteActivo(),
                        m.getConcentracion(),
                        m.getCostoMedicamento(),
                        m.getCantidadInventario()))
                .collect(Collectors.toList());
    }

    /**
     * Convierte la entidad PlanTratamiento en su DTO de respuesta.
     *
     * @param plan Entidad persistente de plan de tratamiento.
     * @return DTO con datos del tratamiento y costo asociados.
     */
    private PlanTratamientoResponseDTO toResponseDTO(PlanTratamiento plan) {
        return new PlanTratamientoResponseDTO(
                plan.getIdPlanTratamiento(),
                plan.getPiezaDental(),
                plan.getEstadoPlan().name(),
                plan.getTratamiento().getIdTratamiento(),
                plan.getTratamiento().getNombreTratamiento(),
                plan.getTratamiento().getDescripcionTratamiento(),
                plan.getTratamiento().getCostoTratamiento().floatValue());
    }

    /**
     * Transforma una cabecera y líneas de detalle de prescripción en un DTO consolidado.
     *
     * @param p Entidad cabecera de Prescripcion.
     * @param detalles Lista de entidades DetallePrescripcion.
     * @return DTO {@link PrescripcionResponseDTO} listo para serialización.
     */
    private PrescripcionResponseDTO mapearPrescripcion(Prescripcion p, List<DetallePrescripcion> detalles) {
        List<PrescripcionResponseDTO.DetallePrescripcionResponseDTO> detallesDTO = detalles.stream()
                .map(d -> new PrescripcionResponseDTO.DetallePrescripcionResponseDTO(
                        d.getIdDetallePrescripcion(),
                        d.getMedicamento().getIdMedicamento(),
                        d.getMedicamento().getNombreMedicamento(),
                        d.getMedicamento().getComponenteActivo(),
                        d.getMedicamento().getConcentracion(),
                        d.getDosisPrescripcion(),
                        d.getFrecuenciaPrescripcion(),
                        d.getDuracionPrescripcion(),
                        d.getIndicacionesPrescripcion(),
                        // Se previene NullPointerException si el medicamento no está asociado a un plan específico
                        d.getPlanTratamiento() != null
                                ? d.getPlanTratamiento().getIdPlanTratamiento()
                                : null
                ))
                .collect(Collectors.toList());

        return new PrescripcionResponseDTO(
                p.getIdPrescripcion(),
                p.getCita().getIdCitas(),
                p.getFechaPrescripcion(),
                detallesDTO);
    }
}
