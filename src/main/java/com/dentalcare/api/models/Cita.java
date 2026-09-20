package com.dentalcare.api.models;

import com.dentalcare.api.models.enums.EstadoCita;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Cita
 *
 * <b>Propósito:</b>
 * Entidad JPA que mapea la tabla 'cita' en la base de datos relacional.
 * Modela un turno de atención odontológica asignado a un paciente y a un odontólogo,
 * registrando fecha, rango horario, estado del ciclo de atención y motivo de cancelación si aplica.
 *
 * <b>Ubicación y Rol en la Arquitectura:</b>
 * - Capa: Dominio / Modelo de Datos (JPA Entity).
 * - Rol: Entidad nuclear de la agenda clínica que conecta pacientes, profesionales y evaluaciones.
 *
 * <b>Trazabilidad (Referencias):</b>
 * - Referenciado por: {@link Odontologo}, {@link Paciente}, {@link EvaluacionClinica},
 *   {@link Prescripcion}, {@link com.dentalcare.api.repositories.CitaRepository}.
 */
@Data
@Entity
@Table(name = "cita")
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCitas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_odontologo", nullable = false)
    private Odontologo odontologo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_paciente", nullable = false)
    private Paciente paciente;

    @Column(nullable = false)
    private LocalDate fechaCita;

    @Column(nullable = false)
    private LocalDateTime horaInicioCita;

    @Column(nullable = false)
    private LocalDateTime horaFinCita;

    private String motivoCancelacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCita estadoCita;
}