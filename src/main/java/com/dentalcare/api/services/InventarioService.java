package com.dentalcare.api.services;

import com.dentalcare.api.dtos.Inventario.*;
import com.dentalcare.api.models.Cita;
import com.dentalcare.api.models.Consumo;
import com.dentalcare.api.models.Material;
import com.dentalcare.api.models.MedicamentoItem;
import com.dentalcare.api.models.Usuario;
import com.dentalcare.api.models.enums.EstadoConsumo;
import com.dentalcare.api.models.enums.TipoItem;
import com.dentalcare.api.repositories.CitaRepository;
import com.dentalcare.api.repositories.ConsumoRepository;
import com.dentalcare.api.repositories.MaterialRepository;
import com.dentalcare.api.repositories.MedicamentoItemRepository;
import com.dentalcare.api.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.dentalcare.api.models.enums.TipoNotificacion;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class InventarioService {

    private static final Pattern PATTERN_MATERIAL = Pattern.compile("^MT\\d+$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_MEDICAMENTO = Pattern.compile("^MD\\d+$", Pattern.CASE_INSENSITIVE);

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private MedicamentoItemRepository medicamentoItemRepository;

    @Autowired
    private ConsumoRepository consumoRepository;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private NotificacionService notificacionService;

    // ── GESTIÓN DE MATERIALES (DENTALCARE-26) ──────────────────────────────────

    @Transactional
    public MaterialResponseDTO registrarMaterial(MaterialRequestDTO dto) {
        String codigoNormalizado = dto.getCodigo().trim().toUpperCase();
        if (!PATTERN_MATERIAL.matcher(codigoNormalizado).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El código debe cumplir el formato MT seguido de números (ej: MT78).");
        }

        String nombreNormalizado = dto.getNombre().trim();
        if (nombreNormalizado.length() < 5 || nombreNormalizado.length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del material debe tener entre 5 y 100 caracteres.");
        }

        if (materialRepository.existsByCodigo(codigoNormalizado)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El código de material '" + codigoNormalizado + "' ya está registrado.");
        }

        Material material = new Material();
        material.setCodigo(codigoNormalizado);
        material.setNombre(nombreNormalizado);
        material.setUnidadMedida(dto.getUnidadMedida().trim());
        material.setStockActual(dto.getStockActual() != null ? dto.getStockActual() : 0);
        material.setStockMinimo(dto.getStockMinimo() != null ? dto.getStockMinimo() : 5);
        material.setDescripcion(dto.getDescripcion() != null ? dto.getDescripcion().trim() : null);

        Material guardado = materialRepository.save(material);
            verificarYNotificarStockBajo("MATERIAL", material.getCodigo(), material.getNombre(), material.getStockActual(), material.getStockMinimo());
        return mapToMaterialResponseDTO(guardado);
    }

    public List<MaterialResponseDTO> listarMateriales() {
        return materialRepository.findAllByOrderByNombreAsc()
                .stream()
                .map(this::mapToMaterialResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public MaterialResponseDTO actualizarMaterial(Integer id, AjusteStockDTO dto) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Material no encontrado"));

        material.setStockActual(dto.getStockActual());
        if (dto.getStockMinimo() != null) {
            material.setStockMinimo(dto.getStockMinimo());
        }
        material.setActualizadoEn(LocalDateTime.now());
        Material actualizado = materialRepository.save(material);
        return mapToMaterialResponseDTO(actualizado);
    }

    // ── GESTIÓN DE MEDICAMENTOS (DENTALCARE-48) ────────────────────────────────

    @Transactional
    public MedicamentoItemResponseDTO registrarIngresoMedicamento(MedicamentoItemRequestDTO dto) {
        String codigoNormalizado = dto.getCodigo().trim().toUpperCase();
        if (!PATTERN_MEDICAMENTO.matcher(codigoNormalizado).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El código debe cumplir el formato MD seguido de números (ej: MD45).");
        }

        String nombreNormalizado = dto.getNombre().trim();
        if (nombreNormalizado.length() < 5 || nombreNormalizado.length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del medicamento debe tener entre 5 y 100 caracteres.");
        }

        if (dto.getCantidadIngreso() == null || dto.getCantidadIngreso() < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cantidad a ingresar debe ser mayor a cero.");
        }

        MedicamentoItem med = medicamentoItemRepository.findByCodigo(codigoNormalizado).orElse(null);

        if (med != null) {
            // Reabastecimiento de lote / incremento de stock
            med.setStockActual(med.getStockActual() + dto.getCantidadIngreso());
            if (dto.getPresentacion() != null && !dto.getPresentacion().isBlank()) {
                med.setPresentacion(dto.getPresentacion().trim());
            }
            if (dto.getLote() != null && !dto.getLote().isBlank()) {
                med.setLote(dto.getLote().trim());
            }
            if (dto.getFechaVencimiento() != null) {
                med.setFechaVencimiento(dto.getFechaVencimiento());
            }
            if (dto.getStockMinimo() != null) {
                med.setStockMinimo(dto.getStockMinimo());
            }
            med.setActualizadoEn(LocalDateTime.now());
            return mapToMedicamentoResponseDTO(medicamentoItemRepository.save(med));
        }

        // Nuevo medicamento
        MedicamentoItem nuevo = new MedicamentoItem();
        nuevo.setCodigo(codigoNormalizado);
        nuevo.setNombre(nombreNormalizado);
        nuevo.setPresentacion(dto.getPresentacion() != null && !dto.getPresentacion().isBlank()
                ? dto.getPresentacion().trim() : "Unidad estándar");
        nuevo.setLote(dto.getLote() != null ? dto.getLote().trim() : null);
        nuevo.setFechaVencimiento(dto.getFechaVencimiento());
        nuevo.setStockActual(dto.getCantidadIngreso());
        nuevo.setStockMinimo(dto.getStockMinimo() != null ? dto.getStockMinimo() : 5);
        nuevo.setConcentracion(nuevo.getPresentacion());
        nuevo.setComponenteActivo(nuevo.getNombre());
        nuevo.setCostoMedicamento(BigDecimal.ZERO);

        MedicamentoItem guardado = medicamentoItemRepository.save(nuevo);
        return mapToMedicamentoResponseDTO(guardado);
    }

    public List<MedicamentoItemResponseDTO> listarMedicamentos() {
        return medicamentoItemRepository.findAllByOrderByNombreAsc()
                .stream()
                .map(this::mapToMedicamentoResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public MedicamentoItemResponseDTO actualizarMedicamento(Integer id, AjusteStockDTO dto) {
        MedicamentoItem med = medicamentoItemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medicamento no encontrado"));

        med.setStockActual(dto.getStockActual());
        if (dto.getStockMinimo() != null) {
            med.setStockMinimo(dto.getStockMinimo());
        }
        med.setActualizadoEn(LocalDateTime.now());
        MedicamentoItem actualizado = medicamentoItemRepository.save(med);
            verificarYNotificarStockBajo("MEDICAMENTO", med.getCodigo(), med.getNombre(), med.getStockActual(), med.getStockMinimo());
        return mapToMedicamentoResponseDTO(actualizado);
    }

    // ── CONSULTA GENERAL DE INVENTARIO (DENTALCARE-46) ──────────────────────────

    public List<ItemInventarioResumenDTO> consultarInventario(String search, String tipo) {
        List<ItemInventarioResumenDTO> items = new ArrayList<>();
        boolean filtrarMaterial = tipo == null || tipo.equalsIgnoreCase("TODOS") || tipo.equalsIgnoreCase("MATERIAL");
        boolean filtrarMed = tipo == null || tipo.equalsIgnoreCase("TODOS") || tipo.equalsIgnoreCase("MEDICAMENTO");

        String term = (search != null) ? search.trim().toLowerCase() : "";

        if (filtrarMaterial) {
            List<Material> materiales = term.isEmpty()
                    ? materialRepository.findAll()
                    : materialRepository.findByNombreContainingIgnoreCaseOrCodigoContainingIgnoreCase(term, term);

            for (Material m : materiales) {
                items.add(new ItemInventarioResumenDTO(
                        m.getId(),
                        "MATERIAL",
                        m.getCodigo(),
                        m.getNombre(),
                        m.getUnidadMedida(),
                        m.getStockActual(),
                        m.getStockMinimo(),
                        calcularAlerta(m.getStockActual(), m.getStockMinimo()),
                        m.getDescripcion(),
                        m.getActualizadoEn() != null ? m.getActualizadoEn() : m.getCreadoEn()
                ));
            }
        }

        if (filtrarMed) {
            List<MedicamentoItem> meds = term.isEmpty()
                    ? medicamentoItemRepository.findAll()
                    : medicamentoItemRepository.findByNombreContainingIgnoreCaseOrCodigoContainingIgnoreCase(term, term);

            for (MedicamentoItem med : meds) {
                items.add(new ItemInventarioResumenDTO(
                        med.getId(),
                        "MEDICAMENTO",
                        med.getCodigo(),
                        med.getNombre(),
                        med.getPresentacion(),
                        med.getStockActual(),
                        med.getStockMinimo(),
                        calcularAlerta(med.getStockActual(), med.getStockMinimo()),
                        med.getLote() != null ? "Lote: " + med.getLote() : null,
                        med.getActualizadoEn() != null ? med.getActualizadoEn() : med.getCreadoEn()
                ));
            }
        }

        items.sort(Comparator.comparing(ItemInventarioResumenDTO::getNombre, String.CASE_INSENSITIVE_ORDER));
        return items;
    }

    // ── CONFIRMACIÓN Y CONTROL DE CONSUMOS (DENTALCARE-47) ──────────────────────

    public List<ConsumoResponseDTO> listarConsumos(String mes) {
        List<Consumo> consumos;
        if (mes != null && !mes.isBlank()) {
            consumos = consumoRepository.findByMesConsumoOrderByFechaConsumoDesc(mes.trim());
        } else {
            consumos = consumoRepository.findAllByOrderByFechaConsumoDesc();
        }

        return consumos.stream().map(this::mapToConsumoResponseDTO).collect(Collectors.toList());
    }

    @Transactional
    public ConsumoResponseDTO registrarConsumo(ConsumoRequestDTO dto, String username) {
        Usuario usuario = null;
        if (username != null && !username.isBlank()) {
            usuario = usuarioRepository.findByUsernameOrEmail(username).orElse(null);
        }
        if (usuario == null) {
            usuario = usuarioRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No existen usuarios en el sistema."));
        }

        String itemCodigo = "";
        String itemNombre = "";

        if (dto.getTipoItem() == TipoItem.MATERIAL) {
            Material m = materialRepository.findById(dto.getItemId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Material no encontrado"));
            itemCodigo = m.getCodigo();
            itemNombre = m.getNombre();
        } else {
            MedicamentoItem med = medicamentoItemRepository.findById(dto.getItemId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medicamento no encontrado"));
            itemCodigo = med.getCodigo();
            itemNombre = med.getNombre();
        }

        Cita cita = null;
        if (dto.getCitaId() != null) {
            cita = citaRepository.findById(dto.getCitaId()).orElse(null);
        }

        String mesConsumo = dto.getMesConsumo();
        if (mesConsumo == null || mesConsumo.isBlank()) {
            mesConsumo = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }

        Consumo consumo = new Consumo();
        consumo.setTipoItem(dto.getTipoItem());
        consumo.setItemId(dto.getItemId());
        consumo.setItemCodigo(itemCodigo);
        consumo.setItemNombre(itemNombre);
        consumo.setCita(cita);
        consumo.setCantidad(dto.getCantidad());
        consumo.setMesConsumo(mesConsumo);
        consumo.setEstado(EstadoConsumo.PENDIENTE);
        consumo.setFechaConsumo(LocalDateTime.now());
        consumo.setUsuarioRegistro(usuario);
        consumo.setNotas(dto.getNotas());

        Consumo guardado = consumoRepository.save(consumo);
        return mapToConsumoResponseDTO(guardado);
    }

    @Transactional
    public ConsumoResponseDTO modificarConsumo(Integer id, ConsumoRequestDTO dto) {
        Consumo consumo = consumoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Consumo no encontrado"));

        if (consumo.getEstado() != EstadoConsumo.PENDIENTE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede modificar un consumo que ya ha sido " + consumo.getEstado() + ".");
        }

        if (dto.getCantidad() != null && dto.getCantidad() > 0) {
            consumo.setCantidad(dto.getCantidad());
        }

        if (dto.getNotas() != null) {
            consumo.setNotas(dto.getNotas().trim());
        }

        Consumo actualizado = consumoRepository.save(consumo);
        return mapToConsumoResponseDTO(actualizado);
    }

    @Transactional
    public ConsumoResponseDTO confirmarConsumo(Integer id) {
        Consumo consumo = consumoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Consumo no encontrado"));

        if (consumo.getEstado() != EstadoConsumo.PENDIENTE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El consumo ya se encuentra en estado " + consumo.getEstado() + ".");
        }

        // Verificación y descuento atómico de stock
        if (consumo.getTipoItem() == TipoItem.MATERIAL) {
            Material material = materialRepository.findById(consumo.getItemId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El material vinculado ya no existe en el catálogo."));

            if (material.getStockActual() < consumo.getCantidad()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Stock insuficiente de " + material.getNombre() + " (" + material.getCodigo() +
                        "). Disponible: " + material.getStockActual() + ", Requerido: " + consumo.getCantidad());
            }

            material.setStockActual(material.getStockActual() - consumo.getCantidad());
            material.setActualizadoEn(LocalDateTime.now());
            materialRepository.save(material);
        } else {
            MedicamentoItem med = medicamentoItemRepository.findById(consumo.getItemId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El medicamento vinculado ya no existe en el catálogo."));

            if (med.getStockActual() < consumo.getCantidad()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Stock insuficiente de " + med.getNombre() + " (" + med.getCodigo() +
                        "). Disponible: " + med.getStockActual() + ", Requerido: " + consumo.getCantidad());
            }

            med.setStockActual(med.getStockActual() - consumo.getCantidad());
            med.setActualizadoEn(LocalDateTime.now());
            medicamentoItemRepository.save(med);
        }

        consumo.setEstado(EstadoConsumo.CONFIRMADO);
        Consumo confirmado = consumoRepository.save(consumo);
        return mapToConsumoResponseDTO(confirmado);
    }

    // ── MAPPERS AUXILIARES ─────────────────────────────────────────────────────

    private String calcularAlerta(Integer stockActual, Integer stockMinimo) {
        int act = (stockActual != null) ? stockActual : 0;
        int min = (stockMinimo != null) ? stockMinimo : 5;
        if (act <= 0) return "CRITICO";
        if (act <= min) return "BAJO";
        return "OPTIMO";
    }

    
    // =========================================================================
    // ALERTAS DE STOCK OPERATIVAS Y AUDITORÍA GERENCIAL (DENTALCARE-49 & DENTALCARE-73)
    // =========================================================================

    public List<AlertaStockDTO> obtenerAlertasStock(String orden) {
        List<AlertaStockDTO> alertas = new ArrayList<>();

        // 1. Materiales bajo stock mínimo
        List<Material> materialesBajo = materialRepository.findMaterialesBajoStockMinimo();
        for (Material m : materialesBajo) {
            int stockAct = (m.getStockActual() != null) ? m.getStockActual() : 0;
            int stockMin = (m.getStockMinimo() != null) ? m.getStockMinimo() : 5;
            int deficit = Math.max(0, stockMin - stockAct);
            double cobertura = stockMin > 0 ? Math.round(((double) stockAct / stockMin) * 1000.0) / 10.0 : 0.0;

            String urgencia;
            if (stockAct <= 0) {
                urgencia = "CRITICO";
            } else if (cobertura <= 50.0) {
                urgencia = "ALTA";
            } else {
                urgencia = "MEDIA";
            }

            int diasEst = stockAct <= 0 ? 0 : Math.max(1, (int) Math.ceil((double) stockAct * 1.5));

            alertas.add(AlertaStockDTO.builder()
                    .id(m.getId())
                    .tipo("MATERIAL")
                    .codigo(m.getCodigo())
                    .nombre(m.getNombre())
                    .unidadMedida(m.getUnidadMedida())
                    .stockActual(stockAct)
                    .stockMinimo(stockMin)
                    .deficit(deficit)
                    .porcentajeCobertura(cobertura)
                    .nivelUrgencia(urgencia)
                    .diasEstimadosAgotamiento(diasEst)
                    .lote(null)
                    .fechaVencimiento(null)
                    .build());
        }

        // 2. Medicamentos bajo stock mínimo
        List<MedicamentoItem> medicamentosBajo = medicamentoItemRepository.findMedicamentosBajoStockMinimo();
        for (MedicamentoItem med : medicamentosBajo) {
            int stockAct = (med.getStockActual() != null) ? med.getStockActual() : 0;
            int stockMin = (med.getStockMinimo() != null) ? med.getStockMinimo() : 5;
            int deficit = Math.max(0, stockMin - stockAct);
            double cobertura = stockMin > 0 ? Math.round(((double) stockAct / stockMin) * 1000.0) / 10.0 : 0.0;

            String urgencia;
            if (stockAct <= 0) {
                urgencia = "CRITICO";
            } else if (cobertura <= 50.0) {
                urgencia = "ALTA";
            } else {
                urgencia = "MEDIA";
            }

            int diasEst = stockAct <= 0 ? 0 : Math.max(1, (int) Math.ceil((double) stockAct * 1.2));

            alertas.add(AlertaStockDTO.builder()
                    .id(med.getId())
                    .tipo("MEDICAMENTO")
                    .codigo(med.getCodigo())
                    .nombre(med.getNombre())
                    .unidadMedida(med.getPresentacion())
                    .stockActual(stockAct)
                    .stockMinimo(stockMin)
                    .deficit(deficit)
                    .porcentajeCobertura(cobertura)
                    .nivelUrgencia(urgencia)
                    .diasEstimadosAgotamiento(diasEst)
                    .lote(med.getLote())
                    .fechaVencimiento(med.getFechaVencimiento())
                    .build());
        }

        // Ordenamiento
        String criterio = (orden != null) ? orden.trim().toLowerCase() : "urgencia_desc";
        switch (criterio) {
            case "deficit_desc":
                alertas.sort(Comparator.comparing(AlertaStockDTO::getDeficit).reversed());
                break;
            case "nombre_asc":
                alertas.sort(Comparator.comparing(a -> a.getNombre().toLowerCase()));
                break;
            case "cobertura_asc":
                alertas.sort(Comparator.comparing(AlertaStockDTO::getPorcentajeCobertura));
                break;
            case "urgencia_desc":
            default:
                alertas.sort((a, b) -> {
                    int pesoA = pesoUrgencia(a.getNivelUrgencia());
                    int pesoB = pesoUrgencia(b.getNivelUrgencia());
                    if (pesoA != pesoB) {
                        return Integer.compare(pesoB, pesoA);
                    }
                    return Double.compare(a.getPorcentajeCobertura(), b.getPorcentajeCobertura());
                });
                break;
        }

        return alertas;
    }

    private int pesoUrgencia(String nivel) {
        if ("CRITICO".equalsIgnoreCase(nivel)) return 3;
        if ("ALTA".equalsIgnoreCase(nivel)) return 2;
        return 1;
    }

    public List<AlertaStockDTO> obtenerTop3Criticos() {
        return obtenerAlertasStock("cobertura_asc").stream()
                .limit(3)
                .collect(Collectors.toList());
    }

    public AlertaStockResumenDTO obtenerResumenAlertas() {
        List<AlertaStockDTO> todas = obtenerAlertasStock("urgencia_desc");
        int criticos = (int) todas.stream().filter(a -> "CRITICO".equalsIgnoreCase(a.getNivelUrgencia())).count();
        int altos = (int) todas.stream().filter(a -> "ALTA".equalsIgnoreCase(a.getNivelUrgencia())).count();
        int medios = (int) todas.stream().filter(a -> "MEDIA".equalsIgnoreCase(a.getNivelUrgencia())).count();

        List<AlertaStockDTO> top3 = todas.stream()
                .sorted(Comparator.comparing(AlertaStockDTO::getPorcentajeCobertura))
                .limit(3)
                .collect(Collectors.toList());

        return AlertaStockResumenDTO.builder()
                .totalAlertas(todas.size())
                .criticos(criticos)
                .altos(altos)
                .medios(medios)
                .top3(top3)
                .alertas(todas)
                .build();
    }

    public byte[] generarReporteExcelAlertas() {
        List<AlertaStockDTO> alertas = obtenerAlertasStock("urgencia_desc");

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Alertas de Stock");

            // Estilos
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleFont.setColor(IndexedColors.DARK_BLUE.getIndex());

            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            Font subtitleFont = workbook.createFont();
            subtitleFont.setItalic(true);
            subtitleFont.setFontHeightInPoints((short) 10);
            subtitleFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());

            CellStyle subtitleStyle = workbook.createCellStyle();
            subtitleStyle.setFont(subtitleFont);
            subtitleStyle.setAlignment(HorizontalAlignment.CENTER);

            // Estilo Encabezado de Columnas (Azul corporativo DentalCare)
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // Estilos para datos
            CellStyle baseStyle = workbook.createCellStyle();
            baseStyle.setBorderTop(BorderStyle.THIN);
            baseStyle.setBorderBottom(BorderStyle.THIN);
            baseStyle.setBorderLeft(BorderStyle.THIN);
            baseStyle.setBorderRight(BorderStyle.THIN);

            CellStyle centerStyle = workbook.createCellStyle();
            centerStyle.cloneStyleFrom(baseStyle);
            centerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Estilo CRITICO
            Font criticoFont = workbook.createFont();
            criticoFont.setBold(true);
            criticoFont.setColor(IndexedColors.DARK_RED.getIndex());
            CellStyle criticoStyle = workbook.createCellStyle();
            criticoStyle.cloneStyleFrom(centerStyle);
            criticoStyle.setFont(criticoFont);
            criticoStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
            criticoStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Estilo ALTA
            Font altaFont = workbook.createFont();
            altaFont.setBold(true);
            altaFont.setColor(IndexedColors.BROWN.getIndex());
            CellStyle altaStyle = workbook.createCellStyle();
            altaStyle.cloneStyleFrom(centerStyle);
            altaStyle.setFont(altaFont);
            altaStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            altaStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Estilo MEDIA
            Font mediaFont = workbook.createFont();
            mediaFont.setColor(IndexedColors.DARK_BLUE.getIndex());
            CellStyle mediaStyle = workbook.createCellStyle();
            mediaStyle.cloneStyleFrom(centerStyle);
            mediaStyle.setFont(mediaFont);

            // TÍTULO (Fila 0 y 1)
            Row row0 = sheet.createRow(0);
            Cell cellTitle = row0.createCell(0);
            cellTitle.setCellValue("DENTALCARE ERP - REPORTE DE ALERTAS Y STOCK CRÍTICO");
            cellTitle.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 10));

            Row row1 = sheet.createRow(1);
            Cell cellSub = row1.createCell(0);
            String fechaStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            cellSub.setCellValue("Generado el: " + fechaStr + " | Cobertura de Insumos Bajo Stock Mínimo");
            cellSub.setCellStyle(subtitleStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 10));

            // ENCABEZADOS DE COLUMNA (Fila 3)
            String[] headers = {
                    "N°", "Tipo", "Código", "Nombre del Insumo", "Unidad / Presentación",
                    "Stock Actual", "Stock Mínimo", "Déficit", "Cobertura", "Nivel Urgencia", "Días Est. Agotamiento"
            };
            Row headerRow = sheet.createRow(3);
            headerRow.setHeightInPoints(24);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // DATOS (Fila 4 en adelante)
            int rowIndex = 4;
            int correlativo = 1;
            for (AlertaStockDTO a : alertas) {
                Row row = sheet.createRow(rowIndex++);
                row.setHeightInPoints(20);

                Cell c0 = row.createCell(0);
                c0.setCellValue(correlativo++);
                c0.setCellStyle(centerStyle);

                Cell c1 = row.createCell(1);
                c1.setCellValue(a.getTipo());
                c1.setCellStyle(centerStyle);

                Cell c2 = row.createCell(2);
                c2.setCellValue(a.getCodigo());
                c2.setCellStyle(centerStyle);

                Cell c3 = row.createCell(3);
                c3.setCellValue(a.getNombre());
                c3.setCellStyle(baseStyle);

                Cell c4 = row.createCell(4);
                c4.setCellValue(a.getUnidadMedida() != null ? a.getUnidadMedida() : "-");
                c4.setCellStyle(centerStyle);

                Cell c5 = row.createCell(5);
                c5.setCellValue(a.getStockActual());
                c5.setCellStyle(centerStyle);

                Cell c6 = row.createCell(6);
                c6.setCellValue(a.getStockMinimo());
                c6.setCellStyle(centerStyle);

                Cell c7 = row.createCell(7);
                c7.setCellValue(a.getDeficit());
                c7.setCellStyle(centerStyle);

                Cell c8 = row.createCell(8);
                c8.setCellValue(String.format(Locale.US, "%.1f%%", a.getPorcentajeCobertura()));
                c8.setCellStyle(centerStyle);

                Cell c9 = row.createCell(9);
                c9.setCellValue(a.getNivelUrgencia());
                if ("CRITICO".equalsIgnoreCase(a.getNivelUrgencia())) {
                    c9.setCellStyle(criticoStyle);
                } else if ("ALTA".equalsIgnoreCase(a.getNivelUrgencia())) {
                    c9.setCellStyle(altaStyle);
                } else {
                    c9.setCellStyle(mediaStyle);
                }

                Cell c10 = row.createCell(10);
                c10.setCellValue(a.getDiasEstimadosAgotamiento() != null ? a.getDiasEstimadosAgotamiento() + " días" : "N/D");
                c10.setCellStyle(centerStyle);
            }

            // Ajuste automático de columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 500);
            }

            workbook.write(bos);
            return bos.toByteArray();

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al generar el reporte Excel de alertas", e);
        }
    }

    public void verificarYNotificarStockBajo(String tipo, String codigo, String nombre, int stockActual, int stockMinimo) {
        if (stockActual <= stockMinimo) {
            String urgencia = (stockActual <= 0) ? "CRÍTICO (Agotado)" : "Stock Bajo";
            String msg = String.format("El %s '%s' (%s) presenta %d unidades (Mínimo: %d). Nivel: %s.",
                    tipo.toLowerCase(), nombre, codigo, stockActual, stockMinimo, urgencia);

            try {
                // Notificar al Asistente Odontólogo
                notificacionService.crearNotificacion(
                        null,
                        "ASISTENTEODONTOLOGO",
                        "Alerta de Stock: " + nombre,
                        msg,
                        TipoNotificacion.STOCK_CRITICO,
                        null,
                        "/inventario"
                );

                // Notificar a Gerencia
                notificacionService.crearNotificacion(
                        null,
                        "GERENTE",
                        "Supervisión de Stock: " + nombre,
                        msg,
                        TipoNotificacion.STOCK_CRITICO,
                        null,
                        "/gerencia/stock-critico"
                );
            } catch (Exception ex) {
                System.err.println("Advertencia: No se pudo emitir la notificación de stock bajo: " + ex.getMessage());
            }
        }
    }

    private MaterialResponseDTO mapToMaterialResponseDTO(Material m) {
        return new MaterialResponseDTO(
                m.getId(),
                m.getCodigo(),
                m.getNombre(),
                m.getUnidadMedida(),
                m.getStockActual(),
                m.getStockMinimo(),
                m.getDescripcion(),
                m.getCreadoEn(),
                m.getActualizadoEn(),
                calcularAlerta(m.getStockActual(), m.getStockMinimo())
        );
    }

    private MedicamentoItemResponseDTO mapToMedicamentoResponseDTO(MedicamentoItem m) {
        return new MedicamentoItemResponseDTO(
                m.getId(),
                m.getCodigo(),
                m.getNombre(),
                m.getPresentacion(),
                m.getLote(),
                m.getFechaVencimiento(),
                m.getStockActual(),
                m.getStockMinimo(),
                m.getCreadoEn(),
                m.getActualizadoEn(),
                calcularAlerta(m.getStockActual(), m.getStockMinimo())
        );
    }

    private ConsumoResponseDTO mapToConsumoResponseDTO(Consumo c) {
        String paciente = null;
        String odontologo = null;
        if (c.getCita() != null) {
            if (c.getCita().getPaciente() != null) {
                paciente = c.getCita().getPaciente().getNombrePaciente() + " " + c.getCita().getPaciente().getApellidoPaciente();
            }
            if (c.getCita().getOdontologo() != null && c.getCita().getOdontologo().getUsuario() != null) {
                odontologo = c.getCita().getOdontologo().getUsuario().getNombreUsuario() + " " + c.getCita().getOdontologo().getUsuario().getApellidoUsuario();
            }
        }

        Integer stockDisponible = null;
        if (c.getTipoItem() == TipoItem.MATERIAL) {
            materialRepository.findById(c.getItemId()).ifPresent(m -> {});
            stockDisponible = materialRepository.findById(c.getItemId()).map(Material::getStockActual).orElse(0);
        } else {
            stockDisponible = medicamentoItemRepository.findById(c.getItemId()).map(MedicamentoItem::getStockActual).orElse(0);
        }

        String userReg = null;
        if (c.getUsuarioRegistro() != null) {
            userReg = c.getUsuarioRegistro().getNombreUsuario() + " " + c.getUsuarioRegistro().getApellidoUsuario();
        }

        return new ConsumoResponseDTO(
                c.getId(),
                c.getTipoItem(),
                c.getItemId(),
                c.getItemCodigo(),
                c.getItemNombre(),
                c.getCita() != null ? c.getCita().getIdCitas() : null,
                paciente,
                odontologo,
                c.getCantidad(),
                c.getMesConsumo(),
                c.getEstado(),
                c.getFechaConsumo(),
                c.getUsuarioRegistro() != null ? c.getUsuarioRegistro().getIdUsuario() : null,
                userReg,
                c.getNotas(),
                stockDisponible
        );
    }
}
