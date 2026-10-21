package com.dentalcare.api.dtos.Inventario;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

@Data
public class MedicamentoItemRequestDTO {

    @NotBlank(message = "El código es obligatorio")
    @Pattern(regexp = "^(?i)MD\\d+$", message = "El código debe tener formato MD seguido de números (ej: MD45)")
    private String codigo;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 5, max = 100, message = "El nombre debe contener entre 5 y 100 caracteres")
    private String nombre;

    private String presentacion;

    private String lote;

    private LocalDate fechaVencimiento;

    @NotNull(message = "La cantidad a ingresar es obligatoria")
    @Min(value = 1, message = "La cantidad a ingresar debe ser mayor que cero")
    private Integer cantidadIngreso;

    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    private Integer stockMinimo = 5;
}
