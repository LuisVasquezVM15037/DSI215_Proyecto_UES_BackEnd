-- =============================================================================
-- SCRIPT DE MIGRACIÓN Y SEMBRADO SPRINT 5 PARA BD AIVEN (MySQL Workbench)
-- Base de datos: bd_dentalcareweb
-- =============================================================================

USE `bd_dentalcareweb`;

-- -----------------------------------------------------------------------------
-- 1. TABLA: notificaciones (Centro Unificado de Notificaciones)
-- DENTALCARE-71 (PBI-Secret-16)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `notificaciones` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `usuario_id` INT DEFAULT NULL COMMENT 'Destinatario específico (NULL si aplica a todo un rol)',
  `rol_destinatario` VARCHAR(50) DEFAULT NULL COMMENT 'Rol destinatario: RECEPCIONISTA, GERENTE, ASISTENTEODONTOLOGO, ADMIN',
  `titulo` VARCHAR(150) NOT NULL,
  `mensaje` TEXT NOT NULL,
  `tipo` ENUM('CITA_PROXIMA', 'LISTA_ESPERA', 'STOCK_CRITICO', 'RENDIMIENTO', 'SISTEMA') NOT NULL DEFAULT 'SISTEMA',
  `leida` BOOLEAN NOT NULL DEFAULT FALSE,
  `fecha_creacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `referencia_id` INT DEFAULT NULL COMMENT 'ID de la entidad vinculada (id_citas, id_paciente, id material)',
  `ruta_redireccion` VARCHAR(255) DEFAULT NULL COMMENT 'Ruta interna para navegación contextual',
  PRIMARY KEY (`id`),
  KEY `idx_notificaciones_usuario` (`usuario_id`),
  KEY `idx_notificaciones_rol` (`rol_destinatario`),
  KEY `idx_notificaciones_leida` (`leida`),
  CONSTRAINT `fk_notificaciones_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id_usuario`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- -----------------------------------------------------------------------------
-- 2. TABLA: configuracion_alerta_cita (Preferencias de Alertas de Citas)
-- DENTALCARE-96 (PBI-Secret-15)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `configuracion_alerta_cita` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `usuario_id` INT NOT NULL UNIQUE,
  `alertas_activas` BOOLEAN NOT NULL DEFAULT TRUE,
  `tiempo_anticipacion_minutos` INT NOT NULL DEFAULT 30,
  `sonido_activo` BOOLEAN NOT NULL DEFAULT TRUE,
  `actualizado_en` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_config_alerta_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id_usuario`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- -----------------------------------------------------------------------------
