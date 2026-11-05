package com.dentalcare.api.controllers;

import com.dentalcare.api.dtos.Cita.ListaEsperaRequestDto;
import com.dentalcare.api.dtos.Cita.ListaEsperaResponseDto;
import com.dentalcare.api.models.enums.EstadoListaEspera;
import com.dentalcare.api.services.ListaEsperaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para la gestión de lista de espera de pacientes.
 * DENTALCARE-92 (PBI-Secret-14)
 */
@RestController
@RequestMapping("/api/agenda/lista-espera")
@CrossOrigin(origins = "*")
public class ListaEsperaController {

    @Autowired
    private ListaEsperaService listaEsperaService;

    @GetMapping
    public ResponseEntity<List<ListaEsperaResponseDto>> listar(
            @RequestParam(required = false) EstadoListaEspera estado) {
        List<ListaEsperaResponseDto> lista = listaEsperaService.listar(estado);
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ListaEsperaResponseDto> obtenerPorId(@PathVariable Integer id) {
        ListaEsperaResponseDto dto = listaEsperaService.obtenerPorId(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<ListaEsperaResponseDto> agregar(@RequestBody ListaEsperaRequestDto request) {
        ListaEsperaResponseDto creada = listaEsperaService.agregar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PatchMapping("/{id}/descartar")
    public ResponseEntity<Map<String, Object>> descartar(@PathVariable Integer id) {
        listaEsperaService.descartar(id);
        Map<String, Object> resp = new HashMap<>();
        resp.put("status", "success");
        resp.put("mensaje", "Registro marcado como descartado en lista de espera");
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> eliminar(@PathVariable Integer id) {
        listaEsperaService.eliminar(id);
        Map<String, Object> resp = new HashMap<>();
        resp.put("status", "success");
        resp.put("mensaje", "Registro eliminado permanentemente de la lista de espera");
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/{id}/asignar")
    public ResponseEntity<ListaEsperaResponseDto> asignarCita(
            @PathVariable Integer id,
            @RequestParam Integer citaId) {
        ListaEsperaResponseDto asignada = listaEsperaService.asignarCita(id, citaId);
        return ResponseEntity.ok(asignada);
    }
}
