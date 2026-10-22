package com.dentalcare.api.dtos.Cita;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FranjaHorariaDTO {

    private String horaInicio;      // "08:00"
    private String horaFin;         // "09:00"
    private String horaInicioIso;   // "2026-10-24T08:00"
    private String horaFinIso;      // "2026-10-24T09:00"
    private boolean disponible;     // true / false
    private String motivo;          // null o "Ocupado: Cita #CITA-X"
    private Integer idCita;         // null si disponible
}
