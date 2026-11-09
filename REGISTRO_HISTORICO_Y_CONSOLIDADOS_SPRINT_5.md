# REGISTRO HISTÓRICO Y CONSOLIDADO DE CAMBIOS — SPRINT 5

> **Proyecto:** Sistema Informático para la Gestión de Servicios Médicos en la Clínica Odontológica DentalCare ERP  
> **Cátedra:** Diseño de Sistemas II (DSI215) — Ciclo II-2026  
> **Institución:** Universidad de El Salvador (UES)  
> **Documento Oficial de Evaluación:** Bitácora consolidada de modificaciones, componentes, servicios, endpoints y estado de implementación del Sprint 5 para la evaluación de cátedra.

---

## 1. TABLERO GENERAL DE AVANCE DEL SPRINT 5

| Fase | Subsistema / Módulo | Rol Responsable | PBIs Cubiertos | Story Points | Estado Técnico |
| :---: | :--- | :--- | :--- | :---: | :---: |
| **Fase 1** | **Agenda Avanzada, Recordatorios (Día Anterior) y Notificaciones** | **Recepcionista / Secretaria** | `DENTALCARE-71`, `DENTALCARE-96`, `DENTALCARE-92` | **7.0 SP** | **COMPLETADA (100%)** |
| **Fase 2** | **Monitoreo Operativo & Auditoría de Stock Crítico** | **Asistente Dental & Gerente** | `DENTALCARE-49`, `DENTALCARE-73` | **5.0 SP** | **COMPLETADA (100%)** |
| **Fase 3** | **Inteligencia de Negocios, Reportes y Rendimiento** | **Gerente** | `DENTALCARE-70`, `DENTALCARE-72`, `DENTALCARE-74`, `DENTALCARE-95` | **9.0 SP** | **COMPLETADA (100%)** |
| **TOTAL** | **Consolidado General Sprint 5** | **Recepcionista, Asistente, Gerente** | **9 Historias de Usuario (PBIs)** | **21.0 SP** | **21.0 SP / 21.0 SP (100% COMPLETADO)** |

---

## 2. CONSOLIDADO DETALLADO — FASE 1: AGENDA AVANZADA, RECORDATORIOS Y NOTIFICACIONES

### 2.1 Historias de Usuario Implementadas y Verificación de Criterios de Aceptación (AC)

#### 1. `DENTALCARE-71` — PBI-Secret-16: Revisar notificaciones (2.0 SP)
* **Objetivo:** Disponer de una bandeja centralizada de eventos del sistema (citas próximas, lista de espera, stock y rendimiento) con capacidades de lectura individual y masiva.
* **Criterios de Aceptación Cumplidos:**
  - [x] **Lista de Notificaciones Recientes:** Consulta unificada de eventos ordenados cronológicamente (`fecha_creacion DESC`) vía `GET /api/notificaciones`.
  - [x] **Contador Reactivo (Badge):** Endpoint ligero `GET /api/notificaciones/resumen-no-leidas` que actualiza el badge en el Navbar cada 30 segundos.
  - [x] **Marcar como Leída Individualmente:** Endpoint `PATCH /api/notificaciones/{id}/marcar-leida` y botón de verificación interactivo por tarjeta.
  - [x] **Marcar Todas como Leídas:** Endpoint `POST /api/notificaciones/marcar-todas-leidas` con confirmación sutil.
  - [x] **Actualización Visual Instantánea:** TanStack State y reactividad en cliente sin recargar la página.
  - [x] **Bandeja Completa:** Nueva vista institucional `/notificaciones` (`NotificacionesPage.jsx`) con filtros por categoría, buscador y navegación contextual (`rutaRedireccion`).

