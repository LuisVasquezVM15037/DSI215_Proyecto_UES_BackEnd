package com.dentalcare.api.dtos.Inventario;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemInventarioResumenDTO {

    private Integer id;
    private String tipo; // "MATERIAL" o "MEDICAMENTO"
    private String codigo;
    private String nombre;
    private String presentacionOUnidad;
    private Integer stockActual;
    private Integer stockMinimo;
    private String estadoAlerta; // "OPTIMO", "BAJO", "CRITICO"
    private String descripcionOLote;
    private LocalDateTime actualizadoEn;
}
