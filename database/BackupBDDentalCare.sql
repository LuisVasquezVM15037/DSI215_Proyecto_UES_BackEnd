CREATE DATABASE  IF NOT EXISTS `bd_dentalcareweb` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `bd_dentalcareweb`;
-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: bd_dentalcareweb
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `cita`
--

DROP TABLE IF EXISTS `cita`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cita` (
  `id_citas` int NOT NULL AUTO_INCREMENT,
  `estado_cita` enum('REPROGRAMADA','PENDIENTE','OTRO','NO_ASISTIO','EN_PROGRESO','PROGRAMADA','CANCELADA','FINALIZADA','COMPLETADA') NOT NULL,
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
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cita`
--

LOCK TABLES `cita` WRITE;
/*!40000 ALTER TABLE `cita` DISABLE KEYS */;
INSERT INTO `cita` VALUES (1,'FINALIZADA','2026-05-10','2026-05-10 09:30:00.000000','2026-05-10 09:00:00.000000',NULL,1,1),(2,'FINALIZADA','2026-05-12','2026-05-12 11:00:00.000000','2026-05-12 10:00:00.000000',NULL,2,2),(3,'FINALIZADA','2026-06-15','2026-06-15 15:00:00.000000','2026-06-15 14:00:00.000000',NULL,3,3),(4,'PROGRAMADA','2026-07-02','2026-07-02 08:45:00.000000','2026-07-02 08:00:00.000000',NULL,1,2),(5,'PROGRAMADA','2026-07-07','2026-07-07 09:30:00.000000','2026-07-07 09:00:00.000000',NULL,2,3),(6,'PROGRAMADA','2026-07-14','2026-07-14 11:00:00.000000','2026-07-14 10:00:00.000000',NULL,3,1),(7,'PROGRAMADA','2026-07-21','2026-07-21 11:45:00.000000','2026-07-21 11:00:00.000000',NULL,1,3),(8,'PROGRAMADA','2026-07-28','2026-07-28 15:00:00.000000','2026-07-28 14:00:00.000000',NULL,2,1),(9,'PROGRAMADA','2026-06-09','2026-06-09 18:00:00.000000','2026-06-09 17:00:00.000000',NULL,3,3),(10,'PROGRAMADA','2026-06-09','2026-06-09 14:00:00.000000','2026-06-09 13:00:00.000000',NULL,2,3);
/*!40000 ALTER TABLE `cita` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `detalle_prescripcion`
--

DROP TABLE IF EXISTS `detalle_prescripcion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detalle_prescripcion`
--

LOCK TABLES `detalle_prescripcion` WRITE;
/*!40000 ALTER TABLE `detalle_prescripcion` DISABLE KEYS */;
INSERT INTO `detalle_prescripcion` VALUES (3,'50',3,'cada 6 h','Tomar con suficiente agua',1,3,NULL),(4,'450',6,'7','fdfdf',1,3,NULL),(5,'344',4,'3','Con mucha agua',1,4,NULL),(6,'500',2,'8','asasa',1,5,NULL),(7,'500',7,'8','Tomar con abundante agua',1,6,NULL);
/*!40000 ALTER TABLE `detalle_prescripcion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `evaluacion_clinica`
--

DROP TABLE IF EXISTS `evaluacion_clinica`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `evaluacion_clinica` (
  `id_evaluacion_clinica` int NOT NULL AUTO_INCREMENT,
  `diagnostico` text NOT NULL,
  `observaciones` text,
  `id_cita` int NOT NULL,
  PRIMARY KEY (`id_evaluacion_clinica`),
  KEY `FKattvruklkmulktk8lbaaq0evw` (`id_cita`),
  CONSTRAINT `FKattvruklkmulktk8lbaaq0evw` FOREIGN KEY (`id_cita`) REFERENCES `cita` (`id_citas`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `evaluacion_clinica`
--

LOCK TABLES `evaluacion_clinica` WRITE;
/*!40000 ALTER TABLE `evaluacion_clinica` DISABLE KEYS */;
INSERT INTO `evaluacion_clinica` VALUES (1,'Acumulación moderada de sarro. Sin caries activas.','Se recomienda limpieza cada 6 meses.',1),(2,'Infección pulpar en pieza 36. Requiere endodoncia.','Paciente refiere dolor intenso hace 3 días.',2),(3,'Gingivitis leve generalizada.','Mejorar técnica de cepillado. Control en 1 mes.',3);
/*!40000 ALTER TABLE `evaluacion_clinica` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `evolucion_tratamiento`
--

DROP TABLE IF EXISTS `evolucion_tratamiento`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `evolucion_tratamiento`
--

LOCK TABLES `evolucion_tratamiento` WRITE;
/*!40000 ALTER TABLE `evolucion_tratamiento` DISABLE KEYS */;
/*!40000 ALTER TABLE `evolucion_tratamiento` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `medicamento`
--

DROP TABLE IF EXISTS `medicamento`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `medicamento`
--

LOCK TABLES `medicamento` WRITE;
/*!40000 ALTER TABLE `medicamento` DISABLE KEYS */;
INSERT INTO `medicamento` VALUES (1,150,'Amoxicilina trihidratada','500 mg',12.50,'Amoxicilina',1);
/*!40000 ALTER TABLE `medicamento` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `movimiento_inventario`
--

DROP TABLE IF EXISTS `movimiento_inventario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `movimiento_inventario`
--

LOCK TABLES `movimiento_inventario` WRITE;
/*!40000 ALTER TABLE `movimiento_inventario` DISABLE KEYS */;
/*!40000 ALTER TABLE `movimiento_inventario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `odontologo`
--

DROP TABLE IF EXISTS `odontologo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `odontologo` (
  `id_odontologo` int NOT NULL AUTO_INCREMENT,
  `especialidad_odontologo` varchar(255) NOT NULL,
  `jvpo_id` varchar(255) NOT NULL,
  `id_usuario` int NOT NULL,
  PRIMARY KEY (`id_odontologo`),
  UNIQUE KEY `UKjlqchk81fefnkidejt3fyfe2p` (`jvpo_id`),
  KEY `FK679oebsued1bcdx9ymqvywme4` (`id_usuario`),
  CONSTRAINT `FK679oebsued1bcdx9ymqvywme4` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `odontologo`
--

LOCK TABLES `odontologo` WRITE;
/*!40000 ALTER TABLE `odontologo` DISABLE KEYS */;
INSERT INTO `odontologo` VALUES (1,'Ortodoncia','JVPO-001',1),(2,'Endodoncia','JVPO-002',2),(3,'Periodoncia','JVPO-003',3);
/*!40000 ALTER TABLE `odontologo` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `paciente`
--

DROP TABLE IF EXISTS `paciente`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `paciente`
--

LOCK TABLES `paciente` WRITE;
/*!40000 ALTER TABLE `paciente` DISABLE KEYS */;
INSERT INTO `paciente` VALUES (1,'Hernández','Pedro Hernández 7999-0001','maria@gmail.com','1990-03-15','María','01234567-8','7111-2222','Ninguna conocida'),(2,'Castillo','Laura Castillo 7888-0002','roberto@gmail.com','1985-07-22','Roberto','09876543-2','7333-4444','Penicilina'),(3,'Ramírez','Carlos Ramírez 7777-0003','sofia@gmail.com','2000-11-05','Sofía','05678901-3','7555-6666','Ibuprofeno');
/*!40000 ALTER TABLE `paciente` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `plan_tratamiento`
--

DROP TABLE IF EXISTS `plan_tratamiento`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `plan_tratamiento` (
  `id_plan_tratamiento` int NOT NULL AUTO_INCREMENT,
  `estado_plan` enum('CANCELADO','COMPLETADO','EN_PROGRESO','OTRO','PENDIENTE','PROGRAMADO') NOT NULL,
  `pieza_dental` int NOT NULL,
  `id_evaluacion_clinica` int NOT NULL,
  `id_tratamiento` int NOT NULL,
  PRIMARY KEY (`id_plan_tratamiento`),
  KEY `FK2b3dnlkcefo1thkg9mymnta5s` (`id_evaluacion_clinica`),
  KEY `FKgn3xdbccu9ed1ebr5s016r12q` (`id_tratamiento`),
  CONSTRAINT `FK2b3dnlkcefo1thkg9mymnta5s` FOREIGN KEY (`id_evaluacion_clinica`) REFERENCES `evaluacion_clinica` (`id_evaluacion_clinica`),
  CONSTRAINT `FKgn3xdbccu9ed1ebr5s016r12q` FOREIGN KEY (`id_tratamiento`) REFERENCES `tratamiento` (`id_tratamiento`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `plan_tratamiento`
--

LOCK TABLES `plan_tratamiento` WRITE;
/*!40000 ALTER TABLE `plan_tratamiento` DISABLE KEYS */;
INSERT INTO `plan_tratamiento` VALUES (1,'COMPLETADO',11,1,1),(2,'EN_PROGRESO',36,2,3),(3,'PENDIENTE',48,3,2);
/*!40000 ALTER TABLE `plan_tratamiento` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `prescripcion`
--

DROP TABLE IF EXISTS `prescripcion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `prescripcion` (
  `id_prescripcion` int NOT NULL AUTO_INCREMENT,
  `fecha_prescripcion` datetime(6) NOT NULL,
  `id_cita` int NOT NULL,
  PRIMARY KEY (`id_prescripcion`),
  KEY `FKidwd4kw198kb3bvabbr727shn` (`id_cita`),
  CONSTRAINT `FKidwd4kw198kb3bvabbr727shn` FOREIGN KEY (`id_cita`) REFERENCES `cita` (`id_citas`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `prescripcion`
--

LOCK TABLES `prescripcion` WRITE;
/*!40000 ALTER TABLE `prescripcion` DISABLE KEYS */;
/*!40000 ALTER TABLE `prescripcion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `proveedor`
--

DROP TABLE IF EXISTS `proveedor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `proveedor`
--

LOCK TABLES `proveedor` WRITE;
/*!40000 ALTER TABLE `proveedor` DISABLE KEYS */;
INSERT INTO `proveedor` VALUES (1,'ventas@insumosdentales.com',_binary '','0614-010190-101-1','Carlos Méndez','123456-7','Insumos Dentales El Salvador S.A. de C.V.','2255-8899');
/*!40000 ALTER TABLE `proveedor` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `registro_acceso`
--

DROP TABLE IF EXISTS `registro_acceso`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `registro_acceso` (
  `id_registro_acceso` int NOT NULL AUTO_INCREMENT,
  `es_exitoso` bit(1) NOT NULL,
  `fecha_acceso` datetime(6) NOT NULL,
  `id_usuario` int NOT NULL,
  PRIMARY KEY (`id_registro_acceso`),
  KEY `FKn01v4h948g7i1mugil8mdsbk8` (`id_usuario`),
  CONSTRAINT `FKn01v4h948g7i1mugil8mdsbk8` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `registro_acceso`
--

LOCK TABLES `registro_acceso` WRITE;
/*!40000 ALTER TABLE `registro_acceso` DISABLE KEYS */;
INSERT INTO `registro_acceso` VALUES (1,_binary '','2026-06-01 08:15:30.000000',1),(2,_binary '\0','2026-06-01 08:20:45.000000',1),(3,_binary '','2026-06-02 09:10:12.000000',2),(4,_binary '\0','2026-06-02 09:18:33.000000',2),(5,_binary '','2026-06-03 10:05:27.000000',3),(6,_binary '\0','2026-06-03 10:11:50.000000',3),(7,_binary '','2026-06-04 14:25:10.000000',1),(8,_binary '','2026-06-04 14:30:22.000000',2),(9,_binary '\0','2026-06-05 16:40:05.000000',3),(10,_binary '','2026-06-05 16:45:18.000000',3),(11,_binary '\0','2026-06-09 19:20:42.174437',1),(12,_binary '\0','2026-06-09 19:20:45.285565',1),(13,_binary '','2026-06-09 19:21:23.265724',1),(14,_binary '','2026-06-09 22:13:08.696586',1),(15,_binary '','2026-06-09 22:15:08.362542',1),(16,_binary '\0','2026-06-09 22:31:17.382342',1),(17,_binary '','2026-06-09 22:31:22.632201',1),(18,_binary '','2026-06-09 22:32:19.502286',2),(19,_binary '\0','2026-06-09 22:32:59.735661',3),(20,_binary '\0','2026-06-09 22:33:05.202724',3),(21,_binary '','2026-06-09 22:33:44.980061',3),(22,_binary '','2026-06-09 23:43:49.750948',1);
/*!40000 ALTER TABLE `registro_acceso` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rol`
--

DROP TABLE IF EXISTS `rol`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rol` (
  `id_rol` int NOT NULL AUTO_INCREMENT,
  `nombre_rol` enum('ADMIN','ASISTENTEODONTOLOGO','GERENTE','ODONTOLOGO','OTRO','PACIENTE','PROVEEDOR','RECEPCIONISTA') NOT NULL,
  PRIMARY KEY (`id_rol`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rol`
--

LOCK TABLES `rol` WRITE;
/*!40000 ALTER TABLE `rol` DISABLE KEYS */;
INSERT INTO `rol` VALUES (1,'ADMIN'),(2,'ODONTOLOGO'),(3,'RECEPCIONISTA');
/*!40000 ALTER TABLE `rol` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tratamiento`
--

DROP TABLE IF EXISTS `tratamiento`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tratamiento` (
  `id_tratamiento` int NOT NULL AUTO_INCREMENT,
  `costo_tratamiento` decimal(10,2) NOT NULL,
  `descripcion_tratamiento` text NOT NULL,
  `nombre_tratamiento` varchar(255) NOT NULL,
  PRIMARY KEY (`id_tratamiento`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tratamiento`
--

LOCK TABLES `tratamiento` WRITE;
/*!40000 ALTER TABLE `tratamiento` DISABLE KEYS */;
INSERT INTO `tratamiento` VALUES (1,45.00,'Eliminación de sarro y placa bacteriana mediante ultrasonido.','Limpieza dental'),(2,60.00,'Extracción de pieza dental sin complicaciones quirúrgicas.','Extracción simple'),(3,250.00,'Tratamiento de conducto para eliminar infección de la pulpa.','Endodoncia');
/*!40000 ALTER TABLE `tratamiento` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuario`
--

DROP TABLE IF EXISTS `usuario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario` (
  `id_usuario` int NOT NULL AUTO_INCREMENT,
  `apellido_usuario` varchar(255) NOT NULL,
  `email_usuario` varchar(255) NOT NULL,
  `es_activo` bit(1) NOT NULL,
  `nombre_usuario` varchar(255) NOT NULL,
  `passwor_usuario` varchar(255) NOT NULL,
  `username_usuario` varchar(255) NOT NULL,
  `id_rol` int NOT NULL,
  PRIMARY KEY (`id_usuario`),
  UNIQUE KEY `UKisxwoo508iq2mrksqgheuh3r3` (`email_usuario`),
  UNIQUE KEY `UK3ig8gp5iyg228fbxkrry1aj62` (`username_usuario`),
  KEY `FKmyv3138vvci6kaq3y5kt4cntu` (`id_rol`),
  CONSTRAINT `FKmyv3138vvci6kaq3y5kt4cntu` FOREIGN KEY (`id_rol`) REFERENCES `rol` (`id_rol`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuario`
--

LOCK TABLES `usuario` WRITE;
/*!40000 ALTER TABLE `usuario` DISABLE KEYS */;
INSERT INTO `usuario` VALUES (1,'García','admin@dentalcare.com',_binary '','Carlos','$2a$10$7zDNNSUFBdDdUdli3dm6VeS2HCrhMlxT4JjT9OJt1467qUGujmpqW','admin',1),(2,'Martínez','martinez@dentalcare.com',_binary '','Luis','$2a$10$2VTKj3fe91ewIhJE1Zn1bOpIj6Gm7wwVhanJ7TdhxO/Z658wiCND2','dr.martinez',2),(3,'López','recepcion@dentalcare.com',_binary '','Ana','$2a$10$Gdqj0eT59an0V051Xx470eym3FJ8XwEB/GousHpL3TKCstW/c1mi2','recepcion1',3),(4,'Vasquez','vm15037@ues.edu.sv',_binary '','Luis','$2a$10$lTbbWjrjzi9Aeu/kzMQbheJuqiokuHyHmGAh6CgRXyq8hY9WeyZc6','luis.vasquez',1);
/*!40000 ALTER TABLE `usuario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'bd_dentalcareweb'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-09-10 11:48:42
