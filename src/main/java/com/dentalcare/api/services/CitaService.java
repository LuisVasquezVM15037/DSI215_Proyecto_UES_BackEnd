package com.dentalcare.api.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.dentalcare.api.dtos.Cita.CitaResponseDTO;
import com.dentalcare.api.dtos.Cita.CitaCancelacionDTO;
import com.dentalcare.api.dtos.Cita.CitaRequestDTO;
import com.dentalcare.api.models.Cita;
import com.dentalcare.api.models.Odontologo;
import com.dentalcare.api.models.Paciente;
import com.dentalcare.api.models.enums.EstadoCita;
import com.dentalcare.api.repositories.CitaRepository;
import com.dentalcare.api.repositories.OdontologoRepository;
import com.dentalcare.api.repositories.PacienteRepository;

/**
 * CitaService
 *
 * <b>Propósito:</b>
 * Gestiona el ciclo de vida y las reglas de negocio de las citas odontológicas.
 * Coordina la verificación de disponibilidad y existencia de profesionales y pacientes,
 * el registro cronológico, las modificaciones de agenda, la cancelación justificada y
 * las transiciones de estado de cada consulta.
 *
 * <b>Ubicación y Rol en la Arquitectura:</b>
 * - Capa: Lógica de Negocio / Capa de Servicios (@Service).
 * - Rol: Orquestador del dominio clínico que transforma peticiones de agenda en entidades
 *   persistibles y proyecta las respuestas en DTOs planos para consumo del frontend.
 *
 * <b>Trazabilidad (Referencias):</b>
 * - Invocado por: {@link com.dentalcare.api.controllers.CitaController}.
 * - Consume / Dependencias: {@link CitaRepository}, {@link OdontologoRepository}, {@link PacienteRepository}.
 */
@Service
public class CitaService {

    private final CitaRepository citaRepository;
    private final OdontologoRepository odontologoRepository;
    private final PacienteRepository pacienteRepository;

    /**
     * Constructor para inyección explícita de dependencias por inversión de control.
     *
     * @param citaRepository Repositorio para operaciones CRUD sobre la tabla de citas.
     * @param odontologoRepository Repositorio para validación de existencia del odontólogo.
     * @param pacienteRepository Repositorio para validación de existencia del paciente.
     */
    public CitaService(CitaRepository citaRepository,
                       OdontologoRepository odontologoRepository,
                       PacienteRepository pacienteRepository) {
        this.citaRepository = citaRepository;
        this.odontologoRepository = odontologoRepository;
        this.pacienteRepository = pacienteRepository;
    }

    /**
     * Recupera el listado completo de citas ordenadas cronológicamente.
     *
     * <b>Propósito:</b>
     * Proveer a la vista del calendario o agenda del frontend la secuencia ordenada
     * de citas por fecha y hora de inicio de forma ascendente.
     *
     * <b>Trazabilidad:</b>
     * - Invocado por: GET /api/citas.
     *
     * @return Lista de {@link CitaResponseDTO} con los datos aplanados de cada cita.
     */
    public List<CitaResponseDTO> obtenerTodas() {
        List<Cita> citas = citaRepository.findAllByOrderByFechaCitaAscHoraInicioCitaAsc();
        return citas.stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    /**
     * Registra una nueva cita clínica en el sistema.
     *
     * <b>Propósito:</b>
     * Validar la existencia previa del odontólogo y del paciente involucrados antes de
     * construir y persistir el registro de la cita.
     *
     * <b>Trazabilidad:</b>
     * - Invocado por: POST /api/citas.
     *
     * @param request DTO {@link CitaRequestDTO} con identificadores y horarios requeridos.
     * @return DTO {@link CitaResponseDTO} que representa la cita persistida en base de datos.
     * @throws RuntimeException Si el odontólogo o el paciente especificados no existen.
     */
    public CitaResponseDTO crearCita(CitaRequestDTO request) {
        // Se valida existencia previa de entidades foráneas para evitar violaciones de clave foránea a nivel de BD
        Odontologo odontologo = odontologoRepository.findById(request.getIdOdontologo())
                .orElseThrow(() -> new RuntimeException("Error: Odontólogo no encontrado"));

        Paciente paciente = pacienteRepository.findById(request.getIdPaciente())
                .orElseThrow(() -> new RuntimeException("Error: Paciente no encontrado"));

        Cita nuevaCita = mapearAEntidad(request, odontologo, paciente);
        Cita citaGuardada = citaRepository.save(nuevaCita);

        return mapearAResponse(citaGuardada);
    }

    /**
     * Consulta una cita puntual a partir de su identificador primario.
     *
     * <b>Propósito:</b>
     * Obtener los detalles consolidados de una cita para operaciones de consulta detallada o edición.
     *
     * <b>Trazabilidad:</b>
     * - Invocado por: GET /api/citas/{id}.
     *
     * @param id Clave primaria de la cita buscada.
     * @return DTO {@link CitaResponseDTO} con la información de la cita.
     * @throws RuntimeException Si la cita no existe en la base de datos.
     */
    public CitaResponseDTO obtenerPorId(Integer id) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Cita no encontrada con el ID: " + id));

        return mapearAResponse(cita);
    }

