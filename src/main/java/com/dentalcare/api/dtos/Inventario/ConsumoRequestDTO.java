package com.dentalcare.api.dtos.Inventario;

import com.dentalcare.api.models.enums.TipoItem;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ConsumoRequestDTO {

    @NotNull(message = "El tipo de ítem es obligatorio (MATERIAL o MEDICAMENTO)")
    private TipoItem tipoItem;

    @NotNull(message = "El ID del ítem es obligatorio")
    private Integer itemId;

    private Integer citaId; // Opcional

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a cero")
    private Integer cantidad;

    private String mesConsumo; // Formato 'YYYY-MM'. Si es null, se calcula a partir del mes actual

    private String notas;
}