#### 2. `DENTALCARE-96` — PBI-Secret-15: Configurar Alertas de Citas Próximas (2.0 SP)
* **Objetivo:** Configurar y emitir recordatorios preventivos a los pacientes con citas programadas según la regla clínica del negocio (anticipación de 1 día antes / día anterior).
* **Criterios de Aceptación Cumplidos:**
  - [x] **Regla de Negocio de Notificación Anticipada (1 Día Antes):** Configuración predeterminada fijada en `"DIA_ANTERIOR"` (1,440 minutos / 24 horas antes) en lugar de una alerta tardía de 30 minutos, permitiendo contactar oportunamente a los pacientes para confirmar asistencia.
  - [x] **Multicanalidad Configurable:** Preferencias para envío por WhatsApp y/o Correo Electrónico Institucional, así como conmutador de aviso sonoro sintetizado.
  - [x] **Plantilla de Mensaje Dinámica:** Plantilla personalizable con variables dinámicas `{paciente}`, `{fecha}`, `{hora}`, `{doctor}` para generar el mensaje institucional.
  - [x] **Banner de Alerta Proactiva:** Componente `CitaProximaAlertBanner.jsx` montado en la cabecera de la agenda que detecta citas del día de mañana pendientes de confirmación y ofrece un botón de acceso directo `[Enviar Recordatorios]`.
  - [x] **Persistencia por Usuario:** Entidad `configuracion_alerta_cita` con endpoints `GET` y `PUT` en `/api/agenda/alertas-citas/configuracion`.

#### 3. `DENTALCARE-92` — PBI-Secret-14: Gestionar Lista de Espera, Recordatorios y Cierre de Citas Vencidas (3.0 SP)
* **Objetivo:** Cargar la lista de citas programadas para el día de mañana (o fecha seleccionada), permitir envíos masivos o individuales por WhatsApp y Correo, gestionar la reasignación de cupos liberados, y ejecutar la transición automática de citas vencidas a estado `NO_ASISTIO` tras la medianoche.
* **Criterios de Aceptación Cumplidos:**
  - [x] **Transición Automática a No Asistió (Medianoche / 11:59 PM):** Toda cita programada o pendiente para el día actual que no haya sido atendida (`COMPLETADA`/`FINALIZADA`) ni reprogramada (`REPROGRAMADA`), al pasar las 11:59 PM (cuando concluye la fecha y `fechaCita < hoy`), cambia automáticamente a `NO_ASISTIO`.
  - [x] **Mantenimiento Programado Triple (Cron + Hook JIT + Cold Start):**
    * **Cron Diario a las 00:01 AM:** Clase `CitaScheduler.java` con anotación `@Scheduled(cron = "0 1 0 * * ?", zone = "America/El_Salvador")` que corre automáticamente después de las 11:59 PM en la zona horaria de El Salvador.
    * **Hook en Consulta Just-in-Time:** Invocación atómica en `CitaService.obtenerTodas()` para que cualquier consulta de la interfaz refleje el estado actualizado al instante.
    * **Cold Start / Reinicio:** Evento `@EventListener(ApplicationReadyEvent.class)` que pone al día el sistema ante apagados del servidor.
  - [x] **Resumen Diario Visual:** En `AppointmentPage.jsx`, el bloque de resumen contabiliza y resalta en rojo/rosa la métrica de `No asistió`.
  - [x] **Carga Automática de Citas de Mañana:** Endpoint `GET /api/agenda/alertas-citas/recordatorios?fecha=YYYY-MM-DD` que lee las citas activas programadas para el día seleccionado.
  - [x] **Botón de Envío Masivo "Enviar a Todos":** Botón de acción destacado en `RecordatoriosCitasPanel.jsx` con selección entre Correo Masivo (simulado en 1 clic) o Asistente Secuencial de WhatsApp.

---

## 3. CONSOLIDADO DETALLADO — FASE 2: MONITOREO OPERATIVO & AUDITORÍA DE STOCK CRÍTICO

### 3.1 Historias de Usuario Implementadas y Verificación de Criterios de Aceptación (AC)

