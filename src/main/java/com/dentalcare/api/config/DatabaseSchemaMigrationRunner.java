package com.dentalcare.api.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Runner de inicialización para aplicar ajustes de esquema DDL que Hibernate
 * ddl-auto=update omite deliberadamente (como alterar la nulabilidad de una columna existente)
 * y sembrado defensivo inicial para Sprint 4.
 */
@Component
public class DatabaseSchemaMigrationRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSchemaMigrationRunner.class);
    private final JdbcTemplate jdbcTemplate;

    public DatabaseSchemaMigrationRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        try {
            log.info("Verificando y aplicando actualización de esquema para 'plan_tratamiento.pieza_dental'...");
            jdbcTemplate.execute("ALTER TABLE plan_tratamiento MODIFY pieza_dental INT NULL");
            log.info("Esquema verificado/actualizado: 'plan_tratamiento.pieza_dental' ahora permite valores NULL.");
        } catch (Exception e) {
            log.warn("Aviso de esquema (normal si ya fue modificado o si no hay permisos DDL): {}", e.getMessage());
        }

        try {
            // Sembrado inicial defensivo para Sprint 4 (Inventario) si las tablas están vacías
            Integer countMateriales = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM materiales", Integer.class);
            if (countMateriales != null && countMateriales == 0) {
                log.info("Sembrando catálogo inicial de materiales para Sprint 4...");
                jdbcTemplate.execute("INSERT INTO materiales (codigo, nombre, unidad_medida, stock_actual, stock_minimo, descripcion) VALUES " +
                        "('MT78', 'Resina Fotocurable A2 Universal', 'Jeringa 4g', 25, 5, 'Resina microhíbrida para restauraciones estéticas anteriores y posteriores'), " +
                        "('MT10', 'Algodón Odontológico Absorbente', 'Rollo x 500', 40, 10, 'Rollos de algodón estéril para aislamiento relativo'), " +
                        "('MT22', 'Ácido Grabador Fosfórico 37%', 'Jeringa 3ml', 15, 5, 'Gel acondicionador de esmalte y dentina dental')");
            }

            Integer countMedicamentos = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM medicamentos", Integer.class);
            if (countMedicamentos != null && countMedicamentos == 0) {
                log.info("Sembrando catálogo inicial de medicamentos para Sprint 4...");
                jdbcTemplate.execute("INSERT INTO medicamentos (codigo, nombre, presentacion, lote, fecha_vencimiento, stock_actual, stock_minimo) VALUES " +
                        "('MD45', 'Amoxicilina 500mg cápsulas', 'Caja x 30 cápsulas', 'LOT-2026-X9', '2027-08-31', 50, 10), " +
                        "('MD12', 'Lidocaína 2% con Epinefrina 1:100000', 'Caja x 50 carpules', 'LOT-2026-L2', '2027-12-31', 60, 15), " +
                        "('MD08', 'Ibuprofeno 600mg tabletas', 'Caja x 20 tabletas', 'LOT-2026-I4', '2028-02-28', 35, 8)");
            }

            Integer countConsumos = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM consumos", Integer.class);
            if (countConsumos != null && countConsumos == 0) {
                log.info("Sembrando consumos de demostración para Sprint 4...");
                Integer idUsuario = jdbcTemplate.queryForObject("SELECT id_usuario FROM usuario LIMIT 1", Integer.class);
                if (idUsuario != null) {
                    jdbcTemplate.execute("INSERT INTO consumos (tipo_item, item_id, item_codigo, item_nombre, cantidad, mes_consumo, estado, usuario_registro_id, notas) VALUES " +
                            "('MATERIAL', 1, 'MT78', 'Resina Fotocurable A2 Universal', 2, '2026-10', 'PENDIENTE', " + idUsuario + ", 'Consumo en calce de amalgama amplia'), " +
                            "('MEDICAMENTO', 2, 'MD12', 'Lidocaína 2% con Epinefrina 1:100000', 3, '2026-10', 'PENDIENTE', " + idUsuario + ", 'Anestesia infiltrativa para exodoncia')");
                }
            }
        } catch (Exception e) {
            log.warn("Aviso de inicialización de datos de inventario: {}", e.getMessage());
        }
    }
}
