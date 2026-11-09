package com.dentalcare.api.services;

import com.dentalcare.api.dtos.Reporte.*;
import com.dentalcare.api.models.Cita;
import com.dentalcare.api.models.Consumo;
import com.dentalcare.api.models.enums.EstadoCita;
import com.dentalcare.api.models.enums.TipoItem;
import com.dentalcare.api.repositories.CitaRepository;
import com.dentalcare.api.repositories.ConsumoRepository;
import com.dentalcare.api.repositories.OdontologoRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para Inteligencia de Negocios, Catálogo y Generación de Reportes Gerenciales.
 * DENTALCARE-70 (PBI-Gerencia-01: Seleccionar Reporte)
 * DENTALCARE-72 (PBI-Gerencia-02: Aplicar Filtros a Reportes)
 * DENTALCARE-74 (PBI-Gerencia-04: Visualizar Resultados de Reportes)
 */
@Service
public class ReporteGerenciaService {

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private ConsumoRepository consumoRepository;

    @Autowired
    private OdontologoRepository odontologoRepository;

    /**
     * Catálogo institucional de reportes disponibles para Gerencia.
     */
    public List<TipoReporteDTO> obtenerTiposReportes() {
        List<TipoReporteDTO> lista = new ArrayList<>();

        lista.add(TipoReporteDTO.builder()
                .id("CONSUMO_MATERIALES")
                .titulo("Consumo de Materiales y Medicamentos Clínicos")
                .descripcion("Análisis cuantitativo de insumos dentales y fármacos consumidos en procedimientos y consultas.")
                .icono("bi-box-seam")
                .categoria("Logística y Almacén")
                .periodoRecomendado("Mensual / Trimestral")
                .build());

        lista.add(TipoReporteDTO.builder()
                .id("NUMERO_ATENCIONES")
                .titulo("Número de Atenciones y Productividad Odontológica")
                .descripcion("Estadísticas de citas realizadas, canceladas, no asistidas y consultas por doctor y especialidad.")
                .icono("bi-calendar-check")
                .categoria("Productividad Clínica")
                .periodoRecomendado("Semanal / Mensual")
                .build());

        return lista;
    }

    /**
     * Motor de filtros y agregación analítica de reportes.
     */
    public ReporteResponseDTO generarReporte(String tipoStr, LocalDate fechaInicio, LocalDate fechaFin,
                                            String categoriaItem, Integer itemId,
                                            Integer odontologoId, String estadoCitaStr) {

        // Fechas por defecto: mes en curso
        LocalDate fInicio = (fechaInicio != null) ? fechaInicio : LocalDate.now().withDayOfMonth(1);
        LocalDate fFin = (fechaFin != null) ? fechaFin : LocalDate.now();
        if (fFin.isBefore(fInicio)) {
            LocalDate tmp = fInicio;
            fInicio = fFin;
            fFin = tmp;
        }

        String tipo = (tipoStr != null) ? tipoStr.trim().toUpperCase() : "CONSUMO_MATERIALES";

        if ("CONSUMO_MATERIALES".equals(tipo)) {
            return procesarReporteConsumos(fInicio, fFin, categoriaItem, itemId);
        } else if ("NUMERO_ATENCIONES".equals(tipo)) {
            return procesarReporteAtenciones(fInicio, fFin, odontologoId, estadoCitaStr);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de reporte desconocido: " + tipoStr);
        }
    }

    // =========================================================================
    // PROCESAMIENTO REPORTE 1: CONSUMO DE MATERIALES
    // =========================================================================