#### 1. `DENTALCARE-49` — PBI-AsisDent-05: Revisar Alertas de Stock (Operativo - 3.0 SP)
* **Rol Responsable:** Asistente Dental
* **Objetivo:** Proporcionar al Asistente Dental una consola operativa de existencias que detecta materiales y medicamentos bajo umbral mínimo (`stockActual <= stockMinimo`), destaca los 3 insumos más urgentes (Top 3), ofrece abastecimiento directo con desaparición reactiva de alertas y exportación a Excel (.xlsx).
* **Criterios de Aceptación Cumplidos:**
  - [x] **Listado de Insumos Próximos a Agotarse:** Consulta unificada de materiales y medicamentos bajo umbral mínimo (`stockActual <= stockMinimo`) vía `GET /api/inventario/alertas`.
  - [x] **Resumen Destacado Top 3 Críticos:** Componente visual con tarjetas en degradado rojo/naranja que identifica los 3 insumos con menor cobertura porcentual de stock vía `GET /api/inventario/alertas/top3`.
  - [x] **Opciones y Botones de Abastecimiento Directo:** Botón `[ + Abastecer / Registrar ]` en cada fila y botón `[ Abastecer Inmediato ]` en las tarjetas Top 3 que abre el modal `AbastecerModal` con preselección del insumo.
  - [x] **Desaparición Automática Reactiva:** Al completar un abastecimiento cuya suma supera el stock mínimo, el insumo se actualiza y la alerta desaparece inmediatamente de la lista sin recargar la página.
  - [x] **Exportación a Hoja de Cálculo Excel (.xlsx):** Endpoint `GET /api/inventario/alertas/exportar-excel` generado con **Apache POI 5.2.5** (`XSSFWorkbook`) con cabeceras corporativas en azul DentalCare, autoajuste de columnas y celdas condicionales semafóricas.
  - [x] **Notificaciones Automáticas:** Al confirmar consumos en `InventarioService.confirmarConsumo`, si las existencias caen al mínimo o por debajo, se emiten notificaciones automáticas tanto al rol `ASISTENTEODONTOLOGO` como a `GERENTE`.

#### 2. `DENTALCARE-73` — PBI-Gerencia-03: Visualizar Alertas De Stock (Gerencial / Solo Lectura - 2.0 SP)
* **Rol Responsable:** Gerente
* **Objetivo:** Proporcionar a la Gerencia una pantalla analítica y de supervisión estratégica sobre los insumos críticos de la clínica, bajo el principio de **segregación estricta de funciones** (solo lectura, sin facultades operativas de modificación).
* **Criterios de Aceptación Cumplidos:**
  - [x] **Lista de Insumos Críticos para Supervisión:** Muestra materiales y medicamentos que han alcanzado o están por debajo del stock mínimo vía `GET /api/gerencia/stock-critico`.
  - [x] **Detalle Informativo Completo:** Refleja código, nombre, tipo, cantidad actual, cantidad mínima permitida, déficit de unidades, porcentaje de cobertura y días estimados de agotamiento.
  - [x] **Ordenamiento Multicriterio por Nivel de Urgencia:** Selector con ordenamiento por Urgencia (`urgencia_desc`), Mayor Déficit (`deficit_desc`), Nombre Alfabético (`nombre_asc`) y Menor Cobertura (`cobertura_asc`).
  - [x] **Regla de Negocio Restrictiva (Solo Lectura):**
    * **En Frontend:** Vista institucional `/gerencia/stock-critico` con badge `Modo Auditoría / Solo Lectura`, banner de gobernanza clínica y ausencia total de botones de modificación o abastecimiento.
    * **En Backend (Segregación de Funciones):** `GerenciaStockController` intercepta cualquier intento de petición `POST`, `PUT`, `PATCH` o `DELETE` retornando inmediatamente un código **`403 FORBIDDEN`** (`SOLO_LECTURA`).
  - [x] **Exportación Oficial de Auditoría en Excel:** Endpoint `GET /api/gerencia/stock-critico/exportar-excel` (`Auditoria_Stock_Critico_DentalCare_YYYYMMDD.xlsx`).
  - [x] **Herramienta de Verificación de Seguridad:** Botón interactivo `[Verificar Regla Solo Lectura]` en la cabecera que simula un intento de mutación y confirma la respuesta 403 Forbidden del backend.

---

## 4. CONSOLIDADO DETALLADO — FASE 3: INTELIGENCIA DE NEGOCIOS, REPORTES Y RENDIMIENTO (ROL: GERENTE)

### 4.1 Historias de Usuario Implementadas y Verificación de Criterios de Aceptación (AC)

