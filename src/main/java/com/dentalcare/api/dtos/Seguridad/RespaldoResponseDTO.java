package com.dentalcare.api.dtos.Seguridad;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RespaldoResponseDTO {
    private Integer id;
    private String nombreArchivo;
    private String rutaAlmacenamiento;
    private Long tamanioBytes;
    private String tamanioFormateado;
    private String estado;
    private String creadoPor;
    private LocalDateTime creadoEn;
}
