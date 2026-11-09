package com.dentalcare.api.controllers;

import com.dentalcare.api.dtos.Reporte.*;
import com.dentalcare.api.security.JwtUtil;
import com.dentalcare.api.services.RendimientoService;
import com.dentalcare.api.services.ReporteGerenciaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controlador REST para Inteligencia de Negocios, Reportes y Rendimiento Clínico (Rol: Gerente).
 * DENTALCARE-70 (PBI-Gerencia-01: Seleccionar Reporte)
 * DENTALCARE-72 (PBI-Gerencia-02: Aplicar Filtros a Reportes)
 * DENTALCARE-74 (PBI-Gerencia-04: Visualizar Resultados de Reportes)
 * DENTALCARE-95 (PBI-Gerencia-05: Configurar Alertas de Rendimiento)
 */
@RestController
@RequestMapping("/api/gerencia")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "*"})
public class GerenciaReportesController {

    @Autowired
    private ReporteGerenciaService reporteGerenciaService;

    @Autowired
    private RendimientoService rendimientoService;

    @Autowired
    private JwtUtil jwtUtil;

    // =========================================================================
    // 1. CATÁLOGO Y CONSULTA DE REPORTES (DENTALCARE-70 & DENTALCARE-72 & DENTALCARE-74)
    // =========================================================================

    @GetMapping("/reportes/tipos")
    public ResponseEntity<List<TipoReporteDTO>> listarTiposReportes() {
        return ResponseEntity.ok(reporteGerenciaService.obtenerTiposReportes());
    }

    @GetMapping("/reportes/datos")
    public ResponseEntity<ReporteResponseDTO> obtenerDatosReporte(
            @RequestParam(required = false, defaultValue = "CONSUMO_MATERIALES") String tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) String categoriaItem,
            @RequestParam(required = false) Integer itemId,
            @RequestParam(required = false) Integer odontologoId,
            @RequestParam(required = false) String estadoCita) {

        ReporteResponseDTO reporte = reporteGerenciaService.generarReporte(
                tipo, fechaInicio, fechaFin, categoriaItem, itemId, odontologoId, estadoCita);
        return ResponseEntity.ok(reporte);
    }

    @GetMapping("/reportes/exportar")
    public ResponseEntity<byte[]> exportarReporte(
            @RequestParam(required = false, defaultValue = "CONSUMO_MATERIALES") String tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) String categoriaItem,
            @RequestParam(required = false) Integer itemId,
            @RequestParam(required = false) Integer odontologoId,
            @RequestParam(required = false) String estadoCita,
            @RequestParam(required = false, defaultValue = "excel") String formato) {

        byte[] excelBytes = reporteGerenciaService.exportarReporteExcel(
                tipo, fechaInicio, fechaFin, categoriaItem, itemId, odontologoId, estadoCita);

        String fechaHoy = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String filename = "Reporte_" + tipo.toUpperCase() + "_" + fechaHoy + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .body(excelBytes);
    }

    // =========================================================================
    // 2. ALERTAS Y UMBRALES DE RENDIMIENTO CLÍNICO (DENTALCARE-95)
    // =========================================================================

    @GetMapping("/alertas-rendimiento")
    public ResponseEntity<List<ReglaAlertaRendimientoResponseDTO>> listarReglasRendimiento() {
        return ResponseEntity.ok(rendimientoService.listarReglas());
    }

    @PostMapping("/alertas-rendimiento")
    public ResponseEntity<ReglaAlertaRendimientoResponseDTO> crearReglaRendimiento(
            @Valid @RequestBody ReglaAlertaRendimientoRequestDTO dto,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String username = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.validateToken(token)) {
                username = jwtUtil.extractUsername(token);
            }
        }

        ReglaAlertaRendimientoResponseDTO creada = rendimientoService.crearRegla(dto, username);
        return new ResponseEntity<>(creada, HttpStatus.CREATED);
    }

    @PutMapping("/alertas-rendimiento/{id}")
    public ResponseEntity<ReglaAlertaRendimientoResponseDTO> actualizarReglaRendimiento(
            @PathVariable Integer id,
            @Valid @RequestBody ReglaAlertaRendimientoRequestDTO dto) {
        return ResponseEntity.ok(rendimientoService.actualizarRegla(id, dto));
    }

    @PatchMapping("/alertas-rendimiento/{id}/toggle")
    public ResponseEntity<ReglaAlertaRendimientoResponseDTO> toggleReglaRendimiento(
            @PathVariable Integer id) {
        return ResponseEntity.ok(rendimientoService.toggleRegla(id));
    }

    @PostMapping("/alertas-rendimiento/evaluar")
    public ResponseEntity<EvaluacionReglasResumenDTO> evaluarReglasManualmente() {
        return ResponseEntity.ok(rendimientoService.evaluarReglas());
    }

    @DeleteMapping("/alertas-rendimiento/{id}")
    public ResponseEntity<Void> eliminarReglaRendimiento(@PathVariable Integer id) {
        rendimientoService.eliminarRegla(id);
        return ResponseEntity.noContent().build();
    }
}
