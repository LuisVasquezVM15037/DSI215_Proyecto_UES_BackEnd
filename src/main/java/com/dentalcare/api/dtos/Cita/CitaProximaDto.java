package com.dentalcare.api.dtos.Cita;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CitaProximaDto {
    private Integer idCitas;
    private Integer idPaciente;
    private String nombrePaciente;
    private String telefonoPaciente;
    private Integer idOdontologo;
    private String nombreOdontologo;
    private String especialidad;
    private LocalDate fechaCita;
    private LocalDateTime horaInicioCita;
    private LocalDateTime horaFinCita;
    private long minutosRestantes;
}
