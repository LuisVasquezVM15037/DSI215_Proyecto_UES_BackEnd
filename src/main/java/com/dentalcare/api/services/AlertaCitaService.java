package com.dentalcare.api.services;

import com.dentalcare.api.dtos.Cita.CitaProximaDto;
import com.dentalcare.api.dtos.Cita.ConfiguracionAlertaCitaDto;
import com.dentalcare.api.models.Cita;
import com.dentalcare.api.models.ConfiguracionAlertaCita;
import com.dentalcare.api.models.Usuario;
import com.dentalcare.api.repositories.CitaRepository;
import com.dentalcare.api.repositories.ConfiguracionAlertaCitaRepository;
import com.dentalcare.api.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AlertaCitaService {

    @Autowired
    private ConfiguracionAlertaCitaRepository configuracionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CitaRepository citaRepository;

    public ConfiguracionAlertaCitaDto obtenerConfiguracion(Integer usuarioId) {
        if (usuarioId == null) {
            return ConfiguracionAlertaCitaDto.builder()
                    .alertasActivas(true)
                    .tiempoAnticipacionMinutos(30)
                    .sonidoActivo(true)
                    .build();
        }

        return configuracionRepository.findByUsuario_IdUsuario(usuarioId)
                .map(c -> ConfiguracionAlertaCitaDto.builder()
                        .alertasActivas(c.getAlertasActivas())
                        .tiempoAnticipacionMinutos(c.getTiempoAnticipacionMinutos())
                        .sonidoActivo(c.getSonidoActivo())
                        .build())
                .orElse(ConfiguracionAlertaCitaDto.builder()
                        .alertasActivas(true)
                        .tiempoAnticipacionMinutos(30)
                        .sonidoActivo(true)
                        .build());
    }

    @Transactional
    public ConfiguracionAlertaCitaDto guardarConfiguracion(Integer usuarioId, ConfiguracionAlertaCitaDto dto) {
        if (usuarioId == null) {
            return dto;
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElse(null);
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
        config.setTiempoAnticipacionMinutos(
                dto.getTiempoAnticipacionMinutos() != null && dto.getTiempoAnticipacionMinutos() > 0
                        ? dto.getTiempoAnticipacionMinutos()
                        : 30
        );
        config.setSonidoActivo(dto.getSonidoActivo() != null ? dto.getSonidoActivo() : true);

        ConfiguracionAlertaCita guardada = configuracionRepository.save(config);

        return ConfiguracionAlertaCitaDto.builder()
                .alertasActivas(guardada.getAlertasActivas())
                .tiempoAnticipacionMinutos(guardada.getTiempoAnticipacionMinutos())
                .sonidoActivo(guardada.getSonidoActivo())
                .build();
    }

    public List<CitaProximaDto> obtenerCitasProximas(Integer usuarioId) {
        ConfiguracionAlertaCitaDto config = obtenerConfiguracion(usuarioId);
        if (!Boolean.TRUE.equals(config.getAlertasActivas())) {
            return new ArrayList<>();
        }

        int minutos = config.getTiempoAnticipacionMinutos() != null ? config.getTiempoAnticipacionMinutos() : 30;
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime ventanaLimite = ahora.plusMinutes(minutos);
        LocalDate hoy = ahora.toLocalDate();

        List<Cita> citas = citaRepository.findCitasProximas(hoy, ahora.minusMinutes(5), ventanaLimite);

        return citas.stream().map(c -> {
            long restantes = ChronoUnit.MINUTES.between(ahora, c.getHoraInicioCita());
            String nombrePac = (c.getPaciente() != null)
                    ? c.getPaciente().getNombrePaciente() + " " + c.getPaciente().getApellidoPaciente()
                    : "Paciente no registrado";
            String telPac = (c.getPaciente() != null) ? c.getPaciente().getTelefonoPaciente() : "";

            String nombreOdont = (c.getOdontologo() != null && c.getOdontologo().getUsuario() != null)
                    ? c.getOdontologo().getUsuario().getNombreUsuario() + " " + c.getOdontologo().getUsuario().getApellidoUsuario()
                    : "Odontólogo General";
            String esp = (c.getOdontologo() != null) ? c.getOdontologo().getEspecialidadOdontologo() : "";

            return CitaProximaDto.builder()
                    .idCitas(c.getIdCitas())
                    .idPaciente(c.getPaciente() != null ? c.getPaciente().getIdPaciente() : null)
                    .nombrePaciente(nombrePac)
                    .telefonoPaciente(telPac)
                    .idOdontologo(c.getOdontologo() != null ? c.getOdontologo().getIdOdontologo() : null)
                    .nombreOdontologo(nombreOdont)
                    .especialidad(esp)
                    .fechaCita(c.getFechaCita())
                    .horaInicioCita(c.getHoraInicioCita())
                    .horaFinCita(c.getHoraFinCita())
                    .minutosRestantes(Math.max(0, restantes))
                    .build();
        }).collect(Collectors.toList());
    }
}
