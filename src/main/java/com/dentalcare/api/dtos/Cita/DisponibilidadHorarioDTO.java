package com.dentalcare.api.dtos.Cita;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisponibilidadHorarioDTO {

    private Integer idOdontologo;
    private String nombreOdontologo;
    private LocalDate fecha;
    private List<FranjaHorariaDTO> horariosDisponibles;
}
