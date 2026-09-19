package com.dentalcare.api.models.enums;

public enum EstadoCita {
    PROGRAMADA, 
    FINALIZADA, 
    COMPLETADA, // Conservado para compatibilidad con registros historicos de BD
    CANCELADA, 
    PENDIENTE, 
    EN_PROGRESO,
    REPROGRAMADA,
    NO_ASISTIO,
    OTRO
}