#### 1. `DENTALCARE-70` — PBI-Gerencia-01: Seleccionar Reporte (2.0 SP)
* **Rol Responsable:** Gerente
* **Épica:** `DENTALCARE-44` (Reportes)
* **Objetivo:** Permitir al Gerente elegir el tipo de análisis institucional a realizar mediante un catálogo interactivo de reportes institucionales.
* **Criterios de Aceptación Cumplidos:**
  - [x] **Catálogo de Reportes Disponibles:** Endpoint `GET /api/gerencia/reportes/tipos` que retorna los 2 grandes reportes estructurados:
    1. **Consumo de Materiales y Medicamentos Clínicos (`CONSUMO_MATERIALES`):** Análisis cuantitativo de insumos y fármacos consumidos en procedimientos y consultas.
    2. **Número de Atenciones y Productividad Odontológica (`NUMERO_ATENCIONES`):** Estadísticas de pacientes atendidos, citas concluidas, canceladas y productividad por doctor.
  - [x] **Selección Flexible del Período de Tiempo:** Presets de selección rápida en un clic para "Mes Actual", "Mes Anterior", "Últimos 30 Días" y rango personalizado mediante date pickers.
  - [x] **Carga Dinámica:** Al seleccionar cualquier tarjeta del catálogo o cambiar el período, el sistema carga reactivamente los datos sin recargar la página.

#### 2. `DENTALCARE-72` — PBI-Gerencia-02: Aplicar Filtros a Reportes (2.0 SP)
* **Rol Responsable:** Gerente
* **Épica:** `DENTALCARE-44` (Reportes)
* **Objetivo:** Segmentar y profundizar en los datos del reporte seleccionado mediante filtros dinámicos combinables.
* **Criterios de Aceptación Cumplidos:**
  - [x] **Filtrado por Rango de Fechas:** Filtros combinables `fechaInicio` y `fechaFin` con validación cruzada en backend y frontend.
  - [x] **Filtrado por Categoría de Insumo:** En el reporte de consumos, selector dinámico para filtrar entre "Todos", "Solo Materiales Odontológicos" o "Solo Medicamentos Clínicos".
  - [x] **Filtrado por Estado de Citas:** En el reporte de atenciones, selector dinámico para segmentar por "Todos los Estados", "Solo Completadas", "Canceladas" o "No Asistió".
  - [x] **Actualización Instantánea de Resultados:** Endpoint `GET /api/gerencia/reportes/datos` con refresco reactivo automático ante cualquier cambio de filtro.

#### 3. `DENTALCARE-74` — PBI-Gerencia-04: Visualizar Resultados de Reportes (3.0 SP)
* **Rol Responsable:** Gerente
* **Épica:** `DENTALCARE-45` (Stock Críticos / Reportes)
* **Objetivo:** Consola visual de resultados que combina métricas resumen (KPI Cards), visualizaciones gráficas interactivas y tabla detallada con opciones de exportación oficial a Excel (.xlsx) e impresión adaptada.
* **Criterios de Aceptación Cumplidos:**
  - [x] **Bloque de KPIs Ejecutivos:** 4 tarjetas superiores con métricas consolidadas (Total atenciones/consumos, tasa de efectividad de citas, doctor líder de productividad, categoría predominante y promedios diarios).
  - [x] **Gráfico Estadístico de Tendencia Temporal:** Gráfico de barras interactivo SVG que muestra la evolución diaria/semanal en el período con tooltips flotantes al pasar el cursor y diferenciación visual de cancelaciones.
  - [x] **Gráfico de Distribución Analítica:** Desglose porcentual con barras de progreso codificadas por color (para categorías de insumos o atenciones por doctor).
  - [x] **Tabla Detallada de Resultados:** Tabla consolidada de registros con buscador en tiempo real, badges de estado y trazabilidad completa de pacientes y doctores.
  - [x] **Exportación a Hoja de Cálculo Excel (.xlsx):** Endpoint `GET /api/gerencia/reportes/exportar` generado con **Apache POI 5.2.5** con membrete oficial, bloques de KPIs y tabla autoajustada.
  - [x] **Impresión Oficial Estilizada:** Botón `[ Imprimir Reporte ]` con estilos CSS `@media print` en `index.css` que ocultan menús, barras de navegación y botones, presentando el reporte como documento formal de gerencia.

