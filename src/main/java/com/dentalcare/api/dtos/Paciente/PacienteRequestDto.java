package com.dentalcare.api.dtos.Paciente;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

/**
 * Propósito: Objeto de transferencia de datos (DTO) de entrada para la creación y edición de pacientes.
 * Separa el contrato público de la API REST del modelo de entidad de persistencia, implementando
 * validaciones estrictas (Bean Validation JSR-380) para salvaguardar la integridad de los datos clínicos y personales.
 * 
 * Ubicación y Rol: Capa de Presentación / DTO de Entrada (Data Transfer Object).
 * 
 * Trazabilidad (Referencias):
 * - Utilizado por: PacienteController (POST/PUT), PacienteService.
 * - Consumido por: Formularios de expediente y registro de pacientes en el frontend.
 */
@Data
public class PacienteRequestDto {

    /** Nombre de pila del paciente */
    @NotBlank(message = "El nombre es obligatorio.")
    private String nombrePaciente;

    /** Apellidos del paciente */
    @NotBlank(message = "El apellido es obligatorio.")
    private String apellidoPaciente;

    /** Número telefónico de contacto directo */
    private String telefonoPaciente;

    /** Fecha de nacimiento del paciente para cálculo de edad clínica */
    @NotNull(message = "La fecha de nacimiento es obligatoria.")
    private LocalDate fechaNacimientoPaciente;

    /** Documento Único de Identidad (DUI) u otro documento legal de identificación */
    @NotBlank(message = "El numero de identidad es obligatorio.")
    private String numeroIdentidadPaciente;

    /** Dirección de correo electrónico del paciente */
    @Email(message = "El formato del email no es valido.")
    private String emailPaciente;

    /** Nombre y teléfono de un contacto para situaciones de urgencia médica */
    private String contactoEmergencia;

    /** Alergias a medicamentos u observaciones de intolerancias farmacológicas */
    private String alergias;
}