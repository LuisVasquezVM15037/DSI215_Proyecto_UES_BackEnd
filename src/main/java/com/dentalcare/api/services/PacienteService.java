package com.dentalcare.api.services;

import com.dentalcare.api.dtos.Paciente.PacienteRequestDto;
import com.dentalcare.api.dtos.Paciente.PacienteResponseDto;
import com.dentalcare.api.models.Paciente;
import com.dentalcare.api.repositories.CitaRepository;
import com.dentalcare.api.repositories.PacienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * PacienteService
 *
 * <b>Propósito:</b>
 * Gestiona el registro demográfico y clínico de los pacientes de la clínica odontológica.
 * Aplica reglas de negocio sobre la unicidad del documento de identidad (DUI/Pasaporte),
 * búsquedas por coincidencia textual, actualización de fichas y comprobación estricta de
 * integridad referencial antes de la eliminación de registros.
 *
 * <b>Ubicación y Rol en la Arquitectura:</b>
 * - Capa: Lógica de Negocio / Capa de Servicios (@Service).
 * - Rol: Proveedor de lógica de dominio para pacientes, mediando entre el controlador REST y
 *   los repositorios de persistencia.
 *
 * <b>Trazabilidad (Referencias):</b>
 * - Invocado por: {@link com.dentalcare.api.controllers.PacienteController}.
 * - Consume / Dependencias: {@link PacienteRepository}, {@link CitaRepository}.
 */
