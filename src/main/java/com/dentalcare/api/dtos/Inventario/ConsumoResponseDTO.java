package com.dentalcare.api.dtos.Inventario;

import com.dentalcare.api.models.enums.EstadoConsumo;
import com.dentalcare.api.models.enums.TipoItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsumoResponseDTO {

    private Integer id;
    private TipoItem tipoItem;
    private Integer itemId;
    private String itemCodigo;
    private String itemNombre;
    private Integer citaId;
    private String pacienteNombre;
    private String odontologoNombre;
    private Integer cantidad;
    private String mesConsumo;
    private EstadoConsumo estado;
    private LocalDateTime fechaConsumo;
    private Integer usuarioRegistroId;
    private String usuarioRegistroNombre;
    private String notas;
    private Integer stockDisponibleItem;
}
