package com.dentalcare.api.controllers;

import com.dentalcare.api.dtos.Odontologo.OdontologoResponseDto;
import com.dentalcare.api.models.Odontologo;
import com.dentalcare.api.repositories.OdontologoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * OdontologoController
 *
 * <b>Propósito:</b>
 * Publica los endpoints para la consulta de profesionales odontólogos habilitados.
 * Provee la lista filtrada de odontólogos con cuentas activas para alimentar los selectores
 * del formulario de citas médicas y asignaciones clínicas.
 *
 * <b>Ubicación y Rol en la Arquitectura:</b>
 * - Capa: Presentación / Controlador REST (@RestController).
 * - Rol: Punto de acceso HTTP para información del cuerpo médico.
 *
 * <b>Trazabilidad (Referencias):</b>
 * - Invocado por: Cliente frontend (usuario.service.js, cita.service.js) al agendar citas o listar personal.
 * - Consume / Dependencias: {@link OdontologoRepository}.
 */
@RestController
@RequestMapping("/api/odontologos")
public class OdontologoController {

    private final OdontologoRepository odontologoRepository;

    /**
     * Constructor para inyección de dependencias.
     *
     * @param odontologoRepository Repositorio de acceso a la entidad Odontologo.
     */
    public OdontologoController(OdontologoRepository odontologoRepository) {
        this.odontologoRepository = odontologoRepository;
    }

    /**
     * Lista los odontólogos cuyo usuario asociado se encuentra en estado activo.
     *
     * <b>Propósito:</b>
     * Proveer al módulo de citas únicamente aquellos facultativos disponibles para atención,
     * excluyendo a profesionales inactivos o dados de baja.
     *
     * <b>Trazabilidad:</b>
     * - Endpoint: GET /api/odontologos
     *
     * @return {@link ResponseEntity} con estado 200 OK y la lista de {@link OdontologoResponseDto}.
     */
    @GetMapping
    public ResponseEntity<List<OdontologoResponseDto>> listarOdontologos() {
        // Se filtra estrictamente por usuarios activos para no ofertar citas con personal inhabilitado
        List<OdontologoResponseDto> response = odontologoRepository.findByUsuario_EsActivoTrue()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Transforma la entidad relacional Odontologo en su DTO aplanado de respuesta.
     *
     * @param o Entidad de base de datos con su usuario asociado cargado.
     * @return DTO {@link OdontologoResponseDto} con nombre compuesto y credenciales JVPO.
     */
    private OdontologoResponseDto toDto(Odontologo o) {
        String nombreCompleto = o.getUsuario() != null
                ? o.getUsuario().getNombreUsuario() + " " + o.getUsuario().getApellidoUsuario()
                : "Sin asignar";
        return new OdontologoResponseDto(
                o.getIdOdontologo(),
                o.getEspecialidadOdontologo(),
                o.getJvpoId(),
                nombreCompleto
        );
    }
}