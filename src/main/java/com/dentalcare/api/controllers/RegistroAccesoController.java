package com.dentalcare.api.controllers;

import com.dentalcare.api.dtos.RegistroAcceso.RegistroAccesoDto;
import com.dentalcare.api.repositories.RegistroAccesoRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * RegistroAccesoController
 *
 * <b>Propósito:</b>
 * Controlador REST que expone la pista de auditoría de intentos de inicio de sesión.
 * Permite a los usuarios con rol de administrador inspeccionar los registros de accesos exitosos
 * y fallidos con sus respectivas marcas de tiempo e identidades de usuario.
 *
 * <b>Ubicación y Rol en la Arquitectura:</b>
 * - Capa: Presentación / Controlador REST (@RestController).
 * - Rol: Punto de acceso restringido para monitoreo y auditoría de seguridad del sistema.
 *
 * <b>Trazabilidad (Referencias):</b>
 * - Invocado por: Módulo de auditoría del frontend (ruta /revisar-accesos consumida desde usuario.service.js).
 * - Consume / Dependencias: {@link RegistroAccesoRepository}.
 */
@RestController
@RequestMapping("/api/registros-acceso")
public class RegistroAccesoController {

    private final RegistroAccesoRepository registroAccesoRepository;

    /**
     * Constructor para inyección de dependencias.
     *
     * @param registroAccesoRepository Repositorio para consulta de la bitácora de accesos.
     */
    public RegistroAccesoController(RegistroAccesoRepository registroAccesoRepository) {
        this.registroAccesoRepository = registroAccesoRepository;
    }

    /**
     * Recupera el historial completo de eventos de autenticación registrados.
     *
     * <b>Propósito:</b>
     * Proveer los datos de auditoría transformados a DTOs planos para su análisis en tablas administrativas.
     *
     * <b>Trazabilidad:</b>
     * - Endpoint: GET /api/registros-acceso
     *
     * @return Lista de {@link RegistroAccesoDto} con el resultado del acceso, fecha y correo del usuario.
     */
    @GetMapping
    public List<RegistroAccesoDto> getRegistrosAcceso() {
        return registroAccesoRepository.findAll()
                .stream()
                .map(registro -> new RegistroAccesoDto(
                        registro.getIdRegistroAcceso(),
                        registro.getEsExitoso(),
                        registro.getFechaAcceso(),
                        registro.getUsuario().getIdUsuario(),
                        registro.getUsuario().getEmailUsuario()
                ))
                .toList();
    }
}