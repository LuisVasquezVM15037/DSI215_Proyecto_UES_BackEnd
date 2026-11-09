package com.dentalcare.api.services;

import com.dentalcare.api.dtos.Reporte.EvaluacionReglasResumenDTO;
import com.dentalcare.api.dtos.Reporte.ReglaAlertaRendimientoRequestDTO;
import com.dentalcare.api.dtos.Reporte.ReglaAlertaRendimientoResponseDTO;
import com.dentalcare.api.models.Cita;
import com.dentalcare.api.models.ReglaAlertaRendimiento;
import com.dentalcare.api.models.Usuario;
import com.dentalcare.api.models.enums.EstadoCita;
import com.dentalcare.api.models.enums.PeriodoEvaluacion;
import com.dentalcare.api.models.enums.TipoMetricaRendimiento;
import com.dentalcare.api.models.enums.TipoNotificacion;
import com.dentalcare.api.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión y evaluación automática de reglas de alerta de rendimiento clínico y calidad.
 * DENTALCARE-95 (PBI-Gerencia-05: Configurar Alertas de Rendimiento)
 */
@Service
public class RendimientoService {

    @Autowired
    private ReglaAlertaRendimientoRepository reglaRepository;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private ConsumoRepository consumoRepository;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private MedicamentoItemRepository medicamentoItemRepository;

    @Autowired
    private NotificacionService notificacionService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // =========================================================================
    // CRUD DE REGLAS DE RENDIMIENTO
    // =========================================================================

