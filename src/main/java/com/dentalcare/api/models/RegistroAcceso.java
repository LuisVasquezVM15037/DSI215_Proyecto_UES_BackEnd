package com.dentalcare.api.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Propósito: Modela los eventos de auditoría y trazabilidad para los intentos de inicio de sesión
 * en el sistema. Registra tanto los accesos exitosos como los intentos fallidos, permitiendo
 * detectar anomalías de seguridad o intentos de intrusión por fuerza bruta.
 * 
 * Ubicación y Rol: Capa de Persistencia / Entidad de Auditoría y Seguridad.
 * 
 * Trazabilidad (Referencias):
 * - Utilizado por: RegistroAccesoRepository, RegistroAccesoController, AutentificacionService.
 * - Relaciones:
 *   - Vinculado a Usuario (@ManyToOne): Identifica el usuario que intentó la autenticación.
 */
@Data
@Entity
@Table(name = "registro_acceso")
public class RegistroAcceso {

    /** Identificador único autoincremental del registro de acceso */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_registro_acceso")
    private Integer idRegistroAcceso;

    /** Indica si el intento de autenticación culminó con éxito (true) o si fue rechazado (false) */
    @Column(name = "es_exitoso", nullable = false)
    private Boolean esExitoso;

    /** Marca temporal precisa (fecha y hora) en que ocurrió el evento de acceso */
    @Column(name = "fecha_acceso", nullable = false)
    private LocalDateTime fechaAcceso;

    /** Cuenta de usuario asociada al intento de inicio de sesión */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;
}