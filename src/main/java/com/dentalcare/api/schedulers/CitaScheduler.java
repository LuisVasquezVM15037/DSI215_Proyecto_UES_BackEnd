package com.dentalcare.api.schedulers;

import com.dentalcare.api.services.CitaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Tarea programada (Scheduler) para el mantenimiento automático del ciclo de vida de las Citas.
 * 
 * Regla de Negocio:
 * Cuando una cita programada para el día actual pasa de las 11:59 PM (el día ha concluido)
 * y el paciente no asistió ni se reprogramó, cambia automáticamente a estado NO_ASISTIO.
 */
@Component
public class CitaScheduler {

    private static final Logger log = LoggerFactory.getLogger(CitaScheduler.class);

    @Autowired
    private CitaService citaService;

    /**
     * Tarea programada nocturna que se ejecuta diariamente a las 00:01 AM
     * (inmediatamente después de las 11:59 PM del día que concluye) en zona horaria America/El_Salvador.
     */
    @Scheduled(cron = "0 1 0 * * ?", zone = "America/El_Salvador")
    public void ejecutarCierreDiarioCitasNoAsistidas() {
        log.info("[CitaScheduler] Ejecutando cierre nocturno (00:01 AM): marcando citas no asistidas...");
        int total = citaService.actualizarCitasVencidasANoAsistio();
        log.info("[CitaScheduler] Cierre nocturno finalizado. Total citas marcadas como NO_ASISTIO: {}", total);
    }

    /**
     * Tarea periódica de respaldo que corre cada 30 minutos como salvaguarda
     * por si el servidor estuvo inactivo o reiniciado durante la medianoche.
     */
    @Scheduled(fixedDelay = 1800000, initialDelay = 10000)
    public void revisionPeriodicaCitasVencidas() {
        int total = citaService.actualizarCitasVencidasANoAsistio();
        if (total > 0) {
            log.info("[CitaScheduler] Revisión periódica: se actualizaron {} citas vencidas a NO_ASISTIO.", total);
        }
    }

    /**
     * Verificación inmediata al encender o reiniciar el servidor (Cold Start).
     */
    @EventListener(ApplicationReadyEvent.class)
    public void alIniciarSistema() {
        log.info("[CitaScheduler] Verificando citas de fechas concluidas al arranque del sistema...");
        int total = citaService.actualizarCitasVencidasANoAsistio();
        if (total > 0) {
            log.info("[CitaScheduler] Arranque: se actualizaron {} citas pendientes anteriores a NO_ASISTIO.", total);
        }
    }
}