    private ReporteResponseDTO procesarReporteConsumos(LocalDate fInicio, LocalDate fFin,
                                                      String categoriaItem, Integer itemId) {
        LocalDateTime desde = fInicio.atStartOfDay();
        LocalDateTime hasta = fFin.atTime(23, 59, 59);

        List<Consumo> consumos = consumoRepository.findConsumosEntreFechas(desde, hasta);

        // Filtros opcionales
        if (categoriaItem != null && !categoriaItem.equalsIgnoreCase("TODOS")) {
            consumos = consumos.stream()
                    .filter(c -> c.getTipoItem().name().equalsIgnoreCase(categoriaItem))
                    .collect(Collectors.toList());
        }

        if (itemId != null) {
            consumos = consumos.stream()
                    .filter(c -> Objects.equals(c.getItemId(), itemId))
                    .collect(Collectors.toList());
        }

        int totalConsumoItems = consumos.stream().mapToInt(c -> c.getCantidad() != null ? c.getCantidad() : 0).sum();

        // Insumo más consumido
        Map<String, Integer> conteoPorInsumo = new HashMap<>();
        for (Consumo c : consumos) {
            conteoPorInsumo.merge(c.getItemNombre(), c.getCantidad() != null ? c.getCantidad() : 0, Integer::sum);
        }
        String insumoMasConsumido = conteoPorInsumo.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Ninguno registrado");

        // Materiales vs Medicamentos
        int totalMateriales = consumos.stream()
                .filter(c -> c.getTipoItem() == TipoItem.MATERIAL)
                .mapToInt(c -> c.getCantidad() != null ? c.getCantidad() : 0).sum();
        int totalMedicamentos = totalConsumoItems - totalMateriales;
        String categoriaMayor = totalMateriales >= totalMedicamentos ? "Materiales Odontológicos" : "Medicamentos Clínicos";

        // Citas vinculadas
        long citasVinculadas = consumos.stream()
                .map(Consumo::getCita)
                .filter(Objects::nonNull)
                .map(Cita::getIdCitas)
                .distinct()
                .count();

        long dias = Math.max(1, ChronoUnit.DAYS.between(fInicio, fFin) + 1);
        double promedioDiario = Math.round(((double) totalConsumoItems / dias) * 10.0) / 10.0;

        ReporteKpisDTO kpis = ReporteKpisDTO.builder()
                .totalConsumoItems(totalConsumoItems)
                .insumoMasConsumido(insumoMasConsumido)
                .categoriaMayorConsumo(categoriaMayor)
                .totalAtenciones((int) citasVinculadas)
                .promedioAtencionesDiarias(promedioDiario)
                .build();

        // Gráfico Tendencia (agrupado por fecha)
        Map<LocalDate, Integer> tendenciaMap = new TreeMap<>();
        for (LocalDate d = fInicio; !d.isAfter(fFin); d = d.plusDays(1)) {
            tendenciaMap.put(d, 0);
        }
        for (Consumo c : consumos) {
            LocalDate d = c.getFechaConsumo().toLocalDate();
            tendenciaMap.merge(d, c.getCantidad() != null ? c.getCantidad() : 0, Integer::sum);
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM");
        List<ReporteTendenciaDTO> graficoTendencia = new ArrayList<>();
        tendenciaMap.forEach((fecha, cant) -> {
            graficoTendencia.add(ReporteTendenciaDTO.builder()
                    .periodo(fecha.format(dtf))
                    .fecha(fecha.toString())
                    .consumos(cant)
                    .atenciones(0)
                    .build());
        });

        // Gráfico Distribución (Categorías o Top Insumos)
        List<ReporteDistribucionDTO> graficoDistribucion = new ArrayList<>();
        if (totalConsumoItems > 0) {
            double pctMat = Math.round(((double) totalMateriales / totalConsumoItems) * 1000.0) / 10.0;
            double pctMed = Math.round(((double) totalMedicamentos / totalConsumoItems) * 1000.0) / 10.0;

            graficoDistribucion.add(ReporteDistribucionDTO.builder()
                    .etiqueta("Materiales Odontológicos")
                    .cantidad(totalMateriales)
                    .porcentaje(pctMat)
                    .color("#2563EB") // Blue 600
                    .build());

            graficoDistribucion.add(ReporteDistribucionDTO.builder()
                    .etiqueta("Medicamentos Clínicos")
                    .cantidad(totalMedicamentos)
                    .porcentaje(pctMed)
                    .color("#0D9488") // Teal 600
                    .build());
        }

        // Tabla Detalle
        DateTimeFormatter fullDtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        List<ReporteDetalleItemDTO> tablaDetalle = new ArrayList<>();
        for (Consumo c : consumos) {
            String paciente = "-";
            String doctor = "-";
            if (c.getCita() != null) {
                if (c.getCita().getPaciente() != null) {
                    paciente = c.getCita().getPaciente().getNombrePaciente() + " " + c.getCita().getPaciente().getApellidoPaciente();
                }
                if (c.getCita().getOdontologo() != null && c.getCita().getOdontologo().getUsuario() != null) {
                    doctor = c.getCita().getOdontologo().getUsuario().getNombreUsuario() + " " + c.getCita().getOdontologo().getUsuario().getApellidoUsuario();
                }
            }

            tablaDetalle.add(ReporteDetalleItemDTO.builder()
                    .id(c.getId())
                    .fecha(c.getFechaConsumo().format(fullDtf))
                    .concepto(c.getItemNombre())
                    .subconcepto(c.getItemCodigo())
                    .categoria(c.getTipoItem().name())
                    .cantidad(c.getCantidad())
                    .persona1(paciente)
                    .persona2(doctor)
                    .estado(c.getEstado().name())
                    .observaciones(c.getNotas())
                    .build());
        }

        Map<String, Object> filtros = new HashMap<>();
        filtros.put("categoriaItem", categoriaItem != null ? categoriaItem : "TODOS");
        filtros.put("itemId", itemId);

        return ReporteResponseDTO.builder()
                .tipoReporte("CONSUMO_MATERIALES")
                .titulo("Reporte de Consumo de Materiales y Medicamentos Clínicos")
                .subtitulo("Período analizado: " + fInicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " al " + fFin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .fechaInicio(fInicio)
                .fechaFin(fFin)
                .filtrosAplicados(filtros)
                .kpis(kpis)
                .graficoTendencia(graficoTendencia)
                .graficoDistribucion(graficoDistribucion)
                .tablaDetalle(tablaDetalle)
                .totalRegistros(tablaDetalle.size())
                .build();
    }

    // =========================================================================
    // PROCESAMIENTO REPORTE 2: NÚMERO DE ATENCIONES ODONTOLÓGICAS
    // =========================================================================

    private ReporteResponseDTO procesarReporteAtenciones(LocalDate fInicio, LocalDate fFin,
                                                        Integer odontologoId, String estadoCitaStr) {
        List<Cita> citas = citaRepository.findCitasEntreFechas(fInicio, fFin);

        // Filtros opcionales
        if (odontologoId != null) {
            citas = citas.stream()
                    .filter(c -> c.getOdontologo() != null && Objects.equals(c.getOdontologo().getIdOdontologo(), odontologoId))
                    .collect(Collectors.toList());
        }

        if (estadoCitaStr != null && !estadoCitaStr.equalsIgnoreCase("TODOS")) {
            citas = citas.stream()
                    .filter(c -> c.getEstadoCita().name().equalsIgnoreCase(estadoCitaStr))
                    .collect(Collectors.toList());
        }

        int totalCitas = citas.size();
        int totalAtenciones = (int) citas.stream().filter(c -> c.getEstadoCita() == EstadoCita.COMPLETADA).count();
        int totalCanceladas = (int) citas.stream().filter(c -> c.getEstadoCita() == EstadoCita.CANCELADA).count();
        int totalNoAsistio = (int) citas.stream().filter(c -> c.getEstadoCita() == EstadoCita.NO_ASISTIO).count();

        double tasaEfectividad = totalCitas > 0 ?
                Math.round(((double) totalAtenciones / totalCitas) * 1000.0) / 10.0 : 0.0;

        // Odontólogo con mayor número de atenciones concluidas
        Map<String, Integer> atencionesPorDoc = new HashMap<>();
        for (Cita c : citas) {
            if (c.getEstadoCita() == EstadoCita.COMPLETADA && c.getOdontologo() != null && c.getOdontologo().getUsuario() != null) {
                String nombreDoc = "Dr(a). " + c.getOdontologo().getUsuario().getNombreUsuario() + " " + c.getOdontologo().getUsuario().getApellidoUsuario();
                atencionesPorDoc.merge(nombreDoc, 1, Integer::sum);
            }
        }

        String doctorMayorAtencion = atencionesPorDoc.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> e.getKey() + " (" + e.getValue() + " atenciones)")
                .orElse("N/D");

        long dias = Math.max(1, ChronoUnit.DAYS.between(fInicio, fFin) + 1);
        double promedioDiario = Math.round(((double) totalAtenciones / dias) * 10.0) / 10.0;

        ReporteKpisDTO kpis = ReporteKpisDTO.builder()
                .totalAtenciones(totalAtenciones)
                .totalCitasProgramadas(totalCitas)
                .totalCanceladas(totalCanceladas)
                .totalNoAsistio(totalNoAsistio)
                .tasaEfectividad(tasaEfectividad)
                .doctorMayorAtencion(doctorMayorAtencion)
                .tratamientoMasDemandado("Profilaxis y Tratamientos Restaurativos")
                .promedioAtencionesDiarias(promedioDiario)
                .build();

        // Gráfico Tendencia
        Map<LocalDate, int[]> tendenciaMap = new TreeMap<>();
        for (LocalDate d = fInicio; !d.isAfter(fFin); d = d.plusDays(1)) {
            tendenciaMap.put(d, new int[]{0, 0}); // [atenciones, canceladas/noAsistio]
        }
        for (Cita c : citas) {
            LocalDate d = c.getFechaCita();
            int[] arr = tendenciaMap.computeIfAbsent(d, k -> new int[]{0, 0});
            if (c.getEstadoCita() == EstadoCita.COMPLETADA) {
                arr[0]++;
            } else if (c.getEstadoCita() == EstadoCita.CANCELADA || c.getEstadoCita() == EstadoCita.NO_ASISTIO) {
                arr[1]++;
            }
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM");
        List<ReporteTendenciaDTO> graficoTendencia = new ArrayList<>();
        tendenciaMap.forEach((fecha, counts) -> {
            graficoTendencia.add(ReporteTendenciaDTO.builder()
                    .periodo(fecha.format(dtf))
                    .fecha(fecha.toString())
                    .atenciones(counts[0])
                    .canceladas(counts[1])
                    .consumos(0)
                    .build());
        });

        // Gráfico Distribución por Odontólogo
        List<ReporteDistribucionDTO> graficoDistribucion = new ArrayList<>();
        String[] palette = {"#2563EB", "#0D9488", "#7C3AED", "#EA580C", "#059669", "#DB2777"};
        int colorIdx = 0;
        for (Map.Entry<String, Integer> entry : atencionesPorDoc.entrySet()) {
            double pct = totalAtenciones > 0 ?
                    Math.round(((double) entry.getValue() / totalAtenciones) * 1000.0) / 10.0 : 0.0;
            graficoDistribucion.add(ReporteDistribucionDTO.builder()
                    .etiqueta(entry.getKey())
                    .cantidad(entry.getValue())
                    .porcentaje(pct)
                    .color(palette[colorIdx % palette.length])
                    .build());
            colorIdx++;
        }

        // Tabla Detalle
        DateTimeFormatter horaFmt = DateTimeFormatter.ofPattern("hh:mm a");
        DateTimeFormatter fechaFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        List<ReporteDetalleItemDTO> tablaDetalle = new ArrayList<>();
        for (Cita c : citas) {
            String paciente = (c.getPaciente() != null) ?
                    c.getPaciente().getNombrePaciente() + " " + c.getPaciente().getApellidoPaciente() : "-";
            String doctor = "-";
            String esp = "-";
            if (c.getOdontologo() != null) {
                esp = c.getOdontologo().getEspecialidadOdontologo();
                if (c.getOdontologo().getUsuario() != null) {
                    doctor = c.getOdontologo().getUsuario().getNombreUsuario() + " " + c.getOdontologo().getUsuario().getApellidoUsuario();
                }
            }

            String hora = (c.getHoraInicioCita() != null) ? c.getHoraInicioCita().format(horaFmt) : "-";

            tablaDetalle.add(ReporteDetalleItemDTO.builder()
                    .id(c.getIdCitas())
                    .fecha(c.getFechaCita().format(fechaFmt) + " (" + hora + ")")
                    .concepto("Atención Dental: " + paciente)
                    .subconcepto("Especialidad: " + esp)
                    .categoria("ATENCION_CLINICA")
                    .cantidad(1)
                    .persona1(paciente)
                    .persona2(doctor)
                    .estado(c.getEstadoCita().name())
                    .observaciones(c.getMotivoCancelacion())
                    .build());
        }

        Map<String, Object> filtros = new HashMap<>();
        filtros.put("odontologoId", odontologoId);
        filtros.put("estadoCita", estadoCitaStr != null ? estadoCitaStr : "TODOS");

        return ReporteResponseDTO.builder()
                .tipoReporte("NUMERO_ATENCIONES")
                .titulo("Reporte de Productividad y Número de Atenciones Clínicas")
                .subtitulo("Período analizado: " + fInicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " al " + fFin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .fechaInicio(fInicio)
                .fechaFin(fFin)
                .filtrosAplicados(filtros)
                .kpis(kpis)
                .graficoTendencia(graficoTendencia)
                .graficoDistribucion(graficoDistribucion)
                .tablaDetalle(tablaDetalle)
                .totalRegistros(tablaDetalle.size())
                .build();
    }

    // =========================================================================
    // EXPORTACIÓN A HOJA DE CÁLCULO EXCEL (.XLSX)
    // =========================================================================

    public byte[] exportarReporteExcel(String tipoStr, LocalDate fechaInicio, LocalDate fechaFin,
                                       String categoriaItem, Integer itemId,
                                       Integer odontologoId, String estadoCitaStr) {

        ReporteResponseDTO reporte = generarReporte(tipoStr, fechaInicio, fechaFin, categoriaItem, itemId, odontologoId, estadoCitaStr);

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Reporte Gerencial");

            // Estilos
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleFont.setColor(IndexedColors.DARK_BLUE.getIndex());

            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            Font subFont = workbook.createFont();
            subFont.setItalic(true);
            subFont.setFontHeightInPoints((short) 10);
            subFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());

            CellStyle subStyle = workbook.createCellStyle();
            subStyle.setFont(subFont);
            subStyle.setAlignment(HorizontalAlignment.CENTER);

            // Encabezados
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // Borde base
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);

            CellStyle centerStyle = workbook.createCellStyle();
            centerStyle.cloneStyleFrom(cellStyle);
            centerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Fila 0: Título
            Row r0 = sheet.createRow(0);
            Cell cTitle = r0.createCell(0);
            cTitle.setCellValue("DENTALCARE ERP - " + reporte.getTitulo().toUpperCase());
            cTitle.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));

