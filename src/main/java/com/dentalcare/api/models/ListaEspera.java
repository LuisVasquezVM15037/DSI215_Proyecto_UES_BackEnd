package com.dentalcare.api.models;

import com.dentalcare.api.models.enums.EstadoListaEspera;
import com.dentalcare.api.models.enums.PrioridadListaEspera;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entidad de persistencia para la gestión de lista de espera de pacientes.
 * DENTALCARE-92 (PBI-Secret-14)
 */
@Data
@Entity
@Table(name = "lista_espera")
public class ListaEspera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "odontologo_id", nullable = true)
    private Odontologo odontologo;

    @Column(name = "rango_horario_preferido", nullable = false, length = 100)
    private String rangoHorarioPreferido;

    @Column(name = "dias_preferidos", length = 100, nullable = true)
    private String diasPreferidos;

    @Column(name = "motivo_consulta", nullable = false, length = 255)
    private String motivoConsulta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PrioridadListaEspera prioridad = PrioridadListaEspera.MEDIA;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoListaEspera estado = EstadoListaEspera.EN_ESPERA;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cita_asignada_id", nullable = true)
    private Cita citaAsignada;

    @Column(columnDefinition = "TEXT", nullable = true)
    private String notas;
}
