package com.dentalcare.api.dtos.Consulta.Tratamiento;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvidenciaResponseDTO {
    private Integer idEvidencia;
    private Integer idPlanTratamiento;
    private String urlFoto;
    private String nombreArchivo;
    private LocalDateTime fechaSubida;
    private String comentario;
}
