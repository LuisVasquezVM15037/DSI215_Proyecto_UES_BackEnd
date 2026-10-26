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
