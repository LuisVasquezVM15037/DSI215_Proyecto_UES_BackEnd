package com.dentalcare.api.models.enums;

/**
 * Propósito: Define las fases o estados evolutivos de una intervención clínica en el tiempo.
 * Permite registrar si una sesión corresponde al inicio, seguimiento o culminación de un tratamiento.
 * 
 * Ubicación y Rol: Capa de Persistencia / Enumerador de Dominio Clínico.
 * 
 * Trazabilidad (Referencias):
 * - Utilizado por: Entidad EvolucionTratamiento.
 */
public enum EstadoEvolucion {
    /** Primera sesión en la que se da apertura al tratamiento */
    INICIADO, 
    
    /** Sesión intermedia de avance o curación continua */
    CONTINUACION, 
    
    /** Sesión de alta o culminación exitosa del tratamiento clínico */
    FINALIZADO, 
    
    /** Intervención pausada o en espera de insumos/evolución del paciente */
    PENDIENTE, 
    
    /** Procedimiento en ejecución activa */
    EN_PROGRESO,
    
    /** Estado comodín para eventualidades clínicas no tabuladas */
    OTRO   
}

