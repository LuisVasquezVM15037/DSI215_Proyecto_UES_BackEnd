package com.dentalcare.api.dtos.Inventario;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialResponseDTO {

    private Integer id;
    private String codigo;
    private String nombre;
    private String unidadMedida;
    private Integer stockActual;
    private Integer stockMinimo;
    private String descripcion;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
    private String estadoAlerta; // OPTIMO, MINIMO, CRITICO
}
