package com.dentalcare.api.dtos.Cita;

import com.dentalcare.api.models.enums.PrioridadListaEspera;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListaEsperaRequestDto {
    private Integer pacienteId;
    private Integer odontologoId; // Opcional
    private String rangoHorarioPreferido;
    private String diasPreferidos;
    private String motivoConsulta;
    private PrioridadListaEspera prioridad; // ALTA, MEDIA, BAJA
    private String notas;
}
