package com.dentalcare.api.dtos.Cita;

import com.dentalcare.api.models.enums.EstadoListaEspera;
import com.dentalcare.api.models.enums.PrioridadListaEspera;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListaEsperaResponseDto {
    private Integer id;
    private Integer pacienteId;
    private String nombrePaciente;
    private String telefonoPaciente;
    private String numeroIdentidadPaciente;
    private Integer odontologoId;
    private String nombreOdontologo;
    private String rangoHorarioPreferido;
    private String diasPreferidos;
    private String motivoConsulta;
    private PrioridadListaEspera prioridad;
    private EstadoListaEspera estado;
    private LocalDateTime fechaSolicitud;
    private Integer citaAsignadaId;
    private String notas;
}
