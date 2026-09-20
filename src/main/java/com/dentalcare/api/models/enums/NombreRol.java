package com.dentalcare.api.models.enums;

/**
 * Propósito: Define el catálogo cerrado de roles de usuario del sistema para el esquema de seguridad RBAC.
 * Cada rol determina los permisos de acceso a endpoints de la API REST y las vistas del frontend.
 * 
 * Ubicación y Rol: Capa de Persistencia y Seguridad / Enumerador de Roles de Usuario.
 * 
 * Trazabilidad (Referencias):
 * - Utilizado por: Entidad Rol, RolRepository, SecurityConfig, UsuarioService, AutentificacionService.
 */
public enum NombreRol {
    /** Administrador general con privilegios totales sobre el sistema y configuración */
    ADMIN,
    
    /** Personal de recepción encargado de agendamiento, pacientes y accesos */
    RECEPCIONISTA,
    
    /** Odontólogo / Facultativo con acceso al módulo clínico, historial y consultas */
    ODONTOLOGO,
    
    /** Asistente odontológico con acceso a soporte de consulta y preparación clínica */
    ASISTENTEODONTOLOGO,
    
    /** Personal gerencial con acceso a reportes ejecutivos y estadísticas clínicas */
    GERENTE,
    
    /** Paciente con acceso limitado a su portal de citas y tratamientos */
    PACIENTE,
    
    /** Proveedor externo de insumos o medicamentos */
    PROVEEDOR,
    
    /** Rol comodín o auxiliar para propósitos futuros */
    OTRO
}

