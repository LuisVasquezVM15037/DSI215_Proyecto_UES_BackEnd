package com.dentalcare.api.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.dentalcare.api.dtos.Cita.CitaCancelacionDTO;
import com.dentalcare.api.dtos.Cita.CitaRequestDTO;
import com.dentalcare.api.dtos.Cita.CitaResponseDTO;
import com.dentalcare.api.dtos.Cita.DisponibilidadHorarioDTO;
import com.dentalcare.api.dtos.Cita.FranjaHorariaDTO;
import com.dentalcare.api.models.Cita;
import com.dentalcare.api.models.Odontologo;
import com.dentalcare.api.models.Paciente;
import com.dentalcare.api.repositories.CitaRepository;
import com.dentalcare.api.repositories.OdontologoRepository;
import com.dentalcare.api.repositories.PacienteRepository;

@Service
public class CitaService {

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private OdontologoRepository odontologoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    // ====================================================
    // Metodos publicos para la logica de negocio de Citas
    // ====================================================

    public List<CitaResponseDTO> obtenerTodas() {
        List<Cita> citas = citaRepository.findAllByOrderByFechaCitaAscHoraInicioCitaAsc();
        return citas.stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    public CitaResponseDTO crearCita(CitaRequestDTO request) {
        // Validar conflicto de horario (DENTALCARE-60 / PBI-Secret-08)
        validarConflictoHorario(
                request.getIdOdontologo(),
                request.getIdPaciente(),
                request.getFechaCita(),
                request.getHoraInicioCita(),
                request.getHoraFinCita(),
                null
        );

        // Validar que las entidades relacionadas existan
        Odontologo odontologo = odontologoRepository.findById(request.getIdOdontologo())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Error: Odontólogo no encontrado"));
        Paciente paciente = pacienteRepository.findById(request.getIdPaciente())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Error: Paciente no encontrado"));

        // Convertir el Request a Entidad
        Cita nuevaCita = mapearAEntidad(request, odontologo, paciente);

        // Guardar en la base de datos
        Cita citaGuardada = citaRepository.save(nuevaCita);

        // Convertir la Entidad guardada a Response y devolverla
        return mapearAResponse(citaGuardada);
    }

    // ==================================================
    // DISPONIBILIDAD Y CONFLICTOS (SPRINT 4 - FASE 2)
    // ==================================================

    /**
     * Calcula dinámicamente las franjas horarias libres y ocupadas para un odontólogo en una fecha.
     * PBI-Secret-09 (DENTALCARE-62)
     */
    public DisponibilidadHorarioDTO obtenerDisponibilidad(Integer idOdontologo, LocalDate fecha) {
        Odontologo odontologo = odontologoRepository.findById(idOdontologo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Odontólogo no encontrado"));

        String nombreOdontologo = (odontologo.getUsuario() != null)
                ? odontologo.getUsuario().getNombreUsuario() + " " + odontologo.getUsuario().getApellidoUsuario()
                : "Especialista";

        List<Cita> citasDelDia = citaRepository.findCitasActivasPorOdontologoYFecha(idOdontologo, fecha);

        // Bloques regulares de jornada clínica (08:00 AM a 05:00 PM con receso de almuerzo 12:00-13:00)
        int[][] slots = {
                {8, 0, 9, 0},
                {9, 0, 10, 0},
                {10, 0, 11, 0},
                {11, 0, 12, 0},
                {13, 0, 14, 0},
                {14, 0, 15, 0},
                {15, 0, 16, 0},
                {16, 0, 17, 0}
        };

        List<FranjaHorariaDTO> franjas = new ArrayList<>();
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

        for (int[] slot : slots) {
            LocalTime inicioTime = LocalTime.of(slot[0], slot[1]);
            LocalTime finTime = LocalTime.of(slot[2], slot[3]);
            LocalDateTime inicioSlot = LocalDateTime.of(fecha, inicioTime);
            LocalDateTime finSlot = LocalDateTime.of(fecha, finTime);

            boolean ocupado = false;
            String motivo = null;
            Integer citaId = null;

            for (Cita c : citasDelDia) {
                // Intersección de intervalos: inicioSlot < citaFin && finSlot > citaInicio
                if (inicioSlot.isBefore(c.getHoraFinCita()) && finSlot.isAfter(c.getHoraInicioCita())) {
                    ocupado = true;
                    citaId = c.getIdCitas();
                    motivo = "Ocupado (Cita #" + c.getIdCitas() + ")";
                    break;
                }
            }

            franjas.add(new FranjaHorariaDTO(
                    inicioTime.format(timeFmt),
                    finTime.format(timeFmt),
                    inicioSlot.toString(),
                    finSlot.toString(),
                    !ocupado,
                    motivo,
                    citaId
            ));
        }

        return new DisponibilidadHorarioDTO(idOdontologo, nombreOdontologo, fecha, franjas);
    }

    /**
     * Valida si existe conflicto de duplicación o solapamiento horario en tiempo real.
     * PBI-Secret-08 (DENTALCARE-60)
     */
    public void validarConflictoHorario(Integer idOdontologo, Integer idPaciente, LocalDate fecha,
                                        LocalDateTime horaInicio, LocalDateTime horaFin, Integer idCitaExcluir) {
        if (horaInicio == null || horaFin == null) {
            return;
        }

        if (!horaInicio.isBefore(horaFin)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La hora de inicio debe ser anterior a la hora de fin.");
        }

        // 1. Conflicto de horario con el Odontólogo
        List<Cita> conflictosOdont = (idCitaExcluir != null)
                ? citaRepository.findConflictosOdontologoExcluyendo(idOdontologo, fecha, idCitaExcluir, horaInicio, horaFin)
                : citaRepository.findConflictosOdontologo(idOdontologo, fecha, horaInicio, horaFin);

        if (!conflictosOdont.isEmpty()) {
            Cita c = conflictosOdont.get(0);
            String doc = (c.getOdontologo() != null && c.getOdontologo().getUsuario() != null)
                    ? c.getOdontologo().getUsuario().getNombreUsuario() + " " + c.getOdontologo().getUsuario().getApellidoUsuario()
                    : "seleccionado";
            String hIni = c.getHoraInicioCita().toLocalTime().toString();
            String hFin = c.getHoraFinCita().toLocalTime().toString();
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Conflicto detectado: El Dr. " + doc + " ya cuenta con una cita asignada de " + hIni + " a " + hFin + " en la fecha indicada.");
        }

        // 2. Conflicto de horario con el mismo Paciente
        if (idPaciente != null) {
            List<Cita> conflictosPac = (idCitaExcluir != null)
                    ? citaRepository.findConflictosPacienteExcluyendo(idPaciente, fecha, idCitaExcluir, horaInicio, horaFin)
                    : citaRepository.findConflictosPaciente(idPaciente, fecha, horaInicio, horaFin);

            if (!conflictosPac.isEmpty()) {
                Cita c = conflictosPac.get(0);
                String hIni = c.getHoraInicioCita().toLocalTime().toString();
                String hFin = c.getHoraFinCita().toLocalTime().toString();
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Conflicto detectado: El paciente ya tiene otra cita programada de " + hIni + " a " + hFin + " en la fecha indicada.");
            }
        }
    }

    // ==================================================
    // Metodos Privados para mapeo entre Entidades y DTOs
    // ==================================================

    private Cita mapearAEntidad(CitaRequestDTO request, Odontologo odontologo, Paciente paciente) {
        Cita cita = new Cita();
        cita.setOdontologo(odontologo);
        cita.setPaciente(paciente);
        cita.setFechaCita(request.getFechaCita());
        cita.setHoraInicioCita(request.getHoraInicioCita());
        cita.setHoraFinCita(request.getHoraFinCita());

        if (request.getEstadoCita() != null) {
            cita.setEstadoCita(request.getEstadoCita());
        } else {
            cita.setEstadoCita(com.dentalcare.api.models.enums.EstadoCita.PROGRAMADA);
        }
        return cita;
    }

    private CitaResponseDTO mapearAResponse(Cita cita) {
        CitaResponseDTO response = new CitaResponseDTO();
        response.setIdCitas(cita.getIdCitas());
        response.setFechaCita(cita.getFechaCita());
        response.setHoraInicioCita(cita.getHoraInicioCita());
        response.setHoraFinCita(cita.getHoraFinCita());
        response.setEstadoCita(cita.getEstadoCita());
        response.setMotivoCancelacion(cita.getMotivoCancelacion());

        if (cita.getPaciente() != null) {
            response.setIdPaciente(cita.getPaciente().getIdPaciente());
            response.setNombreCompletoPaciente(
                    cita.getPaciente().getNombrePaciente() + " " + cita.getPaciente().getApellidoPaciente());
            response.setNumeroIdentidadPaciente(cita.getPaciente().getNumeroIdentidadPaciente());
        }

        if (cita.getOdontologo() != null) {
            response.setIdOdontologo(cita.getOdontologo().getIdOdontologo());
            response.setEspecialidadOdontologo(cita.getOdontologo().getEspecialidadOdontologo());
        }

        return response;
    }

    public CitaResponseDTO obtenerPorId(Integer id) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Error: Cita no encontrada con el ID: " + id));
        return mapearAResponse(cita);
    }

    public CitaResponseDTO cancelarCita(Integer id, CitaCancelacionDTO cancelacionRequest) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Error: Cita no encontrada con el ID: " + id));

        cita.setEstadoCita(com.dentalcare.api.models.enums.EstadoCita.CANCELADA);
        cita.setMotivoCancelacion(cancelacionRequest.getMotivoCancelacion());

        Cita citaActualizada = citaRepository.save(cita);
        return mapearAResponse(citaActualizada);
    }

