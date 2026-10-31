-- =============================================================================
-- SCRIPT DE MIGRACIÓN Y SEMBRADO SPRINTS 3 Y 4 PARA BD AIVEN (MySQL Workbench)
-- Base de datos: bd_dentalcareweb
-- =============================================================================

USE `bd_dentalcareweb`;

-- -----------------------------------------------------------------------------
-- 1. AJUSTES DE ESQUEMA CLÍNICO (Sprint 3 y Sprint 4)
-- -----------------------------------------------------------------------------

-- 1.1 Permitir valores NULL en 'pieza_dental' para tratamientos que no son pieza-específicos
ALTER TABLE `plan_tratamiento` 
    MODIFY COLUMN `pieza_dental` INT NULL;

-- 1.2 Agregar columna 'debe_cambiar_password' a la tabla 'usuario' (Seguridad y Reseteo)
SET @col_exists = (
    SELECT COUNT(*) 
    FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = 'bd_dentalcareweb' 
      AND TABLE_NAME = 'usuario' 
      AND COLUMN_NAME = 'debe_cambiar_password'
);
SET @sql_usuario = IF(@col_exists = 0, 
    'ALTER TABLE `usuario` ADD COLUMN `debe_cambiar_password` BOOLEAN DEFAULT FALSE', 
    'SELECT "Columna debe_cambiar_password ya existe en usuario"'
);
PREPARE stmt_user FROM @sql_usuario;
EXECUTE stmt_user;
DEALLOCATE PREPARE stmt_user;

-- 1.3 Asegurar el rol ASISTENTEODONTOLOGO en la tabla 'rol'
INSERT INTO `rol` (`nombre_rol`)
SELECT 'ASISTENTEODONTOLOGO'
WHERE NOT EXISTS (
    SELECT 1 FROM `rol` WHERE `nombre_rol` = 'ASISTENTEODONTOLOGO'
);

-- -----------------------------------------------------------------------------
-- 2. CREACIÓN DE TABLAS DE INVENTARIO (Sprint 4)
-- -----------------------------------------------------------------------------

-- 2.1 Tabla de Materiales Dentales
CREATE TABLE IF NOT EXISTS `materiales` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `codigo` VARCHAR(20) NOT NULL,
  `nombre` VARCHAR(100) NOT NULL,
  `unidad_medida` VARCHAR(50) NOT NULL,
  `stock_actual` INT NOT NULL DEFAULT 0,
  `stock_minimo` INT NOT NULL DEFAULT 5,
  `descripcion` TEXT,
  `creado_en` DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
  `actualizado_en` DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_materiales_codigo` (`codigo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 2.2 Tabla Unificada de Medicamentos (Catálogo Clínico e Inventario)