#### 4. `DENTALCARE-95` — PBI-Gerencia-05: Configurar Alertas de Rendimiento (2.0 SP)
* **Rol Responsable:** Gerente
* **Épica:** `DENTALCARE-32` (Notificaciones)
* **Objetivo:** Permitir a la Gerencia establecer umbrales automáticos de control de calidad y rendimiento operativo, disparando notificaciones ante violaciones de umbral.
* **Criterios de Aceptación Cumplidos:**
  - [x] **Definición de Indicadores y Umbrales:** Formulario modal en `GerenciaReportesPage.jsx` (pestaña *"Alertas de Rendimiento"*) para configurar:
    * Nombre del indicador y descripción.
    * Métrica supervisada: `MAX_CANCELACIONES_PORCENTAJE`, `ATENCIONES_MINIMAS`, `STOCK_CRITICO_CANTIDAD`, `TIEMPO_ESPERA_PROMEDIO_MINS`.
    * Operador de comparación: `MAYOR_QUE` (>), `MAYOR_IGUAL_QUE` (≥), `MENOR_QUE` (<), `MENOR_IGUAL_QUE` (≤).
    * Valor umbral límite numérico.
    * Frecuencia de evaluación: `SEMANAL`, `DIARIO`, `MENSUAL`.
  - [x] **Envío Automático de Notificación ante Cruce de Umbral:** El motor de evaluación calcula la métrica en la base de datos; si se viola el umbral, actualiza el estado de la regla a disparada y emite una notificación al rol `GERENTE` (`TipoNotificacion.RENDIMIENTO`) con redirección directa a la consola de rendimiento.
  - [x] **Edición, Eliminación y Switch de Activación:** Cada regla cuenta con switch de activación reactivo (`PATCH /toggle`), botón de edición modal y eliminación confirmada.
  - [x] **Evaluación Manual y Programada:**
    * Botón `[ Evaluar Indicadores Ahora ]` (`POST /api/gerencia/alertas-rendimiento/evaluar`) para auditoría inmediata.
    * Tarea programada semanal en Spring Boot (`@Scheduled(cron = "0 0 8 * * MON", zone = "America/El_Salvador")`) y comprobación de seguridad periódica.

---

## 5. INVENTARIO COMPLETO DE ARCHIVOS CREADOS Y MODIFICADOS EN FASE 3

### A. Backend (Spring Boot 3 + Java 17 + MySQL 8.0 + Apache POI)
* **Nuevos Enums JPA:**
  - `src/main/java/com/dentalcare/api/models/enums/TipoMetricaRendimiento.java` (`ATENCIONES_MINIMAS`, `MAX_CANCELACIONES_PORCENTAJE`, `STOCK_CRITICO_CANTIDAD`, `TIEMPO_ESPERA_PROMEDIO_MINS`).
  - `src/main/java/com/dentalcare/api/models/enums/OperadorComparacion.java` (`MENOR_QUE`, `MENOR_IGUAL_QUE`, `MAYOR_QUE`, `MAYOR_IGUAL_QUE`).
  - `src/main/java/com/dentalcare/api/models/enums/PeriodoEvaluacion.java` (`DIARIO`, `SEMANAL`, `MENSUAL`).
  - `src/main/java/com/dentalcare/api/models/enums/TipoReporte.java` (`CONSUMO_MATERIALES`, `NUMERO_ATENCIONES`).
* **Nueva Entidad JPA:**
  - `src/main/java/com/dentalcare/api/models/ReglaAlertaRendimiento.java` (Mapeo tabla `reglas_alerta_rendimiento`).
* **Nuevo Repositorio Spring Data JPA:**
  - `src/main/java/com/dentalcare/api/repositories/ReglaAlertaRendimientoRepository.java`
* **Consultas por Rango de Fechas en Repositorios Existentes:**
  - `src/main/java/com/dentalcare/api/repositories/CitaRepository.java` (Método `findCitasEntreFechas`).
  - `src/main/java/com/dentalcare/api/repositories/ConsumoRepository.java` (Método `findConsumosEntreFechas`).
* **Nuevos Data Transfer Objects (DTOs):**
  - `src/main/java/com/dentalcare/api/dtos/Reporte/TipoReporteDTO.java`
  - `src/main/java/com/dentalcare/api/dtos/Reporte/ReporteKpisDTO.java`
  - `src/main/java/com/dentalcare/api/dtos/Reporte/ReporteTendenciaDTO.java`
  - `src/main/java/com/dentalcare/api/dtos/Reporte/ReporteDistribucionDTO.java`
  - `src/main/java/com/dentalcare/api/dtos/Reporte/ReporteDetalleItemDTO.java`
  - `src/main/java/com/dentalcare/api/dtos/Reporte/ReporteResponseDTO.java`
  - `src/main/java/com/dentalcare/api/dtos/Reporte/ReglaAlertaRendimientoRequestDTO.java`
  - `src/main/java/com/dentalcare/api/dtos/Reporte/ReglaAlertaRendimientoResponseDTO.java`
  - `src/main/java/com/dentalcare/api/dtos/Reporte/EvaluacionReglasResumenDTO.java`