    public CitaResponseDTO actualizarCita(Integer id, CitaRequestDTO request) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada"));

        // Validar conflicto de horario excluyendo la cita actual (DENTALCARE-60)
        validarConflictoHorario(
                request.getIdOdontologo(),
                request.getIdPaciente(),
                request.getFechaCita(),
                request.getHoraInicioCita(),
                request.getHoraFinCita(),
                id
        );

        Odontologo odontologo = odontologoRepository.findById(request.getIdOdontologo())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Odontólogo no encontrado"));

        Paciente paciente = pacienteRepository.findById(request.getIdPaciente())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente no encontrado"));

        cita.setOdontologo(odontologo);
        cita.setPaciente(paciente);
        cita.setFechaCita(request.getFechaCita());
        cita.setHoraInicioCita(request.getHoraInicioCita());
        cita.setHoraFinCita(request.getHoraFinCita());
        cita.setEstadoCita(request.getEstadoCita());

        Cita actualizada = citaRepository.save(cita);
        return mapearAResponse(actualizada);
    }

    @Transactional
    public CitaResponseDTO actualizarEstado(Integer id, com.dentalcare.api.models.enums.EstadoCita nuevoEstado) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Error: Cita no encontrada con el ID: " + id));

        cita.setEstadoCita(nuevoEstado);
        Cita citaActualizada = citaRepository.save(cita);
        return mapearAResponse(citaActualizada);
    }
}
