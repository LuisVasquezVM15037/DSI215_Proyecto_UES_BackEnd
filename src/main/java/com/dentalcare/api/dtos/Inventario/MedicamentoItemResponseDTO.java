package com.dentalcare.api.dtos.Inventario;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicamentoItemResponseDTO {

    private Integer id;
    private String codigo;
    private String nombre;
    private String presentacion;
    private String lote;
    private LocalDate fechaVencimiento;
    private Integer stockActual;
    private Integer stockMinimo;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
    private String estadoAlerta;
}
