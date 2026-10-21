package com.dentalcare.api.dtos.Inventario;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MaterialRequestDTO {

    @NotBlank(message = "El código es obligatorio")
    @Pattern(regexp = "^(?i)MT\\d+$", message = "El código debe tener formato MT seguido de números (ej: MT78)")
    private String codigo;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 5, max = 100, message = "El nombre debe contener entre 5 y 100 caracteres")
    private String nombre;

    @NotBlank(message = "La unidad de medida es obligatoria")
    private String unidadMedida;

    @Min(value = 0, message = "El stock actual no puede ser negativo")
    private Integer stockActual = 0;

    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    private Integer stockMinimo = 5;

    private String descripcion;
}
