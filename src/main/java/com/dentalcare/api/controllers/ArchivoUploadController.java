package com.dentalcare.api.controllers;

import com.dentalcare.api.dtos.Consulta.Tratamiento.EvidenciaResponseDTO;
import com.dentalcare.api.models.EvidenciaProcedimiento;
import com.dentalcare.api.models.PlanTratamiento;
import com.dentalcare.api.repositories.EvidenciaProcedimientoRepository;
import com.dentalcare.api.repositories.PlanTratamientoRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/archivos")
@CrossOrigin(origins = "*")
public class ArchivoUploadController {

    private final Path rootUploadDir = Paths.get("uploads").toAbsolutePath().normalize();
    private final PlanTratamientoRepository planTratamientoRepository;
    private final EvidenciaProcedimientoRepository evidenciaProcedimientoRepository;

    public ArchivoUploadController(
            PlanTratamientoRepository planTratamientoRepository,
            EvidenciaProcedimientoRepository evidenciaProcedimientoRepository) {
        this.planTratamientoRepository = planTratamientoRepository;
        this.evidenciaProcedimientoRepository = evidenciaProcedimientoRepository;
        initDirectories();
    }

    private void initDirectories() {
        try {
            Files.createDirectories(rootUploadDir.resolve("avatars"));
            Files.createDirectories(rootUploadDir.resolve("evidencias"));
        } catch (IOException e) {
            System.err.println("No se pudieron crear directorios de carga: " + e.getMessage());
        }
    }

    // 1. Subida de Foto de Perfil de Usuario (Punto 7)
    @PostMapping("/foto-perfil")
    public ResponseEntity<?> subirFotoPerfil(@RequestParam("archivo") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El archivo de imagen no puede estar vacío."));
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Solo se permiten imágenes (JPEG, PNG, WEBP)."));
        }

        try {
            String originalName = sanitizeFilename(file.getOriginalFilename());
            String uniqueName = UUID.randomUUID().toString() + "_" + originalName;
            Path targetPath = rootUploadDir.resolve("avatars").resolve(uniqueName);

            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            String fileUrl = "/api/archivos/ver/avatars/" + uniqueName;
            return ResponseEntity.ok(Map.of(
                    "url", fileUrl,
                    "filename", uniqueName
            ));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al guardar foto: " + e.getMessage()));
        }
    }

    // 2. Subida de Evidencia para Procedimiento Clínico (Punto 7)
    @PostMapping("/evidencias/plan/{idPlan}")
    public ResponseEntity<?> subirEvidenciaProcedimiento(
            @PathVariable Integer idPlan,
            @RequestParam("archivo") MultipartFile file,
            @RequestParam(value = "comentario", required = false) String comentario) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Debe seleccionar un archivo de imagen."));
        }

        PlanTratamiento plan = planTratamientoRepository.findById(idPlan)
                .orElse(null);
        if (plan == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Procedimiento de plan con ID " + idPlan + " no encontrado."));
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().body(Map.of("error", "El archivo debe ser una imagen válida."));
        }

        try {
            String originalName = sanitizeFilename(file.getOriginalFilename());
            String uniqueName = UUID.randomUUID().toString() + "_" + originalName;
            Path targetPath = rootUploadDir.resolve("evidencias").resolve(uniqueName);

            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            String fileUrl = "/api/archivos/ver/evidencias/" + uniqueName;

            EvidenciaProcedimiento evidencia = new EvidenciaProcedimiento();
            evidencia.setPlanTratamiento(plan);
            evidencia.setUrlFoto(fileUrl);
            evidencia.setNombreArchivo(originalName);
            evidencia.setFechaSubida(LocalDateTime.now());
            evidencia.setComentario(comentario != null ? comentario.trim() : null);

            EvidenciaProcedimiento saved = evidenciaProcedimientoRepository.save(evidencia);

            return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al guardar evidencia: " + e.getMessage()));
        }
    }

    // 3. Listar evidencias de un procedimiento
    @GetMapping("/evidencias/plan/{idPlan}")
    public ResponseEntity<List<EvidenciaResponseDTO>> listarEvidenciasPorPlan(@PathVariable Integer idPlan) {
        List<EvidenciaProcedimiento> lista = evidenciaProcedimientoRepository
                .findByPlanTratamiento_IdPlanTratamientoOrderByFechaSubidaDesc(idPlan);
        List<EvidenciaResponseDTO> dtos = lista.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // 4. Eliminar una evidencia
    @DeleteMapping("/evidencias/{idEvidencia}")
    public ResponseEntity<?> eliminarEvidencia(@PathVariable Integer idEvidencia) {
        return evidenciaProcedimientoRepository.findById(idEvidencia)
                .map(evidencia -> {
                    // Intentar borrar archivo físico
                    try {
                        String url = evidencia.getUrlFoto();
                        if (url != null && url.contains("/")) {
                            String filename = url.substring(url.lastIndexOf("/") + 1);
                            Path filePath = rootUploadDir.resolve("evidencias").resolve(filename);
                            Files.deleteIfExists(filePath);
                        }
                    } catch (Exception ignored) {}

                    evidenciaProcedimientoRepository.delete(evidencia);
                    return ResponseEntity.ok(Map.of("mensaje", "Evidencia eliminada correctamente"));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // 5. Servir archivo de imagen seguro
    @GetMapping("/ver/{carpeta}/{filename:.+}")
    public ResponseEntity<Resource> verArchivo(
            @PathVariable String carpeta,
            @PathVariable String filename) {

        if (!"avatars".equals(carpeta) && !"evidencias".equals(carpeta)) {
            return ResponseEntity.badRequest().build();
        }

        // Sanitización contra path traversal
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            return ResponseEntity.badRequest().build();
        }

        try {
            Path filePath = rootUploadDir.resolve(carpeta).resolve(filename).normalize();
            if (!filePath.startsWith(rootUploadDir)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private String sanitizeFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            return "archivo_" + System.currentTimeMillis();
        }
        return Paths.get(filename).getFileName().toString().replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private EvidenciaResponseDTO toDto(EvidenciaProcedimiento ep) {
        return new EvidenciaResponseDTO(
                ep.getIdEvidencia(),
                ep.getPlanTratamiento().getIdPlanTratamiento(),
                ep.getUrlFoto(),
                ep.getNombreArchivo(),
                ep.getFechaSubida(),
                ep.getComentario()
        );
    }
}
