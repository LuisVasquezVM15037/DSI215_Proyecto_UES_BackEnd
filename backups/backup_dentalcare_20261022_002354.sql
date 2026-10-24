-- ========================================================
-- DENTALCARE CLINICAL ERP - VOLCADO DE BASE DE DATOS
-- Fecha de Generación: 2026-10-22T00:23:54.992387400
-- Generado por Administrador TI: admin
-- ========================================================

SET FOREIGN_KEY_CHECKS = 0;
SET NAMES utf8mb4;

-- --------------------------------------------------------
-- Estructura y datos para la tabla `cita`
-- --------------------------------------------------------
DROP TABLE IF EXISTS `cita`;
CREATE TABLE `cita` (
  `id_citas` int NOT NULL AUTO_INCREMENT,
  `estado_cita` enum('CANCELADA','COMPLETADA','EN_PROGRESO','NO_ASISTIO','OTRO','PENDIENTE','PROGRAMADA','REPROGRAMADA') NOT NULL,
  `fecha_cita` date NOT NULL,
  `hora_fin_cita` datetime(6) NOT NULL,
  `hora_inicio_cita` datetime(6) NOT NULL,
  `motivo_cancelacion` varchar(255) DEFAULT NULL,
  `id_odontologo` int NOT NULL,
  `id_paciente` int NOT NULL,
  PRIMARY KEY (`id_citas`),
  KEY `FK4aleuc3urfrf1fbysd1cycp68` (`id_odontologo`),
  KEY `FK7fljkhue1c7r80b4li70f6fh3` (`id_paciente`),
  CONSTRAINT `FK4aleuc3urfrf1fbysd1cycp68` FOREIGN KEY (`id_odontologo`) REFERENCES `odontologo` (`id_odontologo`),
  CONSTRAINT `FK7fljkhue1c7r80b4li70f6fh3` FOREIGN KEY (`id_paciente`) REFERENCES `paciente` (`id_paciente`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `cita` VALUES (1, 'NO_ASISTIO', '2026-06-13', '2026-06-13T18:30', '2026-06-13T17:30', NULL, 1, 3);
INSERT INTO `cita` VALUES (2, 'NO_ASISTIO', '2026-08-24', '2026-08-24T05:40', '2026-08-24T04:40', NULL, 1, 1);
INSERT INTO `cita` VALUES (3, 'NO_ASISTIO', '2026-09-21', '2026-09-19T14:00', '2026-09-19T13:00', NULL, 1, 1);
INSERT INTO `cita` VALUES (4, 'NO_ASISTIO', '2026-09-26', '2026-09-26T13:00', '2026-09-26T12:00', NULL, 1, 1);
INSERT INTO `cita` VALUES (5, 'NO_ASISTIO', '2026-06-10', '2026-09-19T16:14', '2026-09-19T15:14', NULL, 1, 1);
INSERT INTO `cita` VALUES (6, 'PROGRAMADA', '2026-10-03', '2026-10-04T03:57', '2026-10-04T02:57', NULL, 2, 3);
INSERT INTO `cita` VALUES (7, 'COMPLETADA', '2026-10-16', '2026-10-17T05:00', '2026-10-17T04:00', NULL, 4, 4);
INSERT INTO `cita` VALUES (8, 'COMPLETADA', '2026-10-20', '2026-10-21T06:43', '2026-10-21T05:43', NULL, 4, 4);
INSERT INTO `cita` VALUES (9, 'PROGRAMADA', '2026-10-21', '2026-10-21T15:00', '2026-10-21T14:00', NULL, 1, 1);
INSERT INTO `cita` VALUES (10, 'PROGRAMADA', '2026-10-21', '2026-10-21T15:00', '2026-10-21T14:00', NULL, 3, 2);
INSERT INTO `cita` VALUES (11, 'PROGRAMADA', '2026-10-21', '2026-10-21T21:00', '2026-10-21T20:00', NULL, 1, 1);

-- --------------------------------------------------------
-- Estructura y datos para la tabla `consumos`
-- --------------------------------------------------------
DROP TABLE IF EXISTS `consumos`;
CREATE TABLE `consumos` (
  `id` int NOT NULL AUTO_INCREMENT,
  `cantidad` int NOT NULL,
  `estado` enum('ANULADO','CONFIRMADO','PENDIENTE') NOT NULL,
  `fecha_consumo` datetime(6) NOT NULL,
  `item_codigo` varchar(20) NOT NULL,
  `item_id` int NOT NULL,
  `item_nombre` varchar(100) NOT NULL,
  `mes_consumo` varchar(7) NOT NULL,
  `notas` varchar(255) DEFAULT NULL,
  `tipo_item` enum('MATERIAL','MEDICAMENTO') NOT NULL,
  `cita_id` int DEFAULT NULL,
  `usuario_registro_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKcetbk57qqbtpo993hqwi6j33e` (`cita_id`),
  KEY `FKsgfyh8u1bssdxx8w8fs4xwevt` (`usuario_registro_id`),
  CONSTRAINT `FKcetbk57qqbtpo993hqwi6j33e` FOREIGN KEY (`cita_id`) REFERENCES `cita` (`id_citas`),
  CONSTRAINT `FKsgfyh8u1bssdxx8w8fs4xwevt` FOREIGN KEY (`usuario_registro_id`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- --------------------------------------------------------
-- Estructura y datos para la tabla `detalle_prescripcion`
-- --------------------------------------------------------
DROP TABLE IF EXISTS `detalle_prescripcion`;
CREATE TABLE `detalle_prescripcion` (
  `id_detalle_prescripcion` int NOT NULL AUTO_INCREMENT,
  `dosis_prescripcion` varchar(255) NOT NULL,
  `duracion_prescripcion` int NOT NULL,
  `frecuencia_prescripcion` varchar(255) NOT NULL,
  `indicaciones_prescripcion` text NOT NULL,
  `id_medicamento` int NOT NULL,
  `id_prescripcion` int NOT NULL,
  `id_plan_tratamiento` int DEFAULT NULL,
  PRIMARY KEY (`id_detalle_prescripcion`),
  KEY `FKknmr0t9mip4kf9mfpc1npfoir` (`id_medicamento`),
  KEY `FK1lq2bwwt1gxqj095ptq1t3gmv` (`id_prescripcion`),
  KEY `FK9i1182rrm1ai0e0rwekqmcshr` (`id_plan_tratamiento`),
  CONSTRAINT `FK1lq2bwwt1gxqj095ptq1t3gmv` FOREIGN KEY (`id_prescripcion`) REFERENCES `prescripcion` (`id_prescripcion`),
  CONSTRAINT `FK9i1182rrm1ai0e0rwekqmcshr` FOREIGN KEY (`id_plan_tratamiento`) REFERENCES `plan_tratamiento` (`id_plan_tratamiento`),
  CONSTRAINT `FKknmr0t9mip4kf9mfpc1npfoir` FOREIGN KEY (`id_medicamento`) REFERENCES `medicamento` (`id_medicamento`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `detalle_prescripcion` VALUES (1, '1 cápsula', 7, 'Cada 8 horas', 'Tomar después de las comidas', 1, 1, NULL);
INSERT INTO `detalle_prescripcion` VALUES (2, '500', 3, '8', 'alimentos con medicina', 1, 2, NULL);
INSERT INTO `detalle_prescripcion` VALUES (3, '345', 4, '3', 'Tomar con mucha agua', 1, 3, NULL);
INSERT INTO `detalle_prescripcion` VALUES (4, '454', 4, '4', 'ewwe', 1, 4, NULL);
INSERT INTO `detalle_prescripcion` VALUES (5, '350', 3, '6', 'Tomar con suficiente agua', 1, 5, NULL);
INSERT INTO `detalle_prescripcion` VALUES (6, '344', 5, '4', '', 1, 1, NULL);
INSERT INTO `detalle_prescripcion` VALUES (7, '3', 3, '3', '', 1, 1, NULL);
INSERT INTO `detalle_prescripcion` VALUES (8, '500', 7, '8 horas', 'con mucha agua', 1, 2, NULL);
INSERT INTO `detalle_prescripcion` VALUES (9, '500', 7, '65', 'con mucha agua', 1, 3, NULL);

-- --------------------------------------------------------
-- Estructura y datos para la tabla `evaluacion_clinica`
-- --------------------------------------------------------
DROP TABLE IF EXISTS `evaluacion_clinica`;
CREATE TABLE `evaluacion_clinica` (
  `id_evaluacion_clinica` int NOT NULL AUTO_INCREMENT,
  `diagnostico` text NOT NULL,
  `observaciones` text,
  `id_cita` int NOT NULL,
  PRIMARY KEY (`id_evaluacion_clinica`),
  KEY `FKattvruklkmulktk8lbaaq0evw` (`id_cita`),
  CONSTRAINT `FKattvruklkmulktk8lbaaq0evw` FOREIGN KEY (`id_cita`) REFERENCES `cita` (`id_citas`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `evaluacion_clinica` VALUES (1, 'Dolor de muela cordal', '', 2);
INSERT INTO `evaluacion_clinica` VALUES (2, 'muelas', '', 3);
INSERT INTO `evaluacion_clinica` VALUES (3, 'nada', '', 6);
INSERT INTO `evaluacion_clinica` VALUES (4, 'Relleno de muelas', '', 7);
INSERT INTO `evaluacion_clinica` VALUES (5, 'dolor de muelas', '', 8);

-- --------------------------------------------------------
-- Estructura y datos para la tabla `evolucion_tratamiento`
-- --------------------------------------------------------
DROP TABLE IF EXISTS `evolucion_tratamiento`;
CREATE TABLE `evolucion_tratamiento` (
  `id_evolucion_tratamiento` int NOT NULL AUTO_INCREMENT,
  `estado_evolucion_tratamiento` enum('CONTINUACION','EN_PROGRESO','FINALIZADO','INICIADO','OTRO','PENDIENTE') NOT NULL,
  `fecha_tratamiento` datetime(6) NOT NULL,
  `notas_evolucion_tratamiento` text,
  `id_cita` int NOT NULL,
  `id_plan_tratamiento` int NOT NULL,
  PRIMARY KEY (`id_evolucion_tratamiento`),
  KEY `FKfbrcfnv56lyupuut2aaw5pgin` (`id_cita`),
  KEY `FKdj6sbk78il9xcalw3qk6nt87p` (`id_plan_tratamiento`),
  CONSTRAINT `FKdj6sbk78il9xcalw3qk6nt87p` FOREIGN KEY (`id_plan_tratamiento`) REFERENCES `plan_tratamiento` (`id_plan_tratamiento`),
  CONSTRAINT `FKfbrcfnv56lyupuut2aaw5pgin` FOREIGN KEY (`id_cita`) REFERENCES `cita` (`id_citas`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `evolucion_tratamiento` VALUES (1, 'EN_PROGRESO', '2026-05-22T10:30', 'Se realizó limpieza inicial, resina programada para próxima cita', 1, 1);

-- --------------------------------------------------------
-- Estructura y datos para la tabla `materiales`
-- --------------------------------------------------------
DROP TABLE IF EXISTS `materiales`;
CREATE TABLE `materiales` (
  `id` int NOT NULL AUTO_INCREMENT,
  `actualizado_en` datetime(6) DEFAULT NULL,
  `codigo` varchar(20) NOT NULL,
  `creado_en` datetime(6) DEFAULT NULL,
  `descripcion` text,
  `nombre` varchar(100) NOT NULL,
  `stock_actual` int NOT NULL,
  `stock_minimo` int NOT NULL,
  `unidad_medida` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKd53e1p48du55j0v6ihy8ng61s` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `materiales` VALUES (1, NULL, 'MT78', NULL, 'Resina microhíbrida para restauraciones estéticas anteriores y posteriores', 'Resina Fotocurable A2 Universal', 25, 5, 'Jeringa 4g');
INSERT INTO `materiales` VALUES (2, '2026-10-21T04:40:10.967865', 'MT10', NULL, 'Rollos de algodón estéril para aislamiento relativo', 'Algodón Odontológico Absorbente', 30, 10, 'Rollo x 500');
INSERT INTO `materiales` VALUES (3, NULL, 'MT22', NULL, 'Gel acondicionador de esmalte y dentina dental', 'Ácido Grabador Fosfórico 37%', 15, 5, 'Jeringa 3ml');

-- --------------------------------------------------------
-- Estructura y datos para la tabla `medicamento`
-- --------------------------------------------------------
DROP TABLE IF EXISTS `medicamento`;
CREATE TABLE `medicamento` (
  `id_medicamento` int NOT NULL AUTO_INCREMENT,
  `cantidad_inventario` int NOT NULL,
  `componente_activo` varchar(255) NOT NULL,
  `concentracion` varchar(255) NOT NULL,
  `costo_medicamento` decimal(10,2) NOT NULL,
  `nombre_medicamento` varchar(255) NOT NULL,
  `id_proveedor` int NOT NULL,
  PRIMARY KEY (`id_medicamento`),
  KEY `FKgnlwy4uayvmp9jcvr4901tm2h` (`id_proveedor`),
  CONSTRAINT `FKgnlwy4uayvmp9jcvr4901tm2h` FOREIGN KEY (`id_proveedor`) REFERENCES `proveedor` (`id_proveedor`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `medicamento` VALUES (1, 150, 'Amoxicilina', '500 mg', 0.25, 'Amoxicilina 500mg', 1);

-- --------------------------------------------------------
-- Estructura y datos para la tabla `medicamentos`
-- --------------------------------------------------------
DROP TABLE IF EXISTS `medicamentos`;
CREATE TABLE `medicamentos` (
  `id` int NOT NULL AUTO_INCREMENT,
  `actualizado_en` datetime(6) DEFAULT NULL,
  `codigo` varchar(20) NOT NULL,
  `creado_en` datetime(6) DEFAULT NULL,
  `fecha_vencimiento` date DEFAULT NULL,
  `lote` varchar(50) DEFAULT NULL,
  `nombre` varchar(100) NOT NULL,
  `presentacion` varchar(100) NOT NULL,
  `stock_actual` int NOT NULL,
  `stock_minimo` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKn0m42i9s6rayllrky34o0kgvg` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `medicamentos` VALUES (1, NULL, 'MD45', NULL, '2027-08-31', 'LOT-2026-X9', 'Amoxicilina 500mg cápsulas', 'Caja x 30 cápsulas', 50, 10);
INSERT INTO `medicamentos` VALUES (2, NULL, 'MD12', NULL, '2027-12-31', 'LOT-2026-L2', 'Lidocaína 2% con Epinefrina 1:100000', 'Caja x 50 carpules', 60, 15);
INSERT INTO `medicamentos` VALUES (3, NULL, 'MD08', NULL, '2028-02-28', 'LOT-2026-I4', 'Ibuprofeno 600mg tabletas', 'Caja x 20 tabletas', 35, 8);

-- --------------------------------------------------------
-- Estructura y datos para la tabla `movimiento_inventario`
-- --------------------------------------------------------
DROP TABLE IF EXISTS `movimiento_inventario`;
CREATE TABLE `movimiento_inventario` (
  `id_movimiento` int NOT NULL AUTO_INCREMENT,
  `cantidad` int NOT NULL,
  `fecha_movimiento` datetime(6) NOT NULL,
  `motivo` text NOT NULL,
  `tipo_movimiento` enum('AJUSTE','DEVOLUCION','INGRESO','OTRO','REPOSICION','SALIDA','TRANSFERENCIA_ENTRADA','TRANSFERENCIA_SALIDA') NOT NULL,
  `id_medicamento` int NOT NULL,
  `id_usuario` int NOT NULL,
  PRIMARY KEY (`id_movimiento`),
  KEY `FKp68slfgcwb5ut2i69rnygdku2` (`id_medicamento`),
  KEY `FK93w2jdh5db2ee213yhev288mq` (`id_usuario`),
  CONSTRAINT `FK93w2jdh5db2ee213yhev288mq` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`),
  CONSTRAINT `FKp68slfgcwb5ut2i69rnygdku2` FOREIGN KEY (`id_medicamento`) REFERENCES `medicamento` (`id_medicamento`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `movimiento_inventario` VALUES (1, 150, '2026-05-21T08:00', 'Compra inicial a proveedor', 'INGRESO', 1, 1);

-- --------------------------------------------------------
-- Estructura y datos para la tabla `odontologo`
-- --------------------------------------------------------
DROP TABLE IF EXISTS `odontologo`;
CREATE TABLE `odontologo` (
  `id_odontologo` int NOT NULL AUTO_INCREMENT,
  `especialidad_odontologo` varchar(255) NOT NULL,
  `jvpo_id` varchar(255) NOT NULL,
  `id_usuario` int NOT NULL,
  PRIMARY KEY (`id_odontologo`),
  UNIQUE KEY `UKjlqchk81fefnkidejt3fyfe2p` (`jvpo_id`),
  KEY `FK679oebsued1bcdx9ymqvywme4` (`id_usuario`),
  CONSTRAINT `FK679oebsued1bcdx9ymqvywme4` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `odontologo` VALUES (1, 'Ortodoncia', 'JVPO-001', 1);
INSERT INTO `odontologo` VALUES (2, 'Endodoncia', 'JVPO-002', 2);
INSERT INTO `odontologo` VALUES (3, 'Periodoncia', 'JVPO-003', 3);
INSERT INTO `odontologo` VALUES (4, 'Ortodoncia', 'JPVO-1234', 4);

-- --------------------------------------------------------
-- Estructura y datos para la tabla `paciente`
-- --------------------------------------------------------
DROP TABLE IF EXISTS `paciente`;
CREATE TABLE `paciente` (
  `id_paciente` int NOT NULL AUTO_INCREMENT,
  `apellido_paciente` varchar(255) NOT NULL,
  `contacto_emergencia` varchar(255) DEFAULT NULL,
  `email_paciente` varchar(255) DEFAULT NULL,
  `fecha_nacimiento_paciente` date NOT NULL,
  `nombre_paciente` varchar(255) NOT NULL,
  `numero_identidad_paciente` varchar(255) NOT NULL,
  `telefono_paciente` varchar(255) DEFAULT NULL,
  `alergias` text,
  PRIMARY KEY (`id_paciente`),
  UNIQUE KEY `UKcx80xo5kjuucnsovm197r574` (`numero_identidad_paciente`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `paciente` VALUES (1, 'Hernández', 'Pedro Hernández 7999-0001', 'maria@gmail.com', '1990-03-15', 'María', '01234567-8', '7111-2222', 'Ninguna conocida');
INSERT INTO `paciente` VALUES (2, 'Castillo', 'Laura Castillo 7888-0002', 'roberto@gmail.com', '1985-07-22', 'Roberto', '09876543-2', '7333-4444', 'Penicilina');
INSERT INTO `paciente` VALUES (3, 'Ramírez', 'Carlos Ramírez 7777-0003', 'sofia@gmail.com', '2000-11-05', 'Sofía', '05678901-3', '7555-6666', 'Ibuprofeno');
INSERT INTO `paciente` VALUES (4, 'Vasquez', 'Luis Vasquez 22530431', 'vm15037@ti.ues.edu.sv', '2018-02-22', 'Benjamin', '048504349', '22577777', '');

-- --------------------------------------------------------
-- Estructura y datos para la tabla `password_resets`
-- --------------------------------------------------------
DROP TABLE IF EXISTS `password_resets`;
CREATE TABLE `password_resets` (
  `id` int NOT NULL AUTO_INCREMENT,
  `clave_temporal` varchar(100) DEFAULT NULL,
  `creado_en` datetime(6) NOT NULL,
  `expira_en` datetime(6) NOT NULL,
  `token` varchar(128) NOT NULL,
  `utilizado` bit(1) NOT NULL,
  `usuario_id` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKj3in8q8o0ve0i34pyug2kgibh` (`token`),
  KEY `FKp8wdtbcp9ej27t0fcx6t64rgc` (`usuario_id`),
  CONSTRAINT `FKp8wdtbcp9ej27t0fcx6t64rgc` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- --------------------------------------------------------
-- Estructura y datos para la tabla `plan_tratamiento`
-- --------------------------------------------------------
DROP TABLE IF EXISTS `plan_tratamiento`;
CREATE TABLE `plan_tratamiento` (
  `id_plan_tratamiento` int NOT NULL AUTO_INCREMENT,
  `estado_plan` enum('CANCELADO','COMPLETADO','EN_PROGRESO','OTRO','PENDIENTE','PROGRAMADO') NOT NULL,
  `pieza_dental` int DEFAULT NULL,
  `id_evaluacion_clinica` int NOT NULL,
  `id_tratamiento` int NOT NULL,
  PRIMARY KEY (`id_plan_tratamiento`),
  KEY `FK2b3dnlkcefo1thkg9mymnta5s` (`id_evaluacion_clinica`),
  KEY `FKgn3xdbccu9ed1ebr5s016r12q` (`id_tratamiento`),
  CONSTRAINT `FK2b3dnlkcefo1thkg9mymnta5s` FOREIGN KEY (`id_evaluacion_clinica`) REFERENCES `evaluacion_clinica` (`id_evaluacion_clinica`),
  CONSTRAINT `FKgn3xdbccu9ed1ebr5s016r12q` FOREIGN KEY (`id_tratamiento`) REFERENCES `tratamiento` (`id_tratamiento`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `plan_tratamiento` VALUES (1, 'COMPLETADO', 42, 1, 4);
INSERT INTO `plan_tratamiento` VALUES (2, 'COMPLETADO', 34, 1, 1);
INSERT INTO `plan_tratamiento` VALUES (3, 'COMPLETADO', 48, 1, 3);
INSERT INTO `plan_tratamiento` VALUES (4, 'COMPLETADO', 18, 1, 2);
INSERT INTO `plan_tratamiento` VALUES (5, 'COMPLETADO', 46, 1, 1);
INSERT INTO `plan_tratamiento` VALUES (6, 'COMPLETADO', 26, 1, 1);
INSERT INTO `plan_tratamiento` VALUES (7, 'PENDIENTE', 13, 3, 1);
INSERT INTO `plan_tratamiento` VALUES (8, 'COMPLETADO', 28, 4, 2);
INSERT INTO `plan_tratamiento` VALUES (9, 'COMPLETADO', 0, 4, 5);

-- --------------------------------------------------------
-- Estructura y datos para la tabla `prescripcion`
-- --------------------------------------------------------
DROP TABLE IF EXISTS `prescripcion`;
CREATE TABLE `prescripcion` (
  `id_prescripcion` int NOT NULL AUTO_INCREMENT,
  `fecha_prescripcion` datetime(6) NOT NULL,
  `id_cita` int NOT NULL,
  PRIMARY KEY (`id_prescripcion`),
  KEY `FKidwd4kw198kb3bvabbr727shn` (`id_cita`),
  CONSTRAINT `FKidwd4kw198kb3bvabbr727shn` FOREIGN KEY (`id_cita`) REFERENCES `cita` (`id_citas`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `prescripcion` VALUES (1, '2026-09-19T07:37:35.147805', 2);
INSERT INTO `prescripcion` VALUES (2, '2026-10-17T03:30:52.775152', 7);
INSERT INTO `prescripcion` VALUES (3, '2026-10-21T04:47:57.736414', 8);

-- --------------------------------------------------------
-- Estructura y datos para la tabla `proveedor`
-- --------------------------------------------------------
DROP TABLE IF EXISTS `proveedor`;
CREATE TABLE `proveedor` (
  `id_proveedor` int NOT NULL AUTO_INCREMENT,
  `email` varchar(255) NOT NULL,
  `es_activo` bit(1) NOT NULL,
  `nit` varchar(255) NOT NULL,
  `nombre_contacto` varchar(255) NOT NULL,
  `nrc` varchar(255) NOT NULL,
  `razon_social` varchar(255) NOT NULL,
  `telefono` varchar(255) NOT NULL,
  PRIMARY KEY (`id_proveedor`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `proveedor` VALUES (1, 'ventas@distribuidoradental.com', true, '0614-010190-101-1', 'Ana Lopez', '123456-7', 'Distribuidora Dental S.A.', '2222-3333');

-- --------------------------------------------------------
-- Estructura y datos para la tabla `registro_acceso`
-- --------------------------------------------------------
DROP TABLE IF EXISTS `registro_acceso`;
CREATE TABLE `registro_acceso` (
  `id_registro_acceso` int NOT NULL AUTO_INCREMENT,
  `es_exitoso` bit(1) NOT NULL,
  `fecha_acceso` datetime(6) NOT NULL,
  `id_usuario` int NOT NULL,
  PRIMARY KEY (`id_registro_acceso`),
  KEY `FKn01v4h948g7i1mugil8mdsbk8` (`id_usuario`),
  CONSTRAINT `FKn01v4h948g7i1mugil8mdsbk8` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB AUTO_INCREMENT=44 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `registro_acceso` VALUES (1, true, '2026-06-01T08:15:30', 1);
INSERT INTO `registro_acceso` VALUES (2, false, '2026-06-01T08:20:45', 1);
INSERT INTO `registro_acceso` VALUES (3, true, '2026-06-02T09:10:12', 2);
INSERT INTO `registro_acceso` VALUES (4, false, '2026-06-02T09:18:33', 2);
INSERT INTO `registro_acceso` VALUES (5, true, '2026-06-03T10:05:27', 3);
INSERT INTO `registro_acceso` VALUES (6, false, '2026-06-03T10:11:50', 3);
INSERT INTO `registro_acceso` VALUES (7, true, '2026-06-04T14:25:10', 1);
INSERT INTO `registro_acceso` VALUES (8, true, '2026-06-04T14:30:22', 2);
INSERT INTO `registro_acceso` VALUES (9, false, '2026-06-05T16:40:05', 3);
INSERT INTO `registro_acceso` VALUES (10, true, '2026-06-05T16:45:18', 3);
INSERT INTO `registro_acceso` VALUES (11, true, '2026-06-13T16:56:32.427407', 1);
INSERT INTO `registro_acceso` VALUES (12, true, '2026-06-13T16:57:29.535875', 2);
INSERT INTO `registro_acceso` VALUES (13, true, '2026-06-13T17:18:13.345634', 1);
INSERT INTO `registro_acceso` VALUES (14, true, '2026-08-24T04:06:26.133248', 1);
INSERT INTO `registro_acceso` VALUES (15, false, '2026-09-14T02:07:58.746954', 1);
INSERT INTO `registro_acceso` VALUES (16, false, '2026-09-14T02:12:17.295748', 3);
INSERT INTO `registro_acceso` VALUES (17, true, '2026-09-14T02:12:51.579150', 3);
INSERT INTO `registro_acceso` VALUES (18, true, '2026-09-14T02:27:00.563638', 1);
INSERT INTO `registro_acceso` VALUES (19, true, '2026-09-19T07:13:28.671449', 1);
INSERT INTO `registro_acceso` VALUES (20, true, '2026-09-19T08:14:00.242915', 1);
INSERT INTO `registro_acceso` VALUES (21, true, '2026-09-19T09:05:35.993470', 1);
INSERT INTO `registro_acceso` VALUES (22, true, '2026-09-19T09:07:37.199723', 1);
INSERT INTO `registro_acceso` VALUES (23, true, '2026-09-20T07:48:00.484596', 1);
INSERT INTO `registro_acceso` VALUES (24, true, '2026-09-20T07:49:52.332754', 2);
INSERT INTO `registro_acceso` VALUES (25, true, '2026-09-20T07:50:04.989604', 1);
INSERT INTO `registro_acceso` VALUES (26, true, '2026-09-20T07:50:14.429262', 2);
INSERT INTO `registro_acceso` VALUES (27, true, '2026-09-20T07:51:42.075019', 1);
INSERT INTO `registro_acceso` VALUES (28, true, '2026-09-20T07:52:32.416015', 3);
INSERT INTO `registro_acceso` VALUES (29, true, '2026-10-04T01:55:57.106524', 1);
INSERT INTO `registro_acceso` VALUES (30, true, '2026-10-17T03:00:01.909665', 1);
INSERT INTO `registro_acceso` VALUES (31, true, '2026-10-17T04:01:36.795738', 1);
INSERT INTO `registro_acceso` VALUES (32, true, '2026-10-17T04:03:28.852060', 1);
INSERT INTO `registro_acceso` VALUES (33, true, '2026-10-17T04:12:49.387903', 1);
INSERT INTO `registro_acceso` VALUES (34, true, '2026-10-17T04:15:37.105211', 1);
INSERT INTO `registro_acceso` VALUES (35, true, '2026-10-17T04:19:03.042920', 1);
INSERT INTO `registro_acceso` VALUES (36, true, '2026-10-17T04:19:43.857220', 1);
INSERT INTO `registro_acceso` VALUES (37, true, '2026-10-17T04:21:31.122855', 1);
INSERT INTO `registro_acceso` VALUES (38, true, '2026-10-21T04:31:15.159494', 1);
INSERT INTO `registro_acceso` VALUES (39, true, '2026-10-21T04:37:36.944562', 3);
INSERT INTO `registro_acceso` VALUES (40, true, '2026-10-21T04:39:27.724528', 1);
INSERT INTO `registro_acceso` VALUES (41, true, '2026-10-22T04:31:37.516574', 3);
INSERT INTO `registro_acceso` VALUES (42, true, '2026-10-22T04:34:26.589010', 1);
INSERT INTO `registro_acceso` VALUES (43, true, '2026-10-22T06:22:44.273969', 1);

-- --------------------------------------------------------
-- Estructura y datos para la tabla `respaldos`
-- --------------------------------------------------------
DROP TABLE IF EXISTS `respaldos`;
CREATE TABLE `respaldos` (
  `id` int NOT NULL AUTO_INCREMENT,
  `creado_en` datetime(6) NOT NULL,
  `estado` varchar(50) NOT NULL,
  `nombre_archivo` varchar(255) NOT NULL,
  `ruta_almacenamiento` varchar(500) NOT NULL,
  `tamanio_bytes` bigint NOT NULL,
  `usuario_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK7xlq83qs90gvbx4iikulnu6k0` (`usuario_id`),
  CONSTRAINT `FK7xlq83qs90gvbx4iikulnu6k0` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- --------------------------------------------------------
-- Estructura y datos para la tabla `rol`
-- --------------------------------------------------------
DROP TABLE IF EXISTS `rol`;
CREATE TABLE `rol` (
  `id_rol` int NOT NULL AUTO_INCREMENT,
  `nombre_rol` enum('ADMIN','ASISTENTEODONTOLOGO','GERENTE','ODONTOLOGO','OTRO','PACIENTE','PROVEEDOR','RECEPCIONISTA') NOT NULL,
  PRIMARY KEY (`id_rol`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `rol` VALUES (1, 'ADMIN');
INSERT INTO `rol` VALUES (2, 'ODONTOLOGO');
INSERT INTO `rol` VALUES (3, 'RECEPCIONISTA');

-- --------------------------------------------------------
-- Estructura y datos para la tabla `tratamiento`
-- --------------------------------------------------------
DROP TABLE IF EXISTS `tratamiento`;
CREATE TABLE `tratamiento` (
  `id_tratamiento` int NOT NULL AUTO_INCREMENT,
  `costo_tratamiento` decimal(10,2) NOT NULL,
  `descripcion_tratamiento` text NOT NULL,
  `nombre_tratamiento` varchar(255) NOT NULL,
  PRIMARY KEY (`id_tratamiento`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `tratamiento` VALUES (1, 45.00, 'Eliminación de sarro y placa bacteriana mediante ultrasonido.', 'Limpieza dental');
INSERT INTO `tratamiento` VALUES (2, 60.00, 'Extracción de pieza dental sin complicaciones quirúrgicas.', 'Extracción simple');
INSERT INTO `tratamiento` VALUES (3, 250.00, 'Tratamiento de conducto para eliminar infección de la pulpa.', 'Endodoncia');
INSERT INTO `tratamiento` VALUES (4, 22.00, 'Blanqueamiento', 'Blanqueamiento');
INSERT INTO `tratamiento` VALUES (5, 1700.00, 'Por 12 meses', 'Tratamiento Brackets');
INSERT INTO `tratamiento` VALUES (6, 1500.00, 'Carias completas', 'Tratamiento Carias');

-- --------------------------------------------------------
-- Estructura y datos para la tabla `usuario`
-- --------------------------------------------------------
DROP TABLE IF EXISTS `usuario`;
CREATE TABLE `usuario` (
  `id_usuario` int NOT NULL AUTO_INCREMENT,
  `apellido_usuario` varchar(255) NOT NULL,
  `email_usuario` varchar(255) NOT NULL,
  `es_activo` bit(1) NOT NULL,
  `nombre_usuario` varchar(255) NOT NULL,
  `passwor_usuario` varchar(255) NOT NULL,
  `username_usuario` varchar(255) NOT NULL,
  `id_rol` int NOT NULL,
  `debe_cambiar_password` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id_usuario`),
  UNIQUE KEY `UKisxwoo508iq2mrksqgheuh3r3` (`email_usuario`),
  UNIQUE KEY `UK3ig8gp5iyg228fbxkrry1aj62` (`username_usuario`),
  KEY `FKmyv3138vvci6kaq3y5kt4cntu` (`id_rol`),
  CONSTRAINT `FKmyv3138vvci6kaq3y5kt4cntu` FOREIGN KEY (`id_rol`) REFERENCES `rol` (`id_rol`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `usuario` VALUES (1, 'García', 'admin@dentalcare.com', true, 'Carlos', '$2a$10$7zDNNSUFBdDdUdli3dm6VeS2HCrhMlxT4JjT9OJt1467qUGujmpqW', 'admin', 1, NULL);
INSERT INTO `usuario` VALUES (2, 'Martínez', 'martinez@dentalcare.com', true, 'Luis', '$2a$10$2VTKj3fe91ewIhJE1Zn1bOpIj6Gm7wwVhanJ7TdhxO/Z658wiCND2', 'dr.martinez', 2, NULL);
INSERT INTO `usuario` VALUES (3, 'López', 'recepcion@dentalcare.com', true, 'Ana', '$2a$10$K3hkqrQb3drZLX2IW/ID3Oolkcr5fhdjG65IqjohA/H1CZbIIlY36', 'Recepcionista01', 3, NULL);
INSERT INTO `usuario` VALUES (4, 'Munguia', 'dr.perez@dentalcare.com', true, 'Waldo', '$2a$10$UL/eWEGwjX01IjfeC3Q8yeq1hmeKraqvqgYISeLSDLFxUWj/r8/ha', 'dr.munguia', 2, NULL);
INSERT INTO `usuario` VALUES (5, 'Vasquez', 'benjamin@gmail.com', true, 'Benjamin', '$2a$10$GEwYYbCDjU1shIPDYNBqZuF1xqAUCF1egX04CjGp4/vLZHNKYQYBe', 'bvasquez', 3, NULL);

SET FOREIGN_KEY_CHECKS = 1;

-- FIN DEL RESPALDO DENTALCARE
