package com.dentalcare.api.controllers;

import com.dentalcare.api.dtos.Inventario.*;
import com.dentalcare.api.security.JwtUtil;
import com.dentalcare.api.services.InventarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventario")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class InventarioController {

    @Autowired
    private InventarioService inventarioService;

    @Autowired
    private JwtUtil jwtUtil;

    // ── MATERIALES (DENTALCARE-26) ─────────────────────────────────────────────

    @PostMapping("/materiales")
    public ResponseEntity<MaterialResponseDTO> registrarMaterial(@Valid @RequestBody MaterialRequestDTO dto) {
        MaterialResponseDTO creado = inventarioService.registrarMaterial(dto);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @GetMapping("/materiales")
    public ResponseEntity<List<MaterialResponseDTO>> listarMateriales() {
        return ResponseEntity.ok(inventarioService.listarMateriales());
    }

    @PatchMapping("/materiales/{id}")
    public ResponseEntity<MaterialResponseDTO> actualizarMaterial(
            @PathVariable Integer id,
            @Valid @RequestBody AjusteStockDTO dto) {
        return ResponseEntity.ok(inventarioService.actualizarMaterial(id, dto));
    }

    // ── MEDICAMENTOS (DENTALCARE-48) ───────────────────────────────────────────

    @PostMapping("/medicamentos")
    public ResponseEntity<MedicamentoItemResponseDTO> registrarIngresoMedicamento(
            @Valid @RequestBody MedicamentoItemRequestDTO dto) {
        MedicamentoItemResponseDTO creado = inventarioService.registrarIngresoMedicamento(dto);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @GetMapping("/medicamentos")
    public ResponseEntity<List<MedicamentoItemResponseDTO>> listarMedicamentos() {
        return ResponseEntity.ok(inventarioService.listarMedicamentos());
    }

    @PatchMapping("/medicamentos/{id}")
    public ResponseEntity<MedicamentoItemResponseDTO> actualizarMedicamento(
            @PathVariable Integer id,
            @Valid @RequestBody AjusteStockDTO dto) {
        return ResponseEntity.ok(inventarioService.actualizarMedicamento(id, dto));
    }

    // ── CONSULTA GENERAL DE INVENTARIO (DENTALCARE-46) ─────────────────────────

    @GetMapping("/items")
    public ResponseEntity<List<ItemInventarioResumenDTO>> consultarInventario(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String tipo) {
        return ResponseEntity.ok(inventarioService.consultarInventario(search, tipo));
    }

    // ── CONSUMO Y TRAZABILIDAD MENSUAL (DENTALCARE-47) ─────────────────────────

    @GetMapping("/consumos")
    public ResponseEntity<List<ConsumoResponseDTO>> listarConsumos(
            @RequestParam(required = false) String mes) {
        return ResponseEntity.ok(inventarioService.listarConsumos(mes));
    }

    @PostMapping("/consumos")
    public ResponseEntity<ConsumoResponseDTO> registrarConsumo(
            @Valid @RequestBody ConsumoRequestDTO dto,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String username = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.validateToken(token)) {
                username = jwtUtil.extractUsername(token);
            }
        }
        ConsumoResponseDTO creado = inventarioService.registrarConsumo(dto, username);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @PutMapping("/consumos/{id}")
    public ResponseEntity<ConsumoResponseDTO> modificarConsumo(
            @PathVariable Integer id,
            @RequestBody ConsumoRequestDTO dto) {
        return ResponseEntity.ok(inventarioService.modificarConsumo(id, dto));
    }

    @PostMapping("/consumos/{id}/confirmar")
    public ResponseEntity<ConsumoResponseDTO> confirmarConsumo(@PathVariable Integer id) {
        return ResponseEntity.ok(inventarioService.confirmarConsumo(id));
    }
}
