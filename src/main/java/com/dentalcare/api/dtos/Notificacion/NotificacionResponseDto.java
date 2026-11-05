package com.dentalcare.api.dtos.Notificacion;

import com.dentalcare.api.models.enums.TipoNotificacion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionResponseDto {
    private Integer id;
    private Integer usuarioId;
    private String rolDestinatario;
    private String titulo;
    private String mensaje;
    private TipoNotificacion tipo;
    private Boolean leida;
    private LocalDateTime fechaCreacion;
    private Integer referenciaId;
    private String rutaRedireccion;
}