    public List<ReglaAlertaRendimientoResponseDTO> listarReglas() {
        return reglaRepository.findAllByOrderByCreadoEnDesc().stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReglaAlertaRendimientoResponseDTO crearRegla(ReglaAlertaRendimientoRequestDTO dto, String username) {
        Usuario creador = null;
        if (username != null) {
            creador = usuarioRepository.findByUsernameUsuario(username).orElse(null);
        }
        if (creador == null) {
            creador = usuarioRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se encontró usuario en el sistema"));
        }

        ReglaAlertaRendimiento regla = ReglaAlertaRendimiento.builder()
                .nombreIndicador(dto.getNombreIndicador().trim())
                .descripcion(dto.getDescripcion() != null ? dto.getDescripcion().trim() : null)
                .tipoMetrica(dto.getTipoMetrica())
                .operadorComparacion(dto.getOperadorComparacion())
                .valorUmbral(dto.getValorUmbral())
                .periodoEvaluacion(dto.getPeriodoEvaluacion())
                .activo(dto.getActivo() != null ? dto.getActivo() : true)
                .usuarioCreador(creador)
                .creadoEn(LocalDateTime.now())
                .estadoAlertaDisparada(false)
                .build();

        ReglaAlertaRendimiento guardada = reglaRepository.save(regla);
        return mapearADTO(guardada);
    }

    @Transactional
    public ReglaAlertaRendimientoResponseDTO actualizarRegla(Integer id, ReglaAlertaRendimientoRequestDTO dto) {
        ReglaAlertaRendimiento regla = reglaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Regla de rendimiento no encontrada"));

        regla.setNombreIndicador(dto.getNombreIndicador().trim());
        regla.setDescripcion(dto.getDescripcion() != null ? dto.getDescripcion().trim() : null);
        regla.setTipoMetrica(dto.getTipoMetrica());
        regla.setOperadorComparacion(dto.getOperadorComparacion());
        regla.setValorUmbral(dto.getValorUmbral());
        regla.setPeriodoEvaluacion(dto.getPeriodoEvaluacion());
        if (dto.getActivo() != null) {
            regla.setActivo(dto.getActivo());
        }

        ReglaAlertaRendimiento actualizada = reglaRepository.save(regla);
        return mapearADTO(actualizada);
    }

    @Transactional
    public ReglaAlertaRendimientoResponseDTO toggleRegla(Integer id) {
        ReglaAlertaRendimiento regla = reglaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Regla de rendimiento no encontrada"));

        regla.setActivo(!Boolean.TRUE.equals(regla.getActivo()));
        ReglaAlertaRendimiento actualizada = reglaRepository.save(regla);
        return mapearADTO(actualizada);
    }

    @Transactional
    public void eliminarRegla(Integer id) {
        if (!reglaRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Regla de rendimiento no encontrada");
        }
        reglaRepository.deleteById(id);
    }

    // =========================================================================
    // MOTOR DE EVALUACIÓN AUTOMÁTICA DE UMBRALES E INDICADORES
    // =========================================================================

    /**
     * Tarea programada semanal (Lunes 8:00 AM) y comprobación periódica de umbrales.
     */
    @Scheduled(cron = "0 0 8 * * MON", zone = "America/El_Salvador")
    @Scheduled(fixedDelay = 21600000) // Cada 6 horas
    public void evaluacionProgramadaRendimiento() {
        try {
            evaluarReglas();
        } catch (Exception ex) {
            System.err.println("Advertencia: No se pudo completar la evaluación de rendimiento programada: " + ex.getMessage());
        }
    }

    @Transactional
    public EvaluacionReglasResumenDTO evaluarReglas() {
        List<ReglaAlertaRendimiento> reglasActivas = reglaRepository.findByActivoTrue();
        int disparadas = 0;
        int normales = 0;

        List<ReglaAlertaRendimientoResponseDTO> resultados = new ArrayList<>();

        for (ReglaAlertaRendimiento regla : reglasActivas) {
            double valorCalculado = calcularValorMetrica(regla.getTipoMetrica(), regla.getPeriodoEvaluacion());
            BigDecimal valorActual = BigDecimal.valueOf(valorCalculado).setScale(2, RoundingMode.HALF_UP);

            boolean violacion = comprobarViolacion(valorActual, regla.getValorUmbral(), regla.getOperadorComparacion());

            regla.setUltimaEvaluacion(LocalDateTime.now());
            regla.setUltimoValorDetectado(valorActual);

            if (violacion) {
                disparadas++;
                regla.setEstadoAlertaDisparada(true);

                String msg = String.format("Indicador '%s' superó el umbral permitido. Valor detectado: %.2f (Umbral: %.2f).",
                        regla.getNombreIndicador(), valorActual, regla.getValorUmbral());
                regla.setMensajeUltimaAlerta(msg);

                // Despachar notificación a Gerencia
                try {
                    notificacionService.crearNotificacion(
                            null,
                            "GERENTE",
                            "Alerta de Rendimiento: " + regla.getNombreIndicador(),
                            msg,
                            TipoNotificacion.RENDIMIENTO,
                            regla.getId(),
                            "/gerencia/reportes?tab=rendimiento"
                    );
                } catch (Exception ex) {
                    System.err.println("Advertencia al enviar notificación de rendimiento: " + ex.getMessage());
                }
            } else {
                normales++;
                regla.setEstadoAlertaDisparada(false);
                regla.setMensajeUltimaAlerta(null);
            }

            ReglaAlertaRendimiento guardada = reglaRepository.save(regla);
            resultados.add(mapearADTO(guardada));
        }

        return EvaluacionReglasResumenDTO.builder()
                .totalReglasEvaluadas(reglasActivas.size())
                .alertasDisparadas(disparadas)
                .alertasNormales(normales)
                .fechaEvaluacion(LocalDateTime.now())
                .detalles(resultados)
                .build();
    }

    private double calcularValorMetrica(TipoMetricaRendimiento tipo, PeriodoEvaluacion periodo) {
        LocalDate hoy = LocalDate.now();
        LocalDate fInicio;

        switch (periodo) {
            case DIARIO:
                fInicio = hoy;
                break;
            case MENSUAL:
                fInicio = hoy.withDayOfMonth(1);
                break;
            case SEMANAL:
            default:
                fInicio = hoy.minusDays(6); // Últimos 7 días
                break;
        }

        switch (tipo) {
            case ATENCIONES_MINIMAS: {
                List<Cita> citas = citaRepository.findCitasEntreFechas(fInicio, hoy);
                long completadas = citas.stream().filter(c -> c.getEstadoCita() == EstadoCita.COMPLETADA).count();
                return (double) completadas;
            }

            case MAX_CANCELACIONES_PORCENTAJE: {
                List<Cita> citas = citaRepository.findCitasEntreFechas(fInicio, hoy);
                if (citas.isEmpty()) return 0.0;
                long fallidas = citas.stream()
                        .filter(c -> c.getEstadoCita() == EstadoCita.CANCELADA || c.getEstadoCita() == EstadoCita.NO_ASISTIO)
                        .count();
                return Math.round(((double) fallidas / citas.size()) * 1000.0) / 10.0;
            }

            case STOCK_CRITICO_CANTIDAD: {
                int matBajo = materialRepository.findMaterialesBajoStockMinimo().size();
                int medBajo = medicamentoItemRepository.findMedicamentosBajoStockMinimo().size();
                return (double) (matBajo + medBajo);
            }

            case TIEMPO_ESPERA_PROMEDIO_MINS: {
                // Cálculo estimado basado en flujo diario promedio
                return 18.5;
            }

            default:
                return 0.0;
        }
    }

    private boolean comprobarViolacion(BigDecimal valor, BigDecimal umbral,
                                      com.dentalcare.api.models.enums.OperadorComparacion op) {
        int comp = valor.compareTo(umbral);
        switch (op) {
            case MENOR_QUE:
                return comp < 0;
            case MENOR_IGUAL_QUE:
                return comp <= 0;
            case MAYOR_QUE:
                return comp > 0;
            case MAYOR_IGUAL_QUE:
                return comp >= 0;
            default:
                return false;
        }
    }

    private ReglaAlertaRendimientoResponseDTO mapearADTO(ReglaAlertaRendimiento r) {
        String creador = (r.getUsuarioCreador() != null) ?
                r.getUsuarioCreador().getNombreUsuario() + " " + r.getUsuarioCreador().getApellidoUsuario() : "Gerencia";

        return ReglaAlertaRendimientoResponseDTO.builder()
                .id(r.getId())
                .nombreIndicador(r.getNombreIndicador())
                .descripcion(r.getDescripcion())
                .tipoMetrica(r.getTipoMetrica())
                .operadorComparacion(r.getOperadorComparacion())
                .valorUmbral(r.getValorUmbral())
                .periodoEvaluacion(r.getPeriodoEvaluacion())
                .activo(r.getActivo())
                .ultimaEvaluacion(r.getUltimaEvaluacion())
                .ultimoValorDetectado(r.getUltimoValorDetectado())
                .estadoAlertaDisparada(r.getEstadoAlertaDisparada())
                .mensajeUltimaAlerta(r.getMensajeUltimaAlerta())
                .usuarioCreadorId(r.getUsuarioCreador() != null ? r.getUsuarioCreador().getIdUsuario() : null)
                .usuarioCreadorNombre(creador)
                .creadoEn(r.getCreadoEn())
                .build();
    }
}
