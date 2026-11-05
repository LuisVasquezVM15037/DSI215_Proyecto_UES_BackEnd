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
    private String anticipacionTipo; // DIA_ANTERIOR, MISMO_DIA, HORAS_30
    private Integer tiempoAnticipacionMinutos;
    private Boolean sonidoActivo;
    private Boolean canalWhatsApp;
    private Boolean canalCorreo;
    private String plantillaMensaje;
}
