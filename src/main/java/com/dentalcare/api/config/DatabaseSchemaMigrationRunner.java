package com.dentalcare.api.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Runner de inicialización para aplicar ajustes de esquema DDL que Hibernate
 * ddl-auto=update omite deliberadamente (como alterar la nulabilidad de una columna existente).
 * Se ejecuta automáticamente al iniciar la aplicación web.
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
    }
}