            // Fila 1: Subtítulo
            Row r1 = sheet.createRow(1);
            Cell cSub = r1.createCell(0);
            cSub.setCellValue(reporte.getSubtitulo() + " | Generado: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            cSub.setCellStyle(subStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 7));

            // Fila 3 y 4: Bloque de KPIs
            Row r3 = sheet.createRow(3);
            Cell kpiHeader = r3.createCell(0);
            kpiHeader.setCellValue("INDICADORES CLAVE DE RENDIMIENTO (KPIS DEL PERÍODO)");
            sheet.addMergedRegion(new CellRangeAddress(3, 3, 0, 7));

            Row r4 = sheet.createRow(4);
            if ("CONSUMO_MATERIALES".equals(reporte.getTipoReporte())) {
                r4.createCell(0).setCellValue("Total Unidades Consumidas: " + reporte.getKpis().getTotalConsumoItems());
                r4.createCell(2).setCellValue("Insumo Más Demandado: " + reporte.getKpis().getInsumoMasConsumido());
                r4.createCell(4).setCellValue("Categoría Mayor Consumo: " + reporte.getKpis().getCategoriaMayorConsumo());
                r4.createCell(6).setCellValue("Citas Vinculadas: " + reporte.getKpis().getTotalAtenciones());
            } else {
                r4.createCell(0).setCellValue("Atenciones Concluidas: " + reporte.getKpis().getTotalAtenciones());
                r4.createCell(2).setCellValue("Tasa de Asistencia: " + reporte.getKpis().getTasaEfectividad() + "%");
                r4.createCell(4).setCellValue("Canceladas: " + reporte.getKpis().getTotalCanceladas());
                r4.createCell(6).setCellValue("Doctor con Mayor Atenciones: " + reporte.getKpis().getDoctorMayorAtencion());
            }

            // Fila 6: Encabezados de Tabla Detalle
            String[] headers = {"N°", "Fecha / Hora", "Concepto Principal", "Detalle / Código", "Categoría", "Cantidad", "Paciente", "Doctor / Registro", "Estado"};
            Row hRow = sheet.createRow(6);
            hRow.setHeightInPoints(24);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = hRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Fila 7 en adelante: Filas de datos
            int rowIdx = 7;
            int correlativo = 1;
            for (ReporteDetalleItemDTO d : reporte.getTablaDetalle()) {
                Row row = sheet.createRow(rowIdx++);
                row.setHeightInPoints(20);

                Cell c0 = row.createCell(0);
                c0.setCellValue(correlativo++);
                c0.setCellStyle(centerStyle);

                Cell c1 = row.createCell(1);
                c1.setCellValue(d.getFecha());
                c1.setCellStyle(centerStyle);

                Cell c2 = row.createCell(2);
                c2.setCellValue(d.getConcepto());
                c2.setCellStyle(cellStyle);

                Cell c3 = row.createCell(3);
                c3.setCellValue(d.getSubconcepto());
                c3.setCellStyle(centerStyle);

                Cell c4 = row.createCell(4);
                c4.setCellValue(d.getCategoria());
                c4.setCellStyle(centerStyle);

                Cell c5 = row.createCell(5);
                c5.setCellValue(d.getCantidad());
                c5.setCellStyle(centerStyle);

                Cell c6 = row.createCell(6);
                c6.setCellValue(d.getPersona1());
                c6.setCellStyle(cellStyle);

                Cell c7 = row.createCell(7);
                c7.setCellValue(d.getPersona2());
                c7.setCellStyle(cellStyle);

                Cell c8 = row.createCell(8);
                c8.setCellValue(d.getEstado());
                c8.setCellStyle(centerStyle);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 400);
            }

            workbook.write(bos);
            return bos.toByteArray();

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al generar archivo Excel del reporte", e);
        }
    }
}