CREATE TABLE IF NOT EXISTS `medicamentos` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `codigo` VARCHAR(20) NOT NULL,
  `nombre` VARCHAR(100) NOT NULL,
  `presentacion` VARCHAR(100) NOT NULL,
  `lote` VARCHAR(50) DEFAULT NULL,
  `fecha_vencimiento` DATE DEFAULT NULL,
  `stock_actual` INT NOT NULL DEFAULT 0,
  `stock_minimo` INT NOT NULL DEFAULT 5,
  `componente_activo` VARCHAR(255) DEFAULT NULL,
  `concentracion` VARCHAR(255) DEFAULT NULL,
  `costo_medicamento` DECIMAL(10,2) DEFAULT '0.00',
  `id_proveedor` INT DEFAULT NULL,
  `creado_en` DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
  `actualizado_en` DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_medicamentos_codigo` (`codigo`),
  KEY `FK_medicamentos_proveedor` (`id_proveedor`),
  CONSTRAINT `FK_medicamentos_proveedor` FOREIGN KEY (`id_proveedor`) REFERENCES `proveedor` (`id_proveedor`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 2.3 Migrar datos existentes de la tabla 'medicamento' hacia 'medicamentos'
INSERT IGNORE INTO `medicamentos` 
    (`id`, `codigo`, `nombre`, `presentacion`, `lote`, `fecha_vencimiento`, `stock_actual`, `stock_minimo`, `componente_activo`, `concentracion`, `costo_medicamento`, `id_proveedor`, `creado_en`, `actualizado_en`)
SELECT 
    m.id_medicamento AS `id`,
    CONCAT('MD', LPAD(m.id_medicamento, 2, '0')) AS `codigo`,
    m.nombre_medicamento AS `nombre`,
    COALESCE(m.concentracion, 'Presentación estándar') AS `presentacion`,
    'LOT-INICIAL-2026' AS `lote`,
    DATE_ADD(CURRENT_DATE(), INTERVAL 2 YEAR) AS `fecha_vencimiento`,
    m.cantidad_inventario AS `stock_actual`,
    10 AS `stock_minimo`,
    m.componente_activo,
    m.concentracion,
    m.costo_medicamento,
    m.id_proveedor,
    NOW() AS `creado_en`,
    NOW() AS `actualizado_en`
FROM `medicamento` m;

-- -----------------------------------------------------------------------------
-- 3. UNIFICACIÓN DE CLAVES FORÁNEAS HACIA 'medicamentos'
-- -----------------------------------------------------------------------------

-- 3.1 Ajustar foreign key de detalle_prescripcion para que apunte a 'medicamentos'
SET @fk_dp = (
    SELECT CONSTRAINT_NAME 
    FROM information_schema.KEY_COLUMN_USAGE 
    WHERE TABLE_SCHEMA = 'bd_dentalcareweb' 
      AND TABLE_NAME = 'detalle_prescripcion' 
      AND REFERENCED_TABLE_NAME = 'medicamento'
    LIMIT 1
);

SET @sql_drop_dp_fk = IF(@fk_dp IS NOT NULL, 
    CONCAT('ALTER TABLE `detalle_prescripcion` DROP FOREIGN KEY `', @fk_dp, '`'), 
    'SELECT "FK anterior en detalle_prescripcion no encontrada o ya modificada"'
);
PREPARE stmt_drop_dp FROM @sql_drop_dp_fk;
EXECUTE stmt_drop_dp;
DEALLOCATE PREPARE stmt_drop_dp;

-- Agregar nueva FK apuntando a medicamentos(id) si no existe
SET @fk_dp_new = (
    SELECT CONSTRAINT_NAME 
    FROM information_schema.KEY_COLUMN_USAGE 
    WHERE TABLE_SCHEMA = 'bd_dentalcareweb' 
      AND TABLE_NAME = 'detalle_prescripcion' 
      AND REFERENCED_TABLE_NAME = 'medicamentos'
    LIMIT 1
);

SET @sql_add_dp_fk = IF(@fk_dp_new IS NULL,
    'ALTER TABLE `detalle_prescripcion` ADD CONSTRAINT `FK_detalle_prescripcion_medicamento` FOREIGN KEY (`id_medicamento`) REFERENCES `medicamentos` (`id`)',
    'SELECT "FK en detalle_prescripcion hacia medicamentos ya existe"'
);
PREPARE stmt_add_dp FROM @sql_add_dp_fk;
EXECUTE stmt_add_dp;
DEALLOCATE PREPARE stmt_add_dp;

-- 3.2 Ajustar foreign key de movimiento_inventario para que apunte a 'medicamentos'
SET @fk_mi = (
    SELECT CONSTRAINT_NAME 
    FROM information_schema.KEY_COLUMN_USAGE 
    WHERE TABLE_SCHEMA = 'bd_dentalcareweb' 
      AND TABLE_NAME = 'movimiento_inventario' 
      AND REFERENCED_TABLE_NAME = 'medicamento'
    LIMIT 1
);

SET @sql_drop_mi_fk = IF(@fk_mi IS NOT NULL, 
    CONCAT('ALTER TABLE `movimiento_inventario` DROP FOREIGN KEY `', @fk_mi, '`'), 
    'SELECT "FK anterior en movimiento_inventario no encontrada o ya modificada"'
);
PREPARE stmt_drop_mi FROM @sql_drop_mi_fk;
EXECUTE stmt_drop_mi;
DEALLOCATE PREPARE stmt_drop_mi;

SET @fk_mi_new = (
    SELECT CONSTRAINT_NAME 
    FROM information_schema.KEY_COLUMN_USAGE 
    WHERE TABLE_SCHEMA = 'bd_dentalcareweb' 
      AND TABLE_NAME = 'movimiento_inventario' 
      AND REFERENCED_TABLE_NAME = 'medicamentos'
    LIMIT 1
);

SET @sql_add_mi_fk = IF(@fk_mi_new IS NULL,
    'ALTER TABLE `movimiento_inventario` ADD CONSTRAINT `FK_movimiento_inventario_medicamento` FOREIGN KEY (`id_medicamento`) REFERENCES `medicamentos` (`id`)',
    'SELECT "FK en movimiento_inventario hacia medicamentos ya existe"'
);
PREPARE stmt_add_mi FROM @sql_add_mi_fk;
EXECUTE stmt_add_mi;
DEALLOCATE PREPARE stmt_add_mi;

-- 3.3 Reemplazar la tabla 'medicamento' por la VISTA de compatibilidad
DROP TABLE IF EXISTS `medicamento`;

CREATE OR REPLACE VIEW `medicamento` AS
SELECT 
    `id` AS `id_medicamento`,
    `nombre` AS `nombre_medicamento`,
    COALESCE(`componente_activo`, `codigo`) AS `componente_activo`,
    COALESCE(`concentracion`, `presentacion`) AS `concentracion`,
    `costo_medicamento`,
    `stock_actual` AS `cantidad_inventario`,
    `id_proveedor`
FROM `medicamentos`;

-- -----------------------------------------------------------------------------
-- 4. TABLA DE CONSUMOS CLÍNICOS MENSUALES (Sprint 4)
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS `consumos` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `tipo_item` ENUM('MATERIAL','MEDICAMENTO') NOT NULL,
  `item_id` INT NOT NULL,
  `item_codigo` VARCHAR(20) NOT NULL,
  `item_nombre` VARCHAR(100) NOT NULL,
  `cita_id` INT DEFAULT NULL,
  `cantidad` INT NOT NULL,
  `mes_consumo` VARCHAR(7) NOT NULL,
  `estado` ENUM('ANULADO','CONFIRMADO','PENDIENTE') NOT NULL DEFAULT 'PENDIENTE',
  `fecha_consumo` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `usuario_registro_id` INT NOT NULL,
  `notas` VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_consumos_cita` (`cita_id`),
  KEY `FK_consumos_usuario` (`usuario_registro_id`),
  CONSTRAINT `FK_consumos_cita` FOREIGN KEY (`cita_id`) REFERENCES `cita` (`id_citas`) ON DELETE SET NULL,
  CONSTRAINT `FK_consumos_usuario` FOREIGN KEY (`usuario_registro_id`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- -----------------------------------------------------------------------------
-- 5. SEMBRADO DE CATÁLOGO INICIAL (DATOS REALES LIMPIOS)
-- -----------------------------------------------------------------------------

-- 5.1 Catálogo de Materiales Odontológicos
INSERT IGNORE INTO `materiales` 
    (`id`, `codigo`, `nombre`, `unidad_medida`, `stock_actual`, `stock_minimo`, `descripcion`) 
VALUES 
    (1, 'MT78', 'Resina Fotocurable A2 Universal', 'Jeringa 4g', 25, 5, 'Resina microhíbrida para restauraciones estéticas anteriores y posteriores'),
    (2, 'MT10', 'Algodón Odontológico Absorbente', 'Rollo x 500', 40, 10, 'Rollos de algodón estéril para aislamiento relativo'),
    (3, 'MT22', 'Ácido Grabador Fosfórico 37%', 'Jeringa 3ml', 15, 5, 'Gel acondicionador de esmalte y dentina dental'),
    (4, 'MT01', 'Guantes de Nitrilo Talla M', 'Caja x 100 unidades', 30, 8, 'Guantes sin polvo, examen clínico');

-- 5.2 Catálogo de Medicamentos Odontológicos
-- (Nota: id_proveedor = 1 ya existe verificado en tu tabla proveedor de Aiven)
INSERT IGNORE INTO `medicamentos` 
    (`id`, `codigo`, `nombre`, `presentacion`, `lote`, `fecha_vencimiento`, `stock_actual`, `stock_minimo`, `componente_activo`, `concentracion`, `costo_medicamento`, `id_proveedor`) 
VALUES 
    (1, 'MD45', 'Amoxicilina 500mg cápsulas', 'Caja x 30 cápsulas', 'LOT-2026-X9', '2027-08-31', 50, 10, 'Amoxicilina trihidratada', '500 mg', 5.00, 1),
    (2, 'MD12', 'Lidocaína 2% con Epinefrina 1:100000', 'Caja x 50 carpules', 'LOT-2026-L2', '2027-12-31', 60, 15, 'Lidocaína / Epinefrina', '2% (1:100000)', 1.25, 1),
    (3, 'MD08', 'Ibuprofeno 600mg tabletas', 'Caja x 20 tabletas', 'LOT-2026-I4', '2028-02-28', 35, 8, 'Ibuprofeno', '600 mg', 0.50, 1),
    (4, 'MD03', 'Clindamicina 300mg', 'Caja x 16 cápsulas', 'CL-9921-Z', '2027-10-31', 20, 5, 'Clindamicina Clorhidrato', '300 mg', 8.00, 1);

-- Fin del script.
