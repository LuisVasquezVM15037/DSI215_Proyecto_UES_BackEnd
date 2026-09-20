package com.dentalcare.api.dtos.Consulta.Medicamento;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

/**
 * Propósito: Objeto de transferencia de datos (DTO) de salida para el catálogo de medicamentos.
 * Expone la información farmacológica y el stock disponible para alimentar el selector de prescripciones
 * en el módulo de consulta odontológica.
 * 
 * Ubicación y Rol: Capa de Presentación / DTO de Respuesta (Data Transfer Object).
 * 
 * Trazabilidad (Referencias):
 * - Utilizado por: ConsultaController.listarMedicamentos(), ConsultaService.obtenerTodosLosMedicamentos().
 * - Consumido por: Menús de selección de medicamentos en el asistente de prescripción médica.
 */
@Data
@AllArgsConstructor
public class MedicamentoResponseDTO {

    /** Identificador primario del medicamento */
    private Integer idMedicamento;

    /** Nombre comercial del fármaco */
    private String nombreMedicamento;

    /** Principio activo químico */
    private String componenteActivo;

    /** Concentración o dosificación comercial (ej. 500mg) */
    private String concentracion;

    /** Costo unitario de referencia */
    private BigDecimal costoMedicamento;

    /** Existencias actuales en inventario */
    private Integer cantidadInventario;
}

