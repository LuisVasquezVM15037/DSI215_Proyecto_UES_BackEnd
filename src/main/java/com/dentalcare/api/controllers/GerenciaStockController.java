package com.dentalcare.api.controllers;

import com.dentalcare.api.dtos.Inventario.AlertaStockDTO;
import com.dentalcare.api.dtos.Inventario.AlertaStockResumenDTO;
import com.dentalcare.api.services.InventarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador de Supervisión y Auditoría Gerencial de Stock Crítico.
 * DENTALCARE-73 (PBI-Gerencia-03: Visualizar Alertas De Stock - Rol: Gerente)
 *
 * REGLA DE NEGOCIO ESTRICTA:
 * Este controlador es de SOLO LECTURA. Cualquier mutación (POST, PUT, PATCH, DELETE)
 * es rechazada inmediatamente con 403 Forbidden para preservar la segregación de funciones.
 */
@RestController
@RequestMapping("/api/gerencia/stock-critico")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "*"})
public class GerenciaStockController {

    @Autowired
    private InventarioService inventarioService;

    /**
     * Listado analítico de alertas de stock para supervisión gerencial.
     * Permite ordenamiento por urgencia_desc, deficit_desc, nombre_asc o cobertura_asc.
     */
    @GetMapping
    public ResponseEntity<List<AlertaStockDTO>> listarAlertasGerencia(
            @RequestParam(required = false, defaultValue = "urgencia_desc") String orden) {
        return ResponseEntity.ok(inventarioService.obtenerAlertasStock(orden));
    }

    /**
     * Top 3 insumos más críticos con menor porcentaje de cobertura de stock mínimo.
     */
    @GetMapping("/top3")
    public ResponseEntity<List<AlertaStockDTO>> obtenerTop3Gerencia() {
        return ResponseEntity.ok(inventarioService.obtenerTop3Criticos());
    }

    /**
     * Resumen consolidado para KPIs e indicadores gerenciales.
     */
    @GetMapping("/resumen")
    public ResponseEntity<AlertaStockResumenDTO> obtenerResumenGerencia() {
        return ResponseEntity.ok(inventarioService.obtenerResumenAlertas());
    }

    /**
     * Descarga de informe gerencial de auditoría en formato Excel (.xlsx).
     */
    @GetMapping("/exportar-excel")
    public ResponseEntity<byte[]> exportarAuditoriaExcel() {
        byte[] excelBytes = inventarioService.generarReporteExcelAlertas();
        String filename = "Auditoria_Stock_Critico_DentalCare_" +
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .body(excelBytes);
    }

    /**
     * Bloqueo estricto de mutaciones en el endpoint gerencial (DENTALCARE-73).
     * Asegura la separación de responsabilidades impidiendo creación o modificación.
     */
    @RequestMapping(
            value = {"", "/**"},
            method = {RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE}
    )
    public ResponseEntity<Map<String, Object>> rechazarModificacionesGerencia() {
        Map<String, Object> error = new HashMap<>();
        error.put("error", "FORBIDDEN");
        error.put("codigo", "SOLO_LECTURA");
        error.put("mensaje", "Acceso denegado: La consola de supervisión gerencial de inventario es estrictamente de solo lectura. No se permiten registros ni modificaciones desde este perfil.");
        error.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }
}
