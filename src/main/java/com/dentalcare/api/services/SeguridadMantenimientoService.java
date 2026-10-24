package com.dentalcare.api.services;

import com.dentalcare.api.dtos.Seguridad.*;
import com.dentalcare.api.models.PasswordResetToken;
import com.dentalcare.api.models.Respaldo;
import com.dentalcare.api.models.Usuario;
import com.dentalcare.api.repositories.PasswordResetRepository;
import com.dentalcare.api.repositories.RespaldoRepository;
import com.dentalcare.api.repositories.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.DataSource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class SeguridadMantenimientoService {

    private static final Logger log = LoggerFactory.getLogger(SeguridadMantenimientoService.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final RespaldoRepository respaldoRepository;
    private final PasswordEncoder passwordEncoder;
    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public SeguridadMantenimientoService(UsuarioRepository usuarioRepository,
                                         PasswordResetRepository passwordResetRepository,
                                         RespaldoRepository respaldoRepository,
                                         PasswordEncoder passwordEncoder,
                                         DataSource dataSource,
                                         JdbcTemplate jdbcTemplate) {
        this.usuarioRepository = usuarioRepository;
        this.passwordResetRepository = passwordResetRepository;
        this.respaldoRepository = respaldoRepository;
        this.passwordEncoder = passwordEncoder;
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    // =========================================================================
    //  DENTALCARE-93: RESTABLECIMIENTO DE CONTRASEÑAS Y CAMBIO OBLIGATORIO
    // =========================================================================

    @Transactional
    public RestablecerPasswordResponseDTO restablecerPassword(Integer idUsuario, String adminUsername) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + idUsuario));

        // 1. Invalidar tokens previos no utilizados para este usuario
        List<PasswordResetToken> tokensPrevios = passwordResetRepository.findByUsuarioAndUtilizadoFalse(usuario);
        for (PasswordResetToken t : tokensPrevios) {
            t.setUtilizado(true);
        }
        passwordResetRepository.saveAll(tokensPrevios);

        // 2. Generar clave provisional de 8 caracteres y token criptográfico único
        int randomCode = 100000 + new Random().nextInt(900000);
        String claveProvisional = "Temp#" + randomCode;
        String token = UUID.randomUUID().toString().replace("-", "") + Long.toHexString(System.currentTimeMillis());
        LocalDateTime expiraEn = LocalDateTime.now().plusHours(24);

        // 3. Forzar cambio de clave en la cuenta de usuario y setear clave provisional hasheada
        usuario.setDebeCambiarPassword(true);
        usuario.setPassworUsuario(passwordEncoder.encode(claveProvisional));
        usuarioRepository.save(usuario);

        // 4. Guardar registro en base de datos
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUsuario(usuario);
        resetToken.setToken(token);
        resetToken.setClaveTemporal(claveProvisional);
        resetToken.setExpiraEn(expiraEn);
        resetToken.setUtilizado(false);
        resetToken.setCreadoEn(LocalDateTime.now());
        passwordResetRepository.save(resetToken);

        // 5. Preparar notificación institucional (correo)
        String enlaceRestablecimiento = "http://localhost:5173/reset-password?token=" + token;
        String mensajeNotificacion = String.format(
                "Notificación enviada a %s: Estimado(a) %s %s, su contraseña ha sido restablecida. " +
                "Su contraseña provisional es: '%s'. Al ingresar al sistema se le solicitará actualizarla obligatoriamente. " +
                "También puede acceder mediante el enlace directo: %s (Válido por 24 horas).",
                usuario.getEmailUsuario(), usuario.getNombreUsuario(), usuario.getApellidoUsuario(),
                claveProvisional, enlaceRestablecimiento
        );

        log.info("[SEGURIDAD AUDITORIA] Restablecimiento de clave para usuario '{}' por admin '{}'. Token generado: {}",
                usuario.getUsernameUsuario(), adminUsername, token);

        return RestablecerPasswordResponseDTO.builder()
                .idUsuario(usuario.getIdUsuario())
                .username(usuario.getUsernameUsuario())
                .email(usuario.getEmailUsuario())
                .claveProvisional(claveProvisional)
                .token(token)
                .expiraEn(expiraEn)
                .enlaceRestablecimiento(enlaceRestablecimiento)
                .mensajeNotificacion(mensajeNotificacion)
                .build();
    }

    public Map<String, Object> validarToken(String token) {
        PasswordResetToken resetToken = passwordResetRepository.findByTokenAndUtilizadoFalse(token)
                .orElseThrow(() -> new RuntimeException("El token proporcionado no existe o ya fue utilizado."));

        if (resetToken.getExpiraEn().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("El token de restablecimiento ha expirado (validez: 24 horas). Solicite uno nuevo.");
        }

        Usuario usuario = resetToken.getUsuario();
        return Map.of(
                "valido", true,
                "username", usuario.getUsernameUsuario(),
                "email", usuario.getEmailUsuario(),
                "nombreCompleto", usuario.getNombreUsuario() + " " + usuario.getApellidoUsuario()
        );
    }

    @Transactional
    public void cambiarPasswordObligatorio(CambiarPasswordDTO dto) {
        if (dto.getPasswordNueva() == null || dto.getPasswordNueva().trim().length() < 6) {
            throw new RuntimeException("La nueva contraseña debe tener al menos 6 caracteres.");
        }
        if (dto.getConfirmarPasswordNueva() != null && !dto.getPasswordNueva().equals(dto.getConfirmarPasswordNueva())) {
            throw new RuntimeException("Las contraseñas no coinciden. Verifique e intente nuevamente.");
        }

        Usuario usuario = null;
        PasswordResetToken resetToken = null;

        if (dto.getToken() != null && !dto.getToken().isBlank()) {
            resetToken = passwordResetRepository.findByTokenAndUtilizadoFalse(dto.getToken().trim())
                    .orElseThrow(() -> new RuntimeException("Token de restablecimiento inválido o ya utilizado."));

            if (resetToken.getExpiraEn().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("El token de restablecimiento ha expirado. Contacte al Administrador.");
            }
            usuario = resetToken.getUsuario();
        } else if (dto.getUsername() != null && !dto.getUsername().isBlank()) {
            usuario = usuarioRepository.findByUsernameOrEmail(dto.getUsername().trim())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

            if (dto.getPasswordActual() != null && !dto.getPasswordActual().isBlank()) {
                if (!passwordEncoder.matches(dto.getPasswordActual(), usuario.getPassworUsuario())) {
                    throw new RuntimeException("La contraseña actual o provisional ingresada es incorrecta.");
                }
            }
        } else {
            throw new RuntimeException("Debe proporcionar un token o el nombre de usuario para cambiar la contraseña.");
        }

        // Actualizar contraseña y quitar bandera de cambio obligatorio
        usuario.setPassworUsuario(passwordEncoder.encode(dto.getPasswordNueva()));
        usuario.setDebeCambiarPassword(false);
        usuarioRepository.save(usuario);

        if (resetToken != null) {
            resetToken.setUtilizado(true);
            passwordResetRepository.save(resetToken);
        }

        log.info("[SEGURIDAD AUDITORIA] Contraseña actualizada exitosamente para el usuario '{}'.", usuario.getUsernameUsuario());
    }

    // =========================================================================
    //  DENTALCARE-53: EJECUCIÓN DE RESPALDOS DE BASE DE DATOS
    // =========================================================================

    @Transactional
    public RespaldoResponseDTO generarRespaldo(String adminUsername) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String nombreArchivo = "backup_dentalcare_" + timestamp + ".sql";

        File backupDir = new File("backups");
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }

        File backupFile = new File(backupDir, nombreArchivo);

        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(backupFile), StandardCharsets.UTF_8))) {
            writer.println("-- ========================================================");
            writer.println("-- DENTALCARE CLINICAL ERP - VOLCADO DE BASE DE DATOS");
            writer.println("-- Fecha de Generación: " + LocalDateTime.now());
            writer.println("-- Generado por Administrador TI: " + (adminUsername != null ? adminUsername : "Sistema"));
            writer.println("-- ========================================================\n");

            writer.println("SET FOREIGN_KEY_CHECKS = 0;");
            writer.println("SET NAMES utf8mb4;\n");

            try (Connection conn = dataSource.getConnection()) {
                DatabaseMetaData metaData = conn.getMetaData();
                String catalog = conn.getCatalog();

                List<String> tables = new ArrayList<>();
                try (ResultSet rs = metaData.getTables(catalog, null, "%", new String[]{"TABLE"})) {
                    while (rs.next()) {
                        String tableName = rs.getString("TABLE_NAME");
                        // Excluimos tablas temporales si hubiere
                        tables.add(tableName);
                    }
                }

                // Para cada tabla, extraer DDL y registros
                for (String table : tables) {
                    writer.println("-- --------------------------------------------------------");
                    writer.println("-- Estructura y datos para la tabla `" + table + "`");
                    writer.println("-- --------------------------------------------------------");
                    writer.println("DROP TABLE IF EXISTS `" + table + "`;");

                    try (Statement stmt = conn.createStatement();
                         ResultSet rsDdl = stmt.executeQuery("SHOW CREATE TABLE `" + table + "`")) {
                        if (rsDdl.next()) {
                            String createSql = rsDdl.getString(2);
                            writer.println(createSql + ";\n");
                        }
                    }

                    // Exportar datos
                    try (Statement stmtData = conn.createStatement();
                         ResultSet rsData = stmtData.executeQuery("SELECT * FROM `" + table + "`")) {

                        ResultSetMetaData rsmd = rsData.getMetaData();
                        int columnCount = rsmd.getColumnCount();

                        while (rsData.next()) {
                            StringBuilder insertSql = new StringBuilder();
                            insertSql.append("INSERT INTO `").append(table).append("` VALUES (");

                            for (int i = 1; i <= columnCount; i++) {
                                Object val = rsData.getObject(i);
                                if (val == null) {
                                    insertSql.append("NULL");
                                } else if (val instanceof Number || val instanceof Boolean) {
                                    insertSql.append(val);
                                } else {
                                    String strVal = val.toString();
                                    strVal = strVal.replace("\\", "\\\\").replace("'", "\\'");
                                    insertSql.append("'").append(strVal).append("'");
                                }

                                if (i < columnCount) {
                                    insertSql.append(", ");
                                }
                            }
                            insertSql.append(");");
                            writer.println(insertSql.toString());
                        }
                    }
                    writer.println();
                }

                writer.println("SET FOREIGN_KEY_CHECKS = 1;");
                writer.println("\n-- FIN DEL RESPALDO DENTALCARE");
            }

            long fileSizeBytes = backupFile.length();

            Usuario adminUser = null;
            if (adminUsername != null) {
                adminUser = usuarioRepository.findByUsernameOrEmail(adminUsername).orElse(null);
            }

            Respaldo respaldo = new Respaldo();
            respaldo.setNombreArchivo(nombreArchivo);
            respaldo.setRutaAlmacenamiento(backupFile.getAbsolutePath());
            respaldo.setTamanioBytes(fileSizeBytes);
            respaldo.setEstado("EXITOSO");
            respaldo.setUsuario(adminUser);
            respaldo.setCreadoEn(LocalDateTime.now());

            respaldo = respaldoRepository.save(respaldo);

            log.info("[BACKUP EXITOSO] Respaldo generado: {} ({} bytes)", nombreArchivo, fileSizeBytes);

            return mapToRespaldoDTO(respaldo);

        } catch (Exception e) {
            log.error("[BACKUP ERROR] Falló la generación del respaldo: ", e);
            throw new RuntimeException("Error al generar respaldo de la base de datos: " + e.getMessage(), e);
        }
    }

    public List<RespaldoResponseDTO> listarRespaldos() {
        return respaldoRepository.findAllByOrderByCreadoEnDesc().stream()
                .map(this::mapToRespaldoDTO)
                .toList();
    }

    public File obtenerArchivoRespaldo(Integer id) {
        Respaldo respaldo = respaldoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Respaldo no encontrado con ID: " + id));

        File file = new File(respaldo.getRutaAlmacenamiento());
        if (!file.exists()) {
            throw new RuntimeException("El archivo físico del respaldo no se encuentra en el servidor: " + respaldo.getNombreArchivo());
        }
        return file;
    }

    // =========================================================================
    //  DENTALCARE-64: RESTAURACIÓN SEGURA DE BASE DE DATOS
    // =========================================================================

    @Transactional
    public void restaurarRespaldo(Integer id, RestaurarRespaldoRequestDTO dto, String authenticatedAdminUsername) {
        validarConfirmacionAdmin(dto, authenticatedAdminUsername);

        Respaldo respaldo = respaldoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Respaldo no encontrado con ID: " + id));

        File file = new File(respaldo.getRutaAlmacenamiento());
        if (!file.exists()) {
            throw new RuntimeException("El archivo de respaldo seleccionado no existe en disco.");
        }

        ejecutarScriptSql(file);
        log.info("[RESTAURACION BD] Base de datos restaurada con éxito desde respaldo ID: {}", id);
    }

    @Transactional
    public void restaurarDesdeArchivo(MultipartFile file, String adminPassword, String confirmacionTexto, String authenticatedAdminUsername) {
        RestaurarRespaldoRequestDTO dto = new RestaurarRespaldoRequestDTO();
        dto.setAdminPassword(adminPassword);
        dto.setConfirmacionTexto(confirmacionTexto);
        validarConfirmacionAdmin(dto, authenticatedAdminUsername);

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Debe seleccionar un archivo .sql válido.");
        }

        String origName = file.getOriginalFilename();
        if (origName == null || !origName.toLowerCase().endsWith(".sql")) {
            throw new RuntimeException("El archivo debe tener formato .sql");
        }

        File backupDir = new File("backups");
        if (!backupDir.exists()) backupDir.mkdirs();

        String tempName = "restauracion_upload_" + System.currentTimeMillis() + "_" + origName;
        File destFile = new File(backupDir, tempName);

        try {
            file.transferTo(destFile);
            ejecutarScriptSql(destFile);

            Usuario admin = authenticatedAdminUsername != null ?
                    usuarioRepository.findByUsernameOrEmail(authenticatedAdminUsername).orElse(null) : null;

            Respaldo reg = new Respaldo();
            reg.setNombreArchivo(origName);
            reg.setRutaAlmacenamiento(destFile.getAbsolutePath());
            reg.setTamanioBytes(destFile.length());
            reg.setEstado("EXITOSO");
            reg.setUsuario(admin);
            reg.setCreadoEn(LocalDateTime.now());
            respaldoRepository.save(reg);

            log.info("[RESTAURACION BD] Base de datos restaurada desde archivo cargado: {}", origName);
        } catch (IOException e) {
            throw new RuntimeException("Error al procesar el archivo de respaldo: " + e.getMessage(), e);
        }
    }

    private void validarConfirmacionAdmin(RestaurarRespaldoRequestDTO dto, String authenticatedAdminUsername) {
        if (dto.getConfirmacionTexto() == null || !"RESTAURAR".equalsIgnoreCase(dto.getConfirmacionTexto().trim())) {
            throw new RuntimeException("Confirmación requerida denegada. Debe ingresar exactamente la palabra 'RESTAURAR'.");
        }

        String adminUsername = dto.getAdminUsername() != null && !dto.getAdminUsername().isBlank()
                ? dto.getAdminUsername() : authenticatedAdminUsername;

        Usuario admin = null;
        if (adminUsername != null) {
            admin = usuarioRepository.findByUsernameOrEmail(adminUsername).orElse(null);
        }

        // Si no se encuentra por username directo, buscar al primer usuario con rol ADMIN
        if (admin == null) {
            admin = usuarioRepository.findAll().stream()
                    .filter(u -> u.getRol() != null && "ADMIN".equalsIgnoreCase(u.getRol().getNombreRol().name()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No se encontró usuario administrador en el sistema."));
        }

        if (!passwordEncoder.matches(dto.getAdminPassword(), admin.getPassworUsuario())) {
            throw new RuntimeException("Contraseña de administrador incorrecta. Se ha cancelado la restauración por seguridad.");
        }
    }

    private void ejecutarScriptSql(File sqlFile) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(sqlFile), StandardCharsets.UTF_8))) {
            StringBuilder currentStatement = new StringBuilder();
            String line;

            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0;");

            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();

                // Ignorar líneas vacías y comentarios de una sola línea
                if (trimmed.isEmpty() || trimmed.startsWith("--") || trimmed.startsWith("//") || trimmed.startsWith("#")) {
                    continue;
                }

                currentStatement.append(line).append("\n");

                // Verificar si la línea termina la sentencia SQL con punto y coma
                if (trimmed.endsWith(";")) {
                    String sql = currentStatement.toString().trim();
                    if (!sql.isEmpty()) {
                        try {
                            jdbcTemplate.execute(sql);
                        } catch (Exception ex) {
                            log.warn("[SQL RESTORE WARNING] Sentencia omitida o con advertencia: {} -> {}", sql, ex.getMessage());
                        }
                    }
                    currentStatement.setLength(0);
                }
            }

            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1;");

        } catch (Exception e) {
            log.error("[RESTAURACION ERROR] Falló la ejecución del script SQL: ", e);
            throw new RuntimeException("Fallo en la ejecución del script SQL de restauración: " + e.getMessage(), e);
        }
    }

    private RespaldoResponseDTO mapToRespaldoDTO(Respaldo r) {
        String creadoPor = "Administrador TI";
        if (r.getUsuario() != null) {
            creadoPor = r.getUsuario().getNombreUsuario() + " " + r.getUsuario().getApellidoUsuario();
        }

        return RespaldoResponseDTO.builder()
                .id(r.getId())
                .nombreArchivo(r.getNombreArchivo())
                .rutaAlmacenamiento(r.getRutaAlmacenamiento())
                .tamanioBytes(r.getTamanioBytes())
                .tamanioFormateado(formatearTamanio(r.getTamanioBytes()))
                .estado(r.getEstado())
                .creadoPor(creadoPor)
                .creadoEn(r.getCreadoEn())
                .build();
    }

    private String formatearTamanio(Long bytes) {
        if (bytes == null || bytes == 0) return "0 B";
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format(Locale.US, "%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}