* **Nuevos Servicios de Negocio:**
  - `src/main/java/com/dentalcare/api/services/ReporteGerenciaService.java` (Catálogo, motor analítico de agregación de consumos y atenciones, generación de series temporales y exportador binario Excel Apache POI).
  - `src/main/java/com/dentalcare/api/services/RendimientoService.java` (CRUD de reglas, motor de evaluación de umbrales, disparo de alertas al Centro de Notificaciones y scheduler cron).
* **Nuevo Controlador REST:**
  - `src/main/java/com/dentalcare/api/controllers/GerenciaReportesController.java` (`/api/gerencia/reportes/tipos`, `/reportes/datos`, `/reportes/exportar`, `/alertas-rendimiento`).

### B. Frontend (React 18 + Vite + TailwindCSS)
* **Nuevo Servicio API:**
  - `src/services/reporteGerencia.service.js` (Llamadas a catálogo, datos analíticos, exportación Excel con JWT y CRUD completo de reglas de rendimiento).
* **Nueva Vista Institucional:**
  - `src/views/GerenciaReportesPage.jsx` (Consola con 2 pestañas: *"Reportes & Analítica"* con catálogo, presets, filtros dinámicos, 4 KPIs, gráfico temporal de barras SVG, gráfico de distribución porcentual, tabla detallada e impresión, y pestaña *"Alertas de Rendimiento"* con tarjetas de indicadores, badges de estado, switches de activación y modal de creación/edición).
* **Componentes y Enrutamiento Modificados:**
  - `src/App.jsx` (Ruta protegida perezosa `/gerencia/reportes` con guard `RoleRoute [ROLES.ADMIN, ROLES.GERENTE]`).
  - `src/components/Layout.jsx` (Enlace en la barra lateral "Reportes y BI" para roles Gerente y Administrador).
  - `src/index.css` (Estilos de impresión `@media print` para reporte limpio sin barras laterales).

---

## 6. MATRIZ MAESTRA CONSOLIDADA DE ENDPOINTS — SPRINT 5 (100% COMPLETADO)