@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final CitaRepository citaRepository;

    /**
     * Constructor para inyección de dependencias por inversión de control.
     *
     * @param pacienteRepository Repositorio para persistencia y consultas de pacientes.
     * @param citaRepository Repositorio de citas para verificación de integridad referencial.
     */
    public PacienteService(PacienteRepository pacienteRepository, CitaRepository citaRepository) {
        this.pacienteRepository = pacienteRepository;
        this.citaRepository = citaRepository;
    }

    /**
     * Obtiene el catálogo completo de pacientes registrados.
     *
     * <b>Propósito:</b>
     * Proveer el listado general de pacientes para la tabla principal del módulo de recepción.
     *
     * <b>Trazabilidad:</b>
     * - Invocado por: GET /api/pacientes.
     *
     * @return Lista de {@link PacienteResponseDto} con la información pública de cada paciente.
     */
    public List<PacienteResponseDto> listarTodos() {
        return pacienteRepository.findAll()
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Realiza búsquedas predictivas por término de texto (nombre, apellido o documento).
     *
     * <b>Propósito:</b>
     * Facilitar la localización rápida de fichas de pacientes en la barra de búsqueda del frontend.
     *
     * <b>Trazabilidad:</b>
     * - Invocado por: GET /api/pacientes/buscar?term={term}.
     *
     * @param term Subcadena de búsqueda ingresada por el usuario.
     * @return Lista de {@link PacienteResponseDto} que coinciden con el criterio.
     */
    public List<PacienteResponseDto> buscar(String term) {
        return pacienteRepository.buscarPorTermino(term)
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Da de alta un nuevo paciente en la base de datos.
     *
     * <b>Propósito:</b>
     * Validar que el número de documento de identidad no se encuentre duplicado y registrar
     * la ficha demográfica y de antecedentes alérgicos.
     *
     * <b>Trazabilidad:</b>
     * - Invocado por: POST /api/pacientes.
     *
     * @param request DTO {@link PacienteRequestDto} validado con los datos del nuevo paciente.
     * @return DTO {@link PacienteResponseDto} con la entidad creada y su identificador generado.
     * @throws RuntimeException Si el número de identidad ya existe en la base de datos.
     */
    public PacienteResponseDto crearPaciente(PacienteRequestDto request) {
        // Se asegura la unicidad a nivel de negocio antes de intentar la inserción en la base de datos
        if (pacienteRepository.findByNumeroIdentidadPaciente(request.getNumeroIdentidadPaciente()).isPresent()) {
            throw new RuntimeException("El numero de identidad '" + request.getNumeroIdentidadPaciente() + "' ya esta registrado.");
        }

        Paciente paciente = new Paciente();
        mapRequestToEntity(request, paciente);

        return toResponseDto(pacienteRepository.save(paciente));
    }

    /**
     * Actualiza la información personal y médica de un paciente existente.
     *
     * <b>Propósito:</b>
     * Modificar datos de contacto, residencia o antecedentes médicos, validando que si el documento
     * de identidad cambia, no colisione con el de otro paciente registrado.
     *
     * <b>Trazabilidad:</b>
     * - Invocado por: PUT /api/pacientes/{id}.
     *
     * @param id Clave primaria del paciente a modificar.
     * @param request DTO {@link PacienteRequestDto} con los nuevos valores.
     * @return DTO {@link PacienteResponseDto} con los datos modificados.
     * @throws RuntimeException Si el paciente no existe o el nuevo documento de identidad ya está en uso.
     */
    public PacienteResponseDto actualizarPaciente(Integer id, PacienteRequestDto request) {
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente con id " + id + " no encontrado."));

        // Solo se verifica la unicidad si el documento enviado difiere del documento actual del paciente
        if (!paciente.getNumeroIdentidadPaciente().equals(request.getNumeroIdentidadPaciente()) &&
             pacienteRepository.findByNumeroIdentidadPaciente(request.getNumeroIdentidadPaciente()).isPresent()) {
            throw new RuntimeException("El numero de identidad '" + request.getNumeroIdentidadPaciente() + "' ya esta registrado.");
        }

        mapRequestToEntity(request, paciente);

        return toResponseDto(pacienteRepository.save(paciente));
    }

    /**
     * Elimina el registro de un paciente verificando restricciones de integridad.
     *
     * <b>Propósito:</b>
     * Impedir la eliminación física si el paciente cuenta con citas o historial clínico previo,
     * protegiendo la base de datos contra errores de clave foránea (FK constraint violation).
     *
     * <b>Trazabilidad:</b>
     * - Invocado por: DELETE /api/pacientes/{id}.
     *
     * @param id Clave primaria del paciente a eliminar.
     * @throws RuntimeException Si el paciente no existe.
     * @throws IllegalStateException Si el paciente tiene citas o dependencias asociadas en su historial.
     */
    public void eliminarPaciente(Integer id) {
        if (!pacienteRepository.existsById(id)) {
            throw new RuntimeException("Paciente con id " + id + " no encontrado.");
        }
        // Validación preventiva de integridad referencial para evitar excepciones SQL 500 no controladas
        if (citaRepository.existsByPaciente_IdPaciente(id)) {
            throw new IllegalStateException("No se puede eliminar el paciente porque tiene citas registradas en su historial.");
        }
        pacienteRepository.deleteById(id);
    }

    /**
     * Transfiere los campos del DTO hacia la entidad del modelo.
     *
     * @param request DTO de entrada.
     * @param paciente Entidad de destino.
     */
    private void mapRequestToEntity(PacienteRequestDto request, Paciente paciente) {
        paciente.setNombrePaciente(request.getNombrePaciente());
        paciente.setApellidoPaciente(request.getApellidoPaciente());
        paciente.setTelefonoPaciente(request.getTelefonoPaciente());
        paciente.setFechaNacimientoPaciente(request.getFechaNacimientoPaciente());
        paciente.setNumeroIdentidadPaciente(request.getNumeroIdentidadPaciente());
        paciente.setEmailPaciente(request.getEmailPaciente());
        paciente.setContactoEmergencia(request.getContactoEmergencia());
        paciente.setAlergias(request.getAlergias());
    }

    /**
     * Convierte la entidad Paciente en su DTO de transferencia seguro.
     *
     * @param p Entidad de base de datos.
     * @return DTO de salida sin acoplamiento a la capa de persistencia.
     */
    private PacienteResponseDto toResponseDto(Paciente p) {
        return new PacienteResponseDto(
                p.getIdPaciente(),
                p.getNombrePaciente(),
                p.getApellidoPaciente(),
                p.getTelefonoPaciente(),
                p.getFechaNacimientoPaciente(),
                p.getNumeroIdentidadPaciente(),
                p.getEmailPaciente(),
                p.getContactoEmergencia(),
                p.getAlergias()
        );
    }
}