-- 3. TABLA: lista_espera (Pacientes en Cola para Cupos Liberados)
-- DENTALCARE-92 (PBI-Secret-14)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `lista_espera` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `paciente_id` INT NOT NULL,
  `odontologo_id` INT DEFAULT NULL COMMENT 'Odontólogo preferido (NULL si cualquier odontólogo)',
  `rango_horario_preferido` VARCHAR(100) NOT NULL COMMENT 'Ej: Mañana (08:00 - 12:00), Tarde (14:00 - 18:00)',
  `dias_preferidos` VARCHAR(100) DEFAULT NULL COMMENT 'Ej: Lunes a Viernes',
  `motivo_consulta` VARCHAR(255) NOT NULL,
  `prioridad` ENUM('ALTA', 'MEDIA', 'BAJA') NOT NULL DEFAULT 'MEDIA',
  `estado` ENUM('EN_ESPERA', 'NOTIFICADO', 'AGENDADO', 'DESCARTADO') NOT NULL DEFAULT 'EN_ESPERA',
  `fecha_solicitud` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `cita_asignada_id` INT DEFAULT NULL COMMENT 'ID de la cita creada tras asignación',
  `notas` TEXT DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_lista_espera_paciente` (`paciente_id`),
  KEY `idx_lista_espera_odontologo` (`odontologo_id`),
  KEY `idx_lista_espera_estado` (`estado`),
  CONSTRAINT `fk_lista_espera_paciente` FOREIGN KEY (`paciente_id`) REFERENCES `paciente` (`id_paciente`) ON DELETE CASCADE,
  CONSTRAINT `fk_lista_espera_odontologo` FOREIGN KEY (`odontologo_id`) REFERENCES `odontologo` (`id_odontologo`) ON DELETE SET NULL,
  CONSTRAINT `fk_lista_espera_cita` FOREIGN KEY (`cita_asignada_id`) REFERENCES `cita` (`id_citas`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- -----------------------------------------------------------------------------
-- 4. TABLA: reglas_alerta_rendimiento (Umbrales de Indicadores Clave de Rendimiento)
-- DENTALCARE-95 (PBI-Gerencia-05)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `reglas_alerta_rendimiento` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `nombre_indicador` VARCHAR(100) NOT NULL,
  `descripcion` VARCHAR(255) DEFAULT NULL,
  `tipo_metrica` ENUM('ATENCIONES_MINIMAS', 'MAX_CANCELACIONES_PORCENTAJE', 'STOCK_CRITICO_CANTIDAD', 'TIEMPO_ESPERA_PROMEDIO_MINS') NOT NULL,
  `operador_comparacion` ENUM('MENOR_QUE', 'MENOR_IGUAL_QUE', 'MAYOR_QUE', 'MAYOR_IGUAL_QUE') NOT NULL,
  `valor_umbral` DECIMAL(10,2) NOT NULL,
  `periodo_evaluacion` ENUM('DIARIO', 'SEMANAL', 'MENSUAL') NOT NULL DEFAULT 'SEMANAL',
  `activo` BOOLEAN NOT NULL DEFAULT TRUE,
  `ultima_evaluacion` DATETIME DEFAULT NULL,
  `ultimo_valor_detectado` DECIMAL(10,2) DEFAULT NULL,
  `usuario_creador_id` INT NOT NULL,
  `creado_en` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_reglas_alerta_usuario` (`usuario_creador_id`),
  CONSTRAINT `fk_reglas_alerta_usuario` FOREIGN KEY (`usuario_creador_id`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- -----------------------------------------------------------------------------
-- 5. SEMBRADO DE DATOS DE PRUEBA INICIALES (SPRINT 5 - FASE 1)
-- -----------------------------------------------------------------------------

-- Notificaciones iniciales
INSERT IGNORE INTO `notificaciones` 
  (`id`, `usuario_id`, `rol_destinatario`, `titulo`, `mensaje`, `tipo`, `leida`, `fecha_creacion`, `ruta_redireccion`)
VALUES
  (1, NULL, 'RECEPCIONISTA', 'Recordatorio de Cita Próxima', 'La cita de Carlos Mendoza con Dra. Ramos inicia en 30 minutos.', 'CITA_PROXIMA', FALSE, NOW(), '/agenda'),
  (2, NULL, 'RECEPCIONISTA', 'Cupo de Agenda Liberado', 'Se liberó un espacio a las 10:00 AM. Hay pacientes en lista de espera disponibles.', 'LISTA_ESPERA', FALSE, NOW(), '/agenda?tab=espera'),
  (3, NULL, 'RECEPCIONISTA', 'Bienvenida a Sprint 5', 'El centro de notificaciones y la lista de espera han sido activados en DentalCare.', 'SISTEMA', TRUE, NOW(), '/agenda');

-- Registros iniciales en Lista de Espera (si existen pacientes)
INSERT IGNORE INTO `lista_espera`
  (`id`, `paciente_id`, `odontologo_id`, `rango_horario_preferido`, `dias_preferidos`, `motivo_consulta`, `prioridad`, `estado`, `fecha_solicitud`, `notas`)
SELECT
  1, p.id_paciente, NULL, 'Mañana (08:00 - 12:00)', 'Lunes a Jueves', 'Evaluación por dolor en molar y limpieza', 'ALTA', 'EN_ESPERA', NOW(), 'Disponible con 1 hora de anticipación'
FROM `paciente` p LIMIT 1;
