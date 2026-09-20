package com.dentalcare.api.models;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Propósito: Representa la entidad de dominio persistente 'odontologo'. Modela el perfil profesional
 * de los facultativos dentales en la clínica, vinculando su cuenta de usuario del sistema con sus
 * credenciales sanitarias y número de junta de vigilancia (JVPO).
 * 
 * Ubicación y Rol: Capa de Persistencia / Modelo de Dominio (ORM JPA / Hibernate).
 * 
 * Trazabilidad (Referencias):
 * - Utilizado por: OdontologoRepository, OdontologoController, CitaService, ConsultaService.
 * - Relaciones:
 *   - Pertenece a Usuario (@ManyToOne): Cuenta de acceso y datos personales base.
 *   - Referenciado en Cita: Como profesional asignado a las citas médicas.
 */
@Data
@Entity
@Table(name = "odontologo")
public class Odontologo {

    /** Identificador único autoincremental del odontólogo en la base de datos */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idOdontologo;

    /** Cuenta de usuario asociada a este perfil profesional */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    /** Rama o especialidad odontológica del profesional (ej. Ortodoncia, Endodoncia, Odontopediatría) */
    @Column(nullable = false)
    private String especialidadOdontologo;

    /** 
     * Identificador de registro en la Junta de Vigilancia de la Profesión Odontológica (JVPO).
     * Se define como String para soportar códigos alfanuméricos y formatos con guiones.
     */
    @Column(nullable = false, unique = true)
    private String jvpoId; 
}