    /**
     * Realiza la cancelación justificada de una cita agendada.
     *
     * <b>Propósito:</b>
     * Cambiar el estado a CANCELADA y registrar obligatoriamente el motivo de cancelación
     * para mantener la trazabilidad administrativa.
     *
     * <b>Trazabilidad:</b>
     * - Invocado por: PUT /api/citas/{id}/cancelar.
     *
     * @param id Clave primaria de la cita a cancelar.
     * @param cancelacionRequest DTO con el texto explicativo de la cancelación.
     * @return DTO {@link CitaResponseDTO} con el estado actualizado.
     * @throws IllegalArgumentException Si el motivo de cancelación está vacío o es nulo.
     * @throws RuntimeException Si la cita no es encontrada.
     */
    public CitaResponseDTO cancelarCita(Integer id, CitaCancelacionDTO cancelacionRequest) {
        // Validación defensiva en capa de servicio para asegurar integridad aun si se omitiese la validación en controlador
        if (cancelacionRequest == null || cancelacionRequest.getMotivoCancelacion() == null || cancelacionRequest.getMotivoCancelacion().trim().isEmpty()) {
            throw new IllegalArgumentException("El motivo de cancelación es obligatorio.");
        }

        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Cita no encontrada con el ID: " + id));

        cita.setEstadoCita(EstadoCita.CANCELADA);
        cita.setMotivoCancelacion(cancelacionRequest.getMotivoCancelacion().trim());

        Cita citaActualizada = citaRepository.save(cita);
        return mapearAResponse(citaActualizada);
    }

    /**
     * Actualiza integralmente los datos de programación de una cita existente.
     *
     * <b>Propósito:</b>
     * Modificar profesional, paciente, fechas y horarios de una cita previamente registrada.
     *
     * <b>Trazabilidad:</b>
     * - Invocado por: PUT /api/citas/{id}.
     *
     * @param id Clave primaria de la cita a actualizar.
     * @param request DTO con los nuevos parámetros de agendamiento.
     * @return DTO {@link CitaResponseDTO} con la información modificada.
     * @throws RuntimeException Si la cita, odontólogo o paciente no existen.
     */
    public CitaResponseDTO actualizarCita(Integer id, CitaRequestDTO request) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        Odontologo odontologo = odontologoRepository.findById(request.getIdOdontologo())
                .orElseThrow(() -> new RuntimeException("Odontólogo no encontrado"));

        Paciente paciente = pacienteRepository.findById(request.getIdPaciente())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        cita.setOdontologo(odontologo);
        cita.setPaciente(paciente);
        cita.setFechaCita(request.getFechaCita());
        cita.setHoraInicioCita(request.getHoraInicioCita());
        cita.setHoraFinCita(request.getHoraFinCita());
        cita.setEstadoCita(request.getEstadoCita());

