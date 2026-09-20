package com.dentalcare.api.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

/**
 * Paciente
 *
 * <b>Propósito:</b>
 * Entidad JPA que mapea la tabla 'paciente' en la base de datos.
 * Modela la ficha demográfica y los antecedentes de alergias o emergencias
 * de cada paciente atendido en la clínica dental.
 *
 * <b>Ubicación y Rol en la Arquitectura:</b>
 * - Capa: Dominio / Modelo de Datos (JPA Entity).
 * - Rol: Entidad central del historial clínico, relacionada con las citas médicas.
 *
 * <b>Trazabilidad (Referencias):</b>
 * - Referenciado por: {@link Cita}, {@link com.dentalcare.api.repositories.PacienteRepository}.
 */
@Data
@Entity
@Table(name = "paciente")
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPaciente;

    @Column(nullable = false)
    private String nombrePaciente;

    @Column(nullable = false)
    private String apellidoPaciente;

    private String telefonoPaciente;

    @Column(nullable = false)
    private LocalDate fechaNacimientoPaciente;

    @Column(nullable = false, unique = true)
    private String numeroIdentidadPaciente;

    private String emailPaciente;

    private String contactoEmergencia;

    @Column(columnDefinition = "TEXT")
    private String alergias;
}