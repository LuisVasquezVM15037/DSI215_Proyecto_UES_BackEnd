package com.dentalcare.api.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entidad para almacenar evidencias fotográficas (radiografías, fotos clínicas intraorales)
 * asociadas a cada procedimiento realizado en la consulta (Punto 7).
 */
@Data
@Entity
@Table(name = "evidencia_procedimiento")
public class EvidenciaProcedimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idEvidencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_plan_tratamiento", nullable = false)
    private PlanTratamiento planTratamiento;

    @Column(name = "url_foto", nullable = false, length = 500)
    private String urlFoto;

    @Column(name = "nombre_archivo", length = 255)
    private String nombreArchivo;

    @Column(name = "fecha_subida", nullable = false)
    private LocalDateTime fechaSubida = LocalDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String comentario;
}
