package com.dentalcare.api.controllers;

import com.dentalcare.api.models.Rol;
import com.dentalcare.api.repositories.RolRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * RolController
 *
 * <b>Propósito:</b>
 * Publica el endpoint para la consulta del catálogo de roles del sistema.
 * Utilizado por los formularios de creación y edición de usuarios para poblar los selectores de perfiles.
 *
 * <b>Ubicación y Rol en la Arquitectura:</b>
 * - Capa: Presentación / Controlador REST (@RestController).
 * - Rol: Proveedor de datos de catálogo de roles de acceso.
 *
 * <b>Trazabilidad (Referencias):</b>
 * - Invocado por: Cliente frontend (usuario.service.js) al abrir formularios de gestión de usuarios.
 * - Consume / Dependencias: {@link RolRepository}.
 */
@RestController
@RequestMapping("/api/roles")
public class RolController {

    private final RolRepository rolRepository;

    /**
     * Constructor para inyección de dependencias.
     *
     * @param rolRepository Repositorio para acceso a los registros de roles.
     */
    public RolController(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    /**
     * Recupera la totalidad de roles registrados en la base de datos.
     *
     * <b>Propósito:</b>
     * Proveer el listado de roles (ej. ADMIN, ODONTOLOGO, RECEPCIONISTA) para asignación de permisos.
     *
     * <b>Trazabilidad:</b>
     * - Endpoint: GET /api/roles
     *
     * @return {@link ResponseEntity} con estado 200 OK y la lista de entidades {@link Rol}.
     */
    @GetMapping
    public ResponseEntity<List<Rol>> listarRoles() {
        return ResponseEntity.ok(rolRepository.findAll());
    }
}