| Método | Endpoint URI | Roles Autorizados | Estado Técnico | PBI Vinculado | Descripción |
| :---: | :--- | :--- | :---: | :---: | :--- |
| **FASE 1** | | | | | |
| `GET` | `/api/notificaciones` | Todos los autenticados | **COMPLETADO** | `DENTALCARE-71` | Consulta eventos con filtro opcional de no leídas |
| `GET` | `/api/notificaciones/resumen-no-leidas` | Todos los autenticados | **COMPLETADO** | `DENTALCARE-71` | Conteo ligero para badge en Navbar (polling 30s) |
| `PATCH`| `/api/notificaciones/{id}/marcar-leida` | Todos los autenticados | **COMPLETADO** | `DENTALCARE-71` | Marca una notificación específica como leída |
| `POST` | `/api/notificaciones/marcar-todas-leidas` | Todos los autenticados | **COMPLETADO** | `DENTALCARE-71` | Marca todas las notificaciones pendientes como leídas |
| `GET` | `/api/agenda/alertas-citas/configuracion` | RECEPCIONISTA, ADMIN | **COMPLETADO** | `DENTALCARE-96` | Consulta preferencias de anticipación (1 día antes), canales y plantilla |
| `PUT` | `/api/agenda/alertas-citas/configuracion` | RECEPCIONISTA, ADMIN | **COMPLETADO** | `DENTALCARE-96` | Actualiza preferencias de alerta del usuario |
| `GET` | `/api/agenda/alertas-citas/recordatorios` | RECEPCIONISTA, ADMIN | **COMPLETADO** | `DENTALCARE-92` | Carga citas del día siguiente formateadas para WhatsApp y Correo |
| `POST` | `/api/agenda/alertas-citas/enviar-recordatorio-masivo` | RECEPCIONISTA, ADMIN | **COMPLETADO** | `DENTALCARE-92` | Envío masivo con correo simulado y asistente secuencial de WhatsApp |
| `POST` | `/api/citas/sincronizar-vencidas` | RECEPCIONISTA, ADMIN | **COMPLETADO** | `DENTALCARE-92` | Transición forzada de citas concluidas a `NO_ASISTIO` |
| **FASE 2** | | | | | |
| `GET` | `/api/inventario/alertas` | ASISTENTE, ADMIN, GERENTE | **COMPLETADO** | `DENTALCARE-49` | Listado de insumos bajo umbral mínimo con orden dinámico |
| `GET` | `/api/inventario/alertas/top3` | ASISTENTE, ADMIN, GERENTE | **COMPLETADO** | `DENTALCARE-49` | Los 3 insumos con menor cobertura porcentual de stock |
| `GET` | `/api/inventario/alertas/resumen` | ASISTENTE, ADMIN, GERENTE | **COMPLETADO** | `DENTALCARE-49` | Consolidado analítico para KPIs de inventario |
| `GET` | `/api/inventario/alertas/exportar-excel` | ASISTENTE, ADMIN | **COMPLETADO** | `DENTALCARE-49` | Descarga de hoja de cálculo Excel (.xlsx) corporativo |
| `GET` | `/api/gerencia/stock-critico` | GERENTE, ADMIN | **COMPLETADO** | `DENTALCARE-73` | Consola de auditoría de existencias críticas (Solo Lectura) |
| `GET` | `/api/gerencia/stock-critico/top3` | GERENTE, ADMIN | **COMPLETADO** | `DENTALCARE-73` | Inspección visual de insumos con mayor déficit para Gerencia |
| `GET` | `/api/gerencia/stock-critico/resumen` | GERENTE, ADMIN | **COMPLETADO** | `DENTALCARE-73` | Resumen ejecutivo de stock crítico para Gerencia |
| `GET` | `/api/gerencia/stock-critico/exportar-excel` | GERENTE, ADMIN | **COMPLETADO** | `DENTALCARE-73` | Descarga de reporte oficial de auditoría gerencial en Excel |
| `ANY` | `/api/gerencia/stock-critico/**` (POST, PUT, PATCH, DELETE) | Todos | **COMPLETADO (BLOQUEADO)** | `DENTALCARE-73` | **Rechazo estricto con `403 FORBIDDEN` (`SOLO_LECTURA`)** |
| **FASE 3** | | | | | |
| `GET` | `/api/gerencia/reportes/tipos` | GERENTE, ADMIN | **COMPLETADO** | `DENTALCARE-70` | Catálogo de tipos de reportes institucionales disponibles |
| `GET` | `/api/gerencia/reportes/datos` | GERENTE, ADMIN | **COMPLETADO** | `DENTALCARE-72, 74` | Generación analítica de reportes con filtros dinámicos, KPIs y series |
| `GET` | `/api/gerencia/reportes/exportar` | GERENTE, ADMIN | **COMPLETADO** | `DENTALCARE-74` | Exportación oficial del reporte a hoja de cálculo Excel (.xlsx) con Apache POI |
| `GET` | `/api/gerencia/alertas-rendimiento` | GERENTE, ADMIN | **COMPLETADO** | `DENTALCARE-95` | Listado de reglas de control de calidad y umbrales configurados |
| `POST` | `/api/gerencia/alertas-rendimiento` | GERENTE, ADMIN | **COMPLETADO** | `DENTALCARE-95` | Creación de nueva regla de alerta de rendimiento operativo |
| `PUT` | `/api/gerencia/alertas-rendimiento/{id}` | GERENTE, ADMIN | **COMPLETADO** | `DENTALCARE-95` | Modificación de regla de alerta de rendimiento existente |
| `PATCH`| `/api/gerencia/alertas-rendimiento/{id}/toggle` | GERENTE, ADMIN | **COMPLETADO** | `DENTALCARE-95` | Conmutador (toggle) de activación/desactivación de regla |
| `POST` | `/api/gerencia/alertas-rendimiento/evaluar` | GERENTE, ADMIN | **COMPLETADO** | `DENTALCARE-95` | Evaluación manual bajo demanda del motor de indicadores |
| `DELETE`| `/api/gerencia/alertas-rendimiento/{id}` | GERENTE, ADMIN | **COMPLETADO** | `DENTALCARE-95` | Eliminación de regla de alerta de rendimiento |

---

## 7. REGISTRO DE DECISIONES DE ARQUITECTURA DE SOFTWARE (ADRS) — SPRINT 5

### ADR-S5-01 a ADR-S5-08: (Detallados en Fases 1 y 2)
*(Ver registro histórico de notificaciones, recordatorios, cierre nocturno de citas vencidas, segregación 403 solo lectura y exportación Apache POI).*

