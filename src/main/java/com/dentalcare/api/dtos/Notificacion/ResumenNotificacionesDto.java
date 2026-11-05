package com.dentalcare.api.dtos.Notificacion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumenNotificacionesDto {
    private long totalNoLeidas;
    private List<NotificacionResponseDto> notificaciones;
}