        Cita actualizada = citaRepository.save(cita);
        return mapearAResponse(actualizada);
    }

    /**
     * Modifica de manera atómica el estado de una cita médica.
     *
     * <b>Propósito:</b>
     * Permitir transiciones rápidas de estado (ej. de PROGRAMADA a EN_PROGRESO o FINALIZADA)
     * sin requerir el reenvío completo de los datos de agenda.
     *
     * <b>Trazabilidad:</b>
     * - Invocado por: PUT /api/citas/{id}/estado.
     *
     * @param id Clave primaria de la cita.
     * @param nuevoEstado Nuevo valor del enum {@link EstadoCita}.
     * @return DTO {@link CitaResponseDTO} reflejando el cambio de estado.
     * @throws RuntimeException Si la cita no existe.
     */
    public CitaResponseDTO actualizarEstado(Integer id, EstadoCita nuevoEstado) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Cita no encontrada con el ID: " + id));

        cita.setEstadoCita(nuevoEstado);

        Cita citaActualizada = citaRepository.save(cita);
        return mapearAResponse(citaActualizada);
    }

    /**
     * Mapea un DTO de entrada hacia una entidad persistente Cita.
     *
     * <b>Propósito:</b>
     * Desacoplar el contrato de transferencia HTTP del modelo relacional JPA.
     *
     * @param request Datos de entrada recibidos del cliente.
     * @param odontologo Entidad del odontólogo previamente verificada.
     * @param paciente Entidad del paciente previamente verificada.
     * @return Instancia lista para inserción de {@link Cita}.
     */
    private Cita mapearAEntidad(CitaRequestDTO request, Odontologo odontologo, Paciente paciente) {
        Cita cita = new Cita();
        cita.setOdontologo(odontologo);
        cita.setPaciente(paciente);
        cita.setFechaCita(request.getFechaCita());
        cita.setHoraInicioCita(request.getHoraInicioCita());
        cita.setHoraFinCita(request.getHoraFinCita());

        // Se asigna PROGRAMADA si no se especifica estado para garantizar consistencia en la máquina de estados
        if (request.getEstadoCita() != null) {
            cita.setEstadoCita(request.getEstadoCita());
        } else {
            cita.setEstadoCita(EstadoCita.PROGRAMADA);
        }
        return cita;
    }

    /**
     * Proyecta una entidad Cita hacia un DTO plano de respuesta.
     *
     * <b>Propósito:</b>
     * Aplanar las relaciones relacionales para evitar problemas de referencias circulares
     * durante la serialización JSON de Jackson y no exponer entidades internas del dominio.
     *
     * @param cita Entidad de base de datos cargada.
     * @return DTO {@link CitaResponseDTO} formateado para consumo de la capa de presentación.
     */
    private CitaResponseDTO mapearAResponse(Cita cita) {
        CitaResponseDTO response = new CitaResponseDTO();

        response.setIdCitas(cita.getIdCitas());
        response.setFechaCita(cita.getFechaCita());
        response.setHoraInicioCita(cita.getHoraInicioCita());
        response.setHoraFinCita(cita.getHoraFinCita());
        response.setEstadoCita(cita.getEstadoCita());
        response.setMotivoCancelacion(cita.getMotivoCancelacion());

        // Aplanamiento intencional de datos del paciente para consumo directo en tablas del cliente web
        if (cita.getPaciente() != null) {
            response.setIdPaciente(cita.getPaciente().getIdPaciente());
            response.setNombreCompletoPaciente(
                    cita.getPaciente().getNombrePaciente() + " " + cita.getPaciente().getApellidoPaciente());
            response.setNumeroIdentidadPaciente(cita.getPaciente().getNumeroIdentidadPaciente());
        }

        // Aplanamiento de datos del odontólogo para evitar exponer credenciales o relaciones anidadas
        if (cita.getOdontologo() != null) {
            response.setIdOdontologo(cita.getOdontologo().getIdOdontologo());
            response.setEspecialidadOdontologo(cita.getOdontologo().getEspecialidadOdontologo());
        }

        return response;
    }
}