### ADR-S5-09: Motor de Inteligencia de Negocios Multi-Reporte Desacoplado con Agregaciones Dinámicas
* **Problema:** La gerencia requiere tanto analizar la demanda y efectividad clínica (citas y doctores) como el consumo y gasto en materiales odontológicos, permitiendo filtros variables sin crear endpoints rígidos.
* **Solución Implementada:** `ReporteGerenciaService` procesa dinámicamente según el discriminador de tipo (`CONSUMO_MATERIALES` vs `NUMERO_ATENCIONES`), resolviendo filtros de fechas, doctores y categorías en una estructura unificada `ReporteResponseDTO`.

### ADR-S5-10: Visualización Híbrida Estadística (Gráfico Temporal de Barras + Distribución Proporcional + Tabla Detallada + Impresión Oficial CSS `@media print`)
* **Problema:** Presentar datos extensos en tablas planas dificulta la toma de decisiones gerenciales; por otra parte, depender de librerías de gráficos pesadas puede ralentizar el frontend.
* **Solución Implementada:** Implementación de gráficos nativos responsivos en SVG y Tailwind CSS con tooltips flotantes en tiempo real, barras proporcionales codificadas por color, tabla detallada con buscador local, y estilos `@media print` para generar reportes físicos limpios con un clic.

### ADR-S5-11: Motor Autónomo de Evaluación de Umbrales de Calidad y Disparo de Notificaciones de Rendimiento
* **Problema:** Los gerentes no pueden estar revisando continuamente las métricas para detectar si las inasistencias suben o si las atenciones bajan; se requiere detección proactiva.
* **Solución Implementada:** `RendimientoService` evalúa las reglas activas de forma automática mediante tareas programadas (`@Scheduled`) y bajo demanda. Al cruzar el umbral, despacha eventos estructurados con `TipoNotificacion.RENDIMIENTO` al Centro de Notificaciones.

---

## 8. BITÁCORA DE CONTROL DE CALIDAD Y PRUEBAS DE REGRESIÓN

| Escenario de Verificación | Subsistema | Resultado Técnico | Estado |
| :--- | :--- | :--- | :---: |
| **Compilación Maven Backend (Fases 1, 2 y 3)** | Spring Boot 3 / Java 17 | `BUILD SUCCESS - 146 source files compiled cleanly` | **APROBADO** |
| **Build Vite Frontend (Fases 1, 2 y 3)** | React 18 / Tailwind | `101 modules transformed -> dist/index.html built in 2.38s` | **APROBADO** |
| **Catálogo de Reportes Institucionales** | Backend / Reportes | Endpoint `/api/gerencia/reportes/tipos` retorna catálogo estructurado | **APROBADO** |
| **Reporte de Consumo de Materiales** | Backend / Reportes | Cálculo de unidades, insumo top, categorías y serie temporal | **APROBADO** |
| **Reporte de Número de Atenciones** | Backend / Reportes | Cálculo de atenciones efectivas, tasa de asistencia y doctor líder | **APROBADO** |
| **Exportación Binaria Excel de Reportes** | Backend / Apache POI | Descarga de `Reporte_{TIPO}_{FECHA}.xlsx` (4,546 bytes) | **APROBADO** |
| **Motor de Alertas de Rendimiento** | Backend / Rendimiento | Evaluación de umbrales (> 15% cancelaciones) dispara notificación a Gerente | **APROBADO** |
| **Impresión Limpia de Reportes** | Frontend / index.css | Reglas `@media print` ocultan sidebar, navbar y botones | **APROBADO** |
| **Segregación 403 Solo Lectura Stock** | Backend / GerenciaStock | Rechazo con `403 Forbidden` ante intentos de mutación | **APROBADO** |
| **Transición Nocturna a NO_ASISTIO** | Backend / Scheduler | Citas pasadas en `PROGRAMADA` cambian automáticamente a `NO_ASISTIO` | **APROBADO** |
| **Regresión: Flujo Clínico Sprint 3** | Consultas Odontológicas | Stepper clínico, odontograma y prescripciones operan con total normalidad | **APROBADO** |
| **Regresión: Respaldos Sprint 4** | Seguridad TI / Inventario | Módulos de dump SQL, reset de contraseña e inventario intactos | **APROBADO** |
