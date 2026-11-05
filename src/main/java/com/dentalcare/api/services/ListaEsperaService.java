package com.dentalcare.api.services;

import com.dentalcare.api.dtos.Cita.ListaEsperaRequestDto;
import com.dentalcare.api.dtos.Cita.ListaEsperaResponseDto;
import com.dentalcare.api.models.Cita;
import com.dentalcare.api.models.ListaEspera;
import com.dentalcare.api.models.Odontologo;
import com.dentalcare.api.models.Paciente;
import com.dentalcare.api.models.enums.EstadoListaEspera;
import com.dentalcare.api.models.enums.PrioridadListaEspera;
import com.dentalcare.api.models.enums.TipoNotificacion;
import com.dentalcare.api.repositories.CitaRepository;
import com.dentalcare.api.repositories.ListaEsperaRepository;
import com.dentalcare.api.repositories.OdontologoRepository;
import com.dentalcare.api.repositories.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ListaEsperaService {

    @Autowired
    private ListaEsperaRepository listaEsperaRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private OdontologoRepository odontologoRepository;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private NotificacionService notificacionService;

    public List<ListaEsperaResponseDto> listar(EstadoListaEspera estado) {
        List<ListaEspera> lista;
        if (estado != null) {
            lista = listaEsperaRepository.findByEstadoOrderByFechaSolicitudAsc(estado);
        } else {
            lista = listaEsperaRepository.findAllByOrderByPrioridadAscFechaSolicitudAsc();
        }

        return lista.stream().map(this::mapearADto).collect(Collectors.toList());
    }

    public ListaEsperaResponseDto obtenerPorId(Integer id) {
        ListaEspera le = listaEsperaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro de lista de espera no encontrado con id: " + id));
        return mapearADto(le);
    }

    @Transactional
    public ListaEsperaResponseDto agregar(ListaEsperaRequestDto request) {
        if (request.getPacienteId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El ID de paciente es obligatorio");
        }

        Paciente paciente = pacienteRepository.findById(request.getPacienteId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente no encontrado con id: " + request.getPacienteId()));

        Odontologo odontologo = null;
        if (request.getOdontologoId() != null) {
            odontologo = odontologoRepository.findById(request.getOdontologoId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Odontólogo no encontrado con id: " + request.getOdontologoId()));
        }

        ListaEspera le = new ListaEspera();
        le.setPaciente(paciente);
        le.setOdontologo(odontologo);
        le.setRangoHorarioPreferido(request.getRangoHorarioPreferido() != null ? request.getRangoHorarioPreferido() : "Indistinto");
        le.setDiasPreferidos(request.getDiasPreferidos() != null ? request.getDiasPreferidos() : "Cualquier día");
        le.setMotivoConsulta(request.getMotivoConsulta() != null ? request.getMotivoConsulta() : "Consulta General");
        le.setPrioridad(request.getPrioridad() != null ? request.getPrioridad() : PrioridadListaEspera.MEDIA);
        le.setEstado(EstadoListaEspera.EN_ESPERA);
        le.setFechaSolicitud(LocalDateTime.now());
        le.setNotas(request.getNotas());

        ListaEspera guardada = listaEsperaRepository.save(le);
        return mapearADto(guardada);
    }

    @Transactional
    public void descartar(Integer id) {
        ListaEspera le = listaEsperaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro de lista de espera no encontrado con id: " + id));
        le.setEstado(EstadoListaEspera.DESCARTADO);
        listaEsperaRepository.save(le);
    }

    @Transactional
    public void eliminar(Integer id) {
        if (!listaEsperaRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro no encontrado");
        }
        listaEsperaRepository.deleteById(id);
    }

    @Transactional
    public ListaEsperaResponseDto asignarCita(Integer listaEsperaId, Integer citaId) {
        ListaEspera le = listaEsperaRepository.findById(listaEsperaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro de lista de espera no encontrado"));

        Cita cita = citaRepository.findById(citaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada con id: " + citaId));

        le.setEstado(EstadoListaEspera.AGENDADO);
        le.setCitaAsignada(cita);
        ListaEspera actualizada = listaEsperaRepository.save(le);
        return mapearADto(actualizada);
    }

    /**
     * Algoritmo reactivo de evaluación de cupos liberados.
     * Invocado cuando una cita es cancelada en CitaService.
     * DENTALCARE-92 (PBI-Secret-14)
     */
    @Transactional
    public void evaluarCupoLiberadoPorCancelacion(Cita citaCancelada) {
        if (citaCancelada == null) {
            return;
        }

        Integer idOdontologo = (citaCancelada.getOdontologo() != null)
                ? citaCancelada.getOdontologo().getIdOdontologo()
                : null;

        List<ListaEspera> candidatos = listaEsperaRepository.findCandidatosParaCupo(idOdontologo, EstadoListaEspera.EN_ESPERA);

        if (!candidatos.isEmpty()) {
            ListaEspera primerCandidato = candidatos.get(0);
            primerCandidato.setEstado(EstadoListaEspera.NOTIFICADO);
            listaEsperaRepository.save(primerCandidato);

            String hora = (citaCancelada.getHoraInicioCita() != null)
                    ? citaCancelada.getHoraInicioCita().format(DateTimeFormatter.ofPattern("hh:mm a"))
                    : "hora convenida";
            String fecha = (citaCancelada.getFechaCita() != null)
                    ? citaCancelada.getFechaCita().toString()
                    : "hoy";

            String doctor = (citaCancelada.getOdontologo() != null && citaCancelada.getOdontologo().getUsuario() != null)
                    ? "Dr(a). " + citaCancelada.getOdontologo().getUsuario().getNombreUsuario() + " " + citaCancelada.getOdontologo().getUsuario().getApellidoUsuario()
                    : "odontólogo";

            String pacienteEspera = (primerCandidato.getPaciente() != null)
                    ? primerCandidato.getPaciente().getNombrePaciente() + " " + primerCandidato.getPaciente().getApellidoPaciente()
                    : "Paciente en espera";

            String tel = (primerCandidato.getPaciente() != null && primerCandidato.getPaciente().getTelefonoPaciente() != null)
                    ? primerCandidato.getPaciente().getTelefonoPaciente()
                    : "Sin teléfono registrado";

            String titulo = "¡Espacio Liberado en Agenda!";
            String mensaje = String.format(
                    "Se canceló una cita para el %s a las %s con %s. Primer paciente elegible en Lista de Espera: %s (Tel: %s).",
                    fecha, hora, doctor, pacienteEspera, tel
            );

            notificacionService.crearNotificacion(
                    null,
                    "RECEPCIONISTA",
                    titulo,
                    mensaje,
                    TipoNotificacion.LISTA_ESPERA,
                    citaCancelada.getIdCitas(),
                    "/agenda?tab=espera&pacienteId=" + (primerCandidato.getPaciente() != null ? primerCandidato.getPaciente().getIdPaciente() : "")
            );
        }
    }

    private ListaEsperaResponseDto mapearADto(ListaEspera le) {
        String nombrePac = (le.getPaciente() != null)
                ? le.getPaciente().getNombrePaciente() + " " + le.getPaciente().getApellidoPaciente()
                : "Sin paciente";
        String telPac = (le.getPaciente() != null) ? le.getPaciente().getTelefonoPaciente() : "";
        String duiPac = (le.getPaciente() != null) ? le.getPaciente().getNumeroIdentidadPaciente() : "";

        String nombreOdont = null;
        if (le.getOdontologo() != null && le.getOdontologo().getUsuario() != null) {
            nombreOdont = le.getOdontologo().getUsuario().getNombreUsuario() + " " + le.getOdontologo().getUsuario().getApellidoUsuario();
        }

        return ListaEsperaResponseDto.builder()
                .id(le.getId())
                .pacienteId(le.getPaciente() != null ? le.getPaciente().getIdPaciente() : null)
                .nombrePaciente(nombrePac)
                .telefonoPaciente(telPac)
                .numeroIdentidadPaciente(duiPac)
                .odontologoId(le.getOdontologo() != null ? le.getOdontologo().getIdOdontologo() : null)
                .nombreOdontologo(nombreOdont)
                .rangoHorarioPreferido(le.getRangoHorarioPreferido())
                .diasPreferidos(le.getDiasPreferidos())
                .motivoConsulta(le.getMotivoConsulta())
                .prioridad(le.getPrioridad())
                .estado(le.getEstado())
                .fechaSolicitud(le.getFechaSolicitud())
                .citaAsignadaId(le.getCitaAsignada() != null ? le.getCitaAsignada().getIdCitas() : null)
                .notas(le.getNotas())
                .build();
    }
}
