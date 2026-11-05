package com.dentalcare.api.dtos.Cita;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracionAlertaCitaDto {
    private Boolean alertasActivas;
    private Integer tiempoAnticipacionMinutos;
    private Boolean sonidoActivo;
}
