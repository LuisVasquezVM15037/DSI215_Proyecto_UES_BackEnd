package com.dentalcare.api.dtos.Consulta.Tratamiento;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

/**
 * Propósito: Objeto de transferencia de datos (DTO) de salida para el catálogo de tratamientos.
 * Proyecta los procedimientos odontológicos disponibles para poblar listas y menús contextuales.
 * 
 * Ubicación y Rol: Capa de Presentación / DTO de Respuesta (Data Transfer Object).
 * 
 * Trazabilidad (Referencias):
 * - Utilizado por: ConsultaController.listarTratamientos(), ConsultaService.obtenerTodosLosTratamientos().
 * - Consumido por: Menús de selección de procedimientos en el odontograma y módulos de facturación.
 */
@Data
@AllArgsConstructor
public class TratamientoResponseDTO {

    /** Identificador primario del tratamiento */
    private Integer idTratamiento;

    /** Nombre del procedimiento */
    private String nombreTratamiento;

    /** Detalle clínico del tratamiento */
    private String descripcionTratamiento;

    /** Costo monetario fijado */
    private BigDecimal costoTratamiento;
}

