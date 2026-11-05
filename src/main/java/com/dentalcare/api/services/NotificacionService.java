package com.dentalcare.api.services;

import com.dentalcare.api.dtos.Notificacion.NotificacionResponseDto;
import com.dentalcare.api.dtos.Notificacion.ResumenNotificacionesDto;
import com.dentalcare.api.models.Notificacion;
import com.dentalcare.api.models.Usuario;
import com.dentalcare.api.models.enums.TipoNotificacion;
import com.dentalcare.api.repositories.NotificacionRepository;
import com.dentalcare.api.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificacionService {

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional
    public NotificacionResponseDto crearNotificacion(Integer usuarioId, String rolDestinatario,
                                                     String titulo, String mensaje,
                                                     TipoNotificacion tipo, Integer referenciaId,
                                                     String rutaRedireccion) {
        Notificacion notificacion = new Notificacion();
        if (usuarioId != null) {
            Usuario u = usuarioRepository.findById(usuarioId).orElse(null);
            notificacion.setUsuario(u);
        }
        notificacion.setRolDestinatario(rolDestinatario);
        notificacion.setTitulo(titulo);
        notificacion.setMensaje(mensaje);
        notificacion.setTipo(tipo != null ? tipo : TipoNotificacion.SISTEMA);
        notificacion.setLeida(false);
        notificacion.setFechaCreacion(LocalDateTime.now());
        notificacion.setReferenciaId(referenciaId);
        notificacion.setRutaRedireccion(rutaRedireccion);

        Notificacion guardada = notificacionRepository.save(notificacion);
        return mapearADto(guardada);
    }

    public ResumenNotificacionesDto obtenerResumen(Integer usuarioId, String rol, Boolean soloNoLeidas, Integer limit) {
        int limiteEfectivo = (limit != null && limit > 0) ? limit : 20;
        Pageable pageable = PageRequest.of(0, limiteEfectivo);

        List<Notificacion> lista;
        if (Boolean.TRUE.equals(soloNoLeidas)) {
            lista = notificacionRepository.buscarNoLeidasPorUsuarioORol(usuarioId, rol, pageable);
        } else {
            lista = notificacionRepository.buscarPorUsuarioORol(usuarioId, rol, pageable);
        }

        long noLeidas = notificacionRepository.contarNoLeidas(usuarioId, rol);

        List<NotificacionResponseDto> dtos = lista.stream()
                .map(this::mapearADto)
                .collect(Collectors.toList());

        return ResumenNotificacionesDto.builder()
                .totalNoLeidas(noLeidas)
                .notificaciones(dtos)
                .build();
    }

    public long contarNoLeidas(Integer usuarioId, String rol) {
        return notificacionRepository.contarNoLeidas(usuarioId, rol);
    }

    @Transactional
    public NotificacionResponseDto marcarComoLeida(Integer notificacionId) {
        Notificacion notif = notificacionRepository.findById(notificacionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notificación no encontrada con id: " + notificacionId));
        notif.setLeida(true);
        Notificacion actualizada = notificacionRepository.save(notif);
        return mapearADto(actualizada);
    }

    @Transactional
    public int marcarTodasComoLeidas(Integer usuarioId, String rol) {
        return notificacionRepository.marcarTodasComoLeidas(usuarioId, rol);
    }

    private NotificacionResponseDto mapearADto(Notificacion n) {
        return NotificacionResponseDto.builder()
                .id(n.getId())
                .usuarioId(n.getUsuario() != null ? n.getUsuario().getIdUsuario() : null)
                .rolDestinatario(n.getRolDestinatario())
                .titulo(n.getTitulo())
                .mensaje(n.getMensaje())
                .tipo(n.getTipo())
                .leida(n.getLeida())
                .fechaCreacion(n.getFechaCreacion())
                .referenciaId(n.getReferenciaId())
                .rutaRedireccion(n.getRutaRedireccion())
                .build();
    }
}
