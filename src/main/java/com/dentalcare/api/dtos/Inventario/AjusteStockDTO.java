package com.dentalcare.api.dtos.Inventario;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AjusteStockDTO {

    @NotNull(message = "El stock actual es obligatorio")
    @Min(value = 0, message = "El stock actual no puede ser menor a cero")
    private Integer stockActual;

    @Min(value = 0, message = "El stock mínimo no puede ser menor a cero")
    private Integer stockMinimo;

    private String motivoAjuste;
}
