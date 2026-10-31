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
public class RestablecerPasswordResponseDTO {
    private Integer idUsuario;
    private String username;
    private String email;
    private String claveProvisional;
    private String token;
    private LocalDateTime expiraEn;
    private String enlaceRestablecimiento;
    private String mensajeNotificacion;
}
