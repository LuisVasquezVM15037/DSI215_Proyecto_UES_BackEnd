package com.dentalcare.api.services;

import com.dentalcare.api.dtos.Cita.CitaProximaDto;
import com.dentalcare.api.dtos.Cita.CitaRecordatorioDto;
import com.dentalcare.api.dtos.Cita.ConfiguracionAlertaCitaDto;
import com.dentalcare.api.models.Cita;
import com.dentalcare.api.models.ConfiguracionAlertaCita;
import com.dentalcare.api.models.Usuario;
import com.dentalcare.api.models.enums.TipoNotificacion;
import com.dentalcare.api.repositories.CitaRepository;
import com.dentalcare.api.repositories.ConfiguracionAlertaCitaRepository;
import com.dentalcare.api.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AlertaCitaService {

    @Autowired
    private ConfiguracionAlertaCitaRepository configuracionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private NotificacionService notificacionService;

    public ConfiguracionAlertaCitaDto obtenerConfiguracion(Integer usuarioId) {
        if (usuarioId == null) {
            return defaultConfig();
        }

        return configuracionRepository.findByUsuario_IdUsuario(usuarioId)
                .map(this::mapearADto)
                .orElse(defaultConfig());
    }

    private ConfiguracionAlertaCitaDto defaultConfig() {
        return ConfiguracionAlertaCitaDto.builder()
                .alertasActivas(true)
                .anticipacionTipo("DIA_ANTERIOR")
                .tiempoAnticipacionMinutos(1440) // 1 día antes
                .sonidoActivo(true)
                .canalWhatsApp(true)
                .canalCorreo(true)
                .plantillaMensaje("Hola *{paciente}*, le saludamos de la clínica DentalCare. Le recordamos su cita para el día de mañana *{fecha}* a las *{hora}* con el Dr(a). *{doctor}*. Por favor responda a este mensaje para confirmar o reagendar su consulta. ¡Le esperamos!")
                .build();
    }

    private ConfiguracionAlertaCitaDto mapearADto(ConfiguracionAlertaCita c) {
        return ConfiguracionAlertaCitaDto.builder()
                .alertasActivas(c.getAlertasActivas())
                .anticipacionTipo(c.getAnticipacionTipo() != null ? c.getAnticipacionTipo() : "DIA_ANTERIOR")
                .tiempoAnticipacionMinutos(c.getTiempoAnticipacionMinutos())
                .sonidoActivo(c.getSonidoActivo())
                .canalWhatsApp(c.getCanalWhatsApp() != null ? c.getCanalWhatsApp() : true)
                .canalCorreo(c.getCanalCorreo() != null ? c.getCanalCorreo() : true)
                .plantillaMensaje(c.getPlantillaMensaje() != null ? c.getPlantillaMensaje() : defaultConfig().getPlantillaMensaje())
                .build();
    }

    @Transactional
    public ConfiguracionAlertaCitaDto guardarConfiguracion(Integer usuarioId, ConfiguracionAlertaCitaDto dto) {
        if (usuarioId == null) {
            return dto;
        }

        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        if (usuario == null) {
            return dto;
        }

        ConfiguracionAlertaCita config = configuracionRepository.findByUsuario_IdUsuario(usuarioId)
                .orElseGet(() -> {
                    ConfiguracionAlertaCita nueva = new ConfiguracionAlertaCita();
                    nueva.setUsuario(usuario);
                    return nueva;
                });

        config.setAlertasActivas(dto.getAlertasActivas() != null ? dto.getAlertasActivas() : true);
        config.setAnticipacionTipo(dto.getAnticipacionTipo() != null ? dto.getAnticipacionTipo() : "DIA_ANTERIOR");
        config.setTiempoAnticipacionMinutos(
                dto.getTiempoAnticipacionMinutos() != null && dto.getTiempoAnticipacionMinutos() > 0
                        ? dto.getTiempoAnticipacionMinutos()
                        : 1440
        );
        config.setSonidoActivo(dto.getSonidoActivo() != null ? dto.getSonidoActivo() : true);
        config.setCanalWhatsApp(dto.getCanalWhatsApp() != null ? dto.getCanalWhatsApp() : true);
        config.setCanalCorreo(dto.getCanalCorreo() != null ? dto.getCanalCorreo() : true);
        if (dto.getPlantillaMensaje() != null) {
            config.setPlantillaMensaje(dto.getPlantillaMensaje());
        }

        ConfiguracionAlertaCita guardada = configuracionRepository.save(config);
        return mapearADto(guardada);
    }

    /**
     * Obtiene el listado de citas para enviar recordatorios por WhatsApp / Correo.
     * Por regla de negocio clínica, se enfoca en las citas del DÍA DE MAÑANA (o fecha dada).
     */
    public List<CitaRecordatorioDto> obtenerCitasParaRecordatorio(LocalDate fechaConsulta) {
        LocalDate fecha = (fechaConsulta != null) ? fechaConsulta : LocalDate.now().plusDays(1);
        List<Cita> citas = citaRepository.findCitasActivasPorFecha(fecha);

        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", new Locale("es", "ES"));
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("hh:mm a");

        return citas.stream().map(c -> {
            String nombrePac = (c.getPaciente() != null)
                    ? c.getPaciente().getNombrePaciente() + " " + c.getPaciente().getApellidoPaciente()
                    : "Paciente";
            String telPac = (c.getPaciente() != null && c.getPaciente().getTelefonoPaciente() != null)
                    ? c.getPaciente().getTelefonoPaciente()
                    : "";
            String emailPac = (c.getPaciente() != null && c.getPaciente().getEmailPaciente() != null)
                    ? c.getPaciente().getEmailPaciente()
                    : "paciente@dentalcare.com";

            String nombreOdont = (c.getOdontologo() != null && c.getOdontologo().getUsuario() != null)
                    ? c.getOdontologo().getUsuario().getNombreUsuario() + " " + c.getOdontologo().getUsuario().getApellidoUsuario()
                    : "Doctor Asignado";
            String especialidad = (c.getOdontologo() != null) ? c.getOdontologo().getEspecialidadOdontologo() : "Odontología General";

            String fechaStr = c.getFechaCita() != null ? c.getFechaCita().format(dateFmt) : fecha.toString();
            String horaStr = c.getHoraInicioCita() != null ? c.getHoraInicioCita().format(timeFmt) : "hora agendada";

            // Plantilla de WhatsApp
            String textoWhatsapp = String.format(
                    "Hola *%s*, le saludamos cordialmente de la Clínica Odontológica *DentalCare*. Le recordamos su cita programada para el día *%s* a las *%s* con el Dr(a). *%s* (%s). Por favor responda a este mensaje con \"CONFIRMAR\" o avísenos si necesita reprogramar. ¡Le esperamos!",
                    nombrePac, fechaStr, horaStr, nombreOdont, especialidad
            );

            // Generar enlace directo a WhatsApp (limpiando guiones o espacios)
            String telLimpio = telPac.replaceAll("[^0-9]", "");
            if (telLimpio.length() == 8) {
                telLimpio = "503" + telLimpio; // Código de país El Salvador
            }
            String linkWa = "";
            try {
                linkWa = "https://api.whatsapp.com/send?phone=" + telLimpio + "&text=" + URLEncoder.encode(textoWhatsapp, StandardCharsets.UTF_8);
            } catch (Exception ignored) {}

            // Asunto y cuerpo de Correo Electrónico
            String asuntoCorreo = "Recordatorio de Cita Odontológica — DentalCare ERP (" + fechaStr + ")";
            String cuerpoCorreo = String.format(
                    "Estimado/a %s,\n\nEsperamos que se encuentre bien. Le escribimos de la Clínica Odontológica DentalCare para recordarle su cita programada:\n\n• Fecha: %s\n• Hora: %s\n• Especialista: Dr(a). %s\n• Especialidad: %s\n• Ubicación: Clínica DentalCare, San Salvador\n\nPor favor confirme su asistencia respondiendo a este correo o comunicándose con nuestra recepción. Si requiere reprogramar, le solicitamos avisar con al menos 24 horas de anticipación.\n\nAtentamente,\nEquipo de Recepción y Atención Clínica\nDentalCare ERP",
                    nombrePac, fechaStr, horaStr, nombreOdont, especialidad
            );

            return CitaRecordatorioDto.builder()
                    .idCitas(c.getIdCitas())
                    .idPaciente(c.getPaciente() != null ? c.getPaciente().getIdPaciente() : null)
                    .nombrePaciente(nombrePac)
                    .telefonoPaciente(telPac)
                    .emailPaciente(emailPac)
                    .idOdontologo(c.getOdontologo() != null ? c.getOdontologo().getIdOdontologo() : null)
                    .nombreOdontologo(nombreOdont)
                    .especialidad(especialidad)
                    .fechaCita(c.getFechaCita())
                    .horaInicioCita(c.getHoraInicioCita())
                    .horaFinCita(c.getHoraFinCita())
                    .estadoCita(c.getEstadoCita() != null ? c.getEstadoCita().name() : "PROGRAMADA")
                    .mensajeWhatsApp(textoWhatsapp)
                    .linkWhatsApp(linkWa)
                    .asuntoCorreo(asuntoCorreo)
                    .cuerpoCorreo(cuerpoCorreo)
                    .recordatorioEnviado(false)
                    .canalEnviado(null)
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * Registra o simula el envío del recordatorio para una cita y deja trazabilidad en el Centro de Notificaciones.
     */
    @Transactional
    public void registrarEnvioRecordatorio(Integer idCita, String canal) {
        Cita cita = citaRepository.findById(idCita)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada con id: " + idCita));

        String nombrePac = (cita.getPaciente() != null)
                ? cita.getPaciente().getNombrePaciente() + " " + cita.getPaciente().getApellidoPaciente()
                : "Paciente";
        String canalStr = (canal != null) ? canal.toUpperCase() : "WHATSAPP";

        String titulo = "Recordatorio Enviado (" + canalStr + ")";
        String mensaje = String.format("Se envió recordatorio exitosamente por %s al paciente %s para su cita del %s a las %s.",
                canalStr,
                nombrePac,
                cita.getFechaCita() != null ? cita.getFechaCita().toString() : "fecha indicada",
                cita.getHoraInicioCita() != null ? cita.getHoraInicioCita().toLocalTime().toString() : ""
        );

        notificacionService.crearNotificacion(
                null,
                "RECEPCIONISTA",
                titulo,
                mensaje,
                TipoNotificacion.CITA_PROXIMA,
                cita.getIdCitas(),
                "/agenda"
        );
    }

    /**
     * Registra o simula el envío masivo de recordatorios para todas las citas indicadas (o de una fecha)
     * y genera una notificación consolidada para el personal de Recepción.
     */
    @Transactional
    public Map<String, Object> registrarEnvioRecordatorioMasivo(LocalDate fechaConsulta, String canal, List<Integer> idsCitas) {
        LocalDate fecha = (fechaConsulta != null) ? fechaConsulta : LocalDate.now().plusDays(1);
        List<Cita> citas;
        if (idsCitas != null && !idsCitas.isEmpty()) {
            citas = citaRepository.findAllById(idsCitas);
        } else {
            citas = citaRepository.findCitasActivasPorFecha(fecha);
        }

        String canalStr = (canal != null) ? canal.toUpperCase() : "CORREO";
        int total = 0;
        for (Cita c : citas) {
            if (c.getEstadoCita() != null && c.getEstadoCita().name().equals("CANCELADA")) {
                continue;
            }
            total++;
        }

        String titulo = "Envío Masivo de Recordatorios (" + canalStr + ")";
        String mensaje = String.format("Se completó el envío de %d recordatorios de citas por %s para la fecha %s.",
                total, canalStr, fecha.toString());

        notificacionService.crearNotificacion(
                null,
                "RECEPCIONISTA",
                titulo,
                mensaje,
                TipoNotificacion.CITA_PROXIMA,
                null,
                "/agenda"
        );

        Map<String, Object> resp = new HashMap<>();
        resp.put("status", "success");
        resp.put("totalProcesados", total);
        resp.put("canal", canalStr);
        resp.put("fecha", fecha.toString());
        resp.put("mensaje", mensaje);
        return resp;
    }

    public List<CitaProximaDto> obtenerCitasProximas(Integer usuarioId) {
        ConfiguracionAlertaCitaDto config = obtenerConfiguracion(usuarioId);
        if (!Boolean.TRUE.equals(config.getAlertasActivas())) {
            return new ArrayList<>();
        }

        // Si el modo configurado es DIA_ANTERIOR, busca citas de mañana
        if ("DIA_ANTERIOR".equalsIgnoreCase(config.getAnticipacionTipo())) {
            LocalDate maniana = LocalDate.now().plusDays(1);
            List<Cita> citasManiana = citaRepository.findCitasActivasPorFecha(maniana);
            return citasManiana.stream().map(c -> {
                String nombrePac = (c.getPaciente() != null)
                        ? c.getPaciente().getNombrePaciente() + " " + c.getPaciente().getApellidoPaciente()
                        : "Paciente";
                String telPac = (c.getPaciente() != null) ? c.getPaciente().getTelefonoPaciente() : "";
                String nombreOdont = (c.getOdontologo() != null && c.getOdontologo().getUsuario() != null)
                        ? c.getOdontologo().getUsuario().getNombreUsuario() + " " + c.getOdontologo().getUsuario().getApellidoUsuario()
                        : "Odontólogo General";

                return CitaProximaDto.builder()
                        .idCitas(c.getIdCitas())
                        .idPaciente(c.getPaciente() != null ? c.getPaciente().getIdPaciente() : null)
                        .nombrePaciente(nombrePac)
                        .telefonoPaciente(telPac)
                        .idOdontologo(c.getOdontologo() != null ? c.getOdontologo().getIdOdontologo() : null)
                        .nombreOdontologo(nombreOdont)
                        .especialidad(c.getOdontologo() != null ? c.getOdontologo().getEspecialidadOdontologo() : "")
                        .fechaCita(c.getFechaCita())
                        .horaInicioCita(c.getHoraInicioCita())
                        .horaFinCita(c.getHoraFinCita())
                        .minutosRestantes(1440)
                        .build();
            }).collect(Collectors.toList());
        }

        // De lo contrario, calcula según minutos
        int minutos = config.getTiempoAnticipacionMinutos() != null ? config.getTiempoAnticipacionMinutos() : 30;
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime ventanaLimite = ahora.plusMinutes(minutos);
        LocalDate hoy = ahora.toLocalDate();

        List<Cita> citas = citaRepository.findCitasProximas(hoy, ahora.minusMinutes(5), ventanaLimite);

        return citas.stream().map(c -> {
            long restantes = ChronoUnit.MINUTES.between(ahora, c.getHoraInicioCita());
            String nombrePac = (c.getPaciente() != null)
                    ? c.getPaciente().getNombrePaciente() + " " + c.getPaciente().getApellidoPaciente()
                    : "Paciente";
            String telPac = (c.getPaciente() != null) ? c.getPaciente().getTelefonoPaciente() : "";
            String nombreOdont = (c.getOdontologo() != null && c.getOdontologo().getUsuario() != null)
                    ? c.getOdontologo().getUsuario().getNombreUsuario() + " " + c.getOdontologo().getUsuario().getApellidoUsuario()
                    : "Odontólogo General";

            return CitaProximaDto.builder()
                    .idCitas(c.getIdCitas())
                    .idPaciente(c.getPaciente() != null ? c.getPaciente().getIdPaciente() : null)
                    .nombrePaciente(nombrePac)
                    .telefonoPaciente(telPac)
                    .idOdontologo(c.getOdontologo() != null ? c.getOdontologo().getIdOdontologo() : null)
                    .nombreOdontologo(nombreOdont)
                    .especialidad(c.getOdontologo() != null ? c.getOdontologo().getEspecialidadOdontologo() : "")
                    .fechaCita(c.getFechaCita())
                    .horaInicioCita(c.getHoraInicioCita())
                    .horaFinCita(c.getHoraFinCita())
                    .minutosRestantes(Math.max(0, restantes))
                    .build();
        }).collect(Collectors.toList());
    }
}
