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
| **Fase 2** | **Monitoreo Operativo & Auditoría de Stock Crítico** | **Asistente Dental & Gerente** | `DENTALCARE-49`, `DENTALCARE-73` | **5.0 SP** | **PLANIFICADO / LISTO PARA FASE 2** |
| **Fase 3** | **Inteligencia de Negocios, Reportes y Rendimiento** | **Gerente** | `DENTALCARE-70`, `DENTALCARE-72`, `DENTALCARE-74`, `DENTALCARE-95` | **9.0 SP** | **PLANIFICADO / LISTO PARA FASE 3** |
| **TOTAL** | **Consolidado General Sprint 5** | **Recepcionista, Asistente, Gerente** | **9 Historias de Usuario (PBIs)** | **21.0 SP** | **FASE 1 COMPLETADA (7 SP)** |

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
  - [x] **Convenciones y Casos de Uso:** Cumplimiento total de estándares de nomenclatura y revisión de caso de uso.

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
    * **Respaldo Periódico:** Revisión cada 30 minutos (`fixedDelay = 1800000`).
  - [x] **Resumen Diario Visual:** En `AppointmentPage.jsx`, el bloque de resumen contabiliza y resalta en rojo/rosa la métrica de `No asistió`.
  - [x] **Carga Automática de Citas de Mañana:** Endpoint `GET /api/agenda/alertas-citas/recordatorios?fecha=YYYY-MM-DD` que lee las citas activas programadas para el día seleccionado.
  - [x] **Botón de Envío Masivo "Enviar a Todos":** Botón de acción destacado en `RecordatoriosCitasPanel.jsx` con selección entre Correo Masivo (simulado en 1 clic) o Asistente Secuencial de WhatsApp.
  - [x] **Asistente Secuencial de WhatsApp:** Modal interactivo que evita el bloqueo de ventanas emergentes (pop-up blockers) del navegador web, permitiendo abrir chats uno a uno con verificación.
  - [x] **Trazabilidad y Notificaciones:** Registro de eventos de confirmación y cierre diario de citas no asistidas en el Centro de Notificaciones.
  - [x] **Detección Automática de Cupos Liberados:** Al cancelar una cita en `CitaService.cancelarCita(...)`, se ejecuta `ListaEsperaService.evaluarCupoLiberadoPorCancelacion(cita)` alertando inmediatamente a recepción.

---

## 2.2 Inventario de Archivos Creados y Modificados en Fase 1

### A. Backend (Spring Boot 3 + Java 17 + MySQL 8.0)
* **Nuevos Modelos y Enums JPA:**
  - `src/main/java/com/dentalcare/api/models/enums/TipoNotificacion.java` (Valores: `CITA_PROXIMA`, `LISTA_ESPERA`, `STOCK_CRITICO`, `RENDIMIENTO`, `SISTEMA`).
  - `src/main/java/com/dentalcare/api/models/enums/EstadoListaEspera.java` (Valores: `EN_ESPERA`, `NOTIFICADO`, `AGENDADO`, `DESCARTADO`).
  - `src/main/java/com/dentalcare/api/models/enums/PrioridadListaEspera.java` (Valores: `ALTA`, `MEDIA`, `BAJA`).
  - `src/main/java/com/dentalcare/api/models/enums/EstadoCita.java` (Incluye `NO_ASISTIO`, `PROGRAMADA`, `PENDIENTE`, `COMPLETADA`, `CANCELADA`, etc.).
  - `src/main/java/com/dentalcare/api/models/Notificacion.java` (Mapeo tabla `notificaciones`).
  - `src/main/java/com/dentalcare/api/models/ConfiguracionAlertaCita.java` (Mapeo tabla `configuracion_alerta_cita`).
  - `src/main/java/com/dentalcare/api/models/ListaEspera.java` (Mapeo tabla `lista_espera`).
* **Nuevos Data Transfer Objects (DTOs):**
  - `src/main/java/com/dentalcare/api/dtos/Notificacion/NotificacionResponseDto.java`
  - `src/main/java/com/dentalcare/api/dtos/Notificacion/ResumenNotificacionesDto.java`
  - `src/main/java/com/dentalcare/api/dtos/Cita/ConfiguracionAlertaCitaDto.java`
  - `src/main/java/com/dentalcare/api/dtos/Cita/CitaProximaDto.java`
  - `src/main/java/com/dentalcare/api/dtos/Cita/CitaRecordatorioDto.java`
  - `src/main/java/com/dentalcare/api/dtos/Cita/ListaEsperaRequestDto.java`
  - `src/main/java/com/dentalcare/api/dtos/Cita/ListaEsperaResponseDto.java`
* **Nuevos Componentes y Tareas Programadas:**
  - `src/main/java/com/dentalcare/api/schedulers/CitaScheduler.java` (Cron 00:01 AM, revisión periódica y arranque en frío).
  - `src/main/java/com/dentalcare/api/ApiApplication.java` (Habilitación de `@EnableScheduling`).
* **Nuevos Repositorios Spring Data JPA:**
  - `src/main/java/com/dentalcare/api/repositories/NotificacionRepository.java`
  - `src/main/java/com/dentalcare/api/repositories/ConfiguracionAlertaCitaRepository.java`
  - `src/main/java/com/dentalcare/api/repositories/ListaEsperaRepository.java`
  - `src/main/java/com/dentalcare/api/repositories/CitaRepository.java` (Incluye `actualizarCitasVencidasANoAsistio` y `findCitasActivasPorFecha`).
* **Nuevos Servicios de Negocio:**
  - `src/main/java/com/dentalcare/api/services/NotificacionService.java`
  - `src/main/java/com/dentalcare/api/services/AlertaCitaService.java`
  - `src/main/java/com/dentalcare/api/services/ListaEsperaService.java`
  - Modificación en `src/main/java/com/dentalcare/api/services/CitaService.java` (Método `actualizarCitasVencidasANoAsistio`, hook en `obtenerTodas` y `obtenerPorId`).
* **Nuevos Controladores REST:**
  - `src/main/java/com/dentalcare/api/controllers/NotificacionController.java` (`/api/notificaciones`)
  - `src/main/java/com/dentalcare/api/controllers/AlertaCitaController.java` (`/api/agenda/alertas-citas`)
  - `src/main/java/com/dentalcare/api/controllers/CitaController.java` (Endpoint `/sincronizar-vencidas`)
  - `src/main/java/com/dentalcare/api/controllers/ListaEsperaController.java` (`/api/agenda/lista-espera`)
* **Script de Base de Datos:**
  - `database/MIGRACION_SPRINT5_AIVEN.sql` (DDL para tablas nuevas y columnas ampliadas).

### B. Frontend (React 18 + Vite + TailwindCSS)
* **Nuevos Servicios API:**
  - `src/services/notificacion.service.js`
  - `src/services/alertaCita.service.js`
  - `src/services/listaEspera.service.js`
* **Nuevos Componentes y Vistas:**
  - `src/components/NotificationBell.jsx` (Campana reactiva en Navbar con badge, dropdown y pestañas).
  - `src/components/RecordatoriosCitasPanel.jsx` (Panel de recordatorios para mañana/hoy con WhatsApp, correo simulado y botón masivo).
  - `src/components/CitaProximaAlertBanner.jsx` (Banner superior en Agenda que alerta sobre citas de mañana por confirmar).
  - `src/components/ModalConfigAlertaCita.jsx` (Modal de configuración de canales, anticipación de 1 día y plantilla).
  - `src/views/NotificacionesPage.jsx` (Bandeja unificada `/notificaciones`).
* **Componentes Modificados:**
  - `src/components/Layout.jsx` (Montaje de `<NotificationBell />`).
  - `src/views/AppointmentPage.jsx` (Pestaña `Recordatorios (Mañana)`, badge y resumen diario de `No asistió`).
  - `src/App.jsx` (Ruta `/notificaciones`).

---

## 3. MATRIZ MAESTRA DE ENDPOINTS — SPRINT 5 (FASE 1 COMPLETADA)

| Método | Endpoint URI | Roles Autorizados | Estado Técnico | Descripción |
| :---: | :--- | :--- | :---: | :--- |
| `GET` | `/api/notificaciones` | Todos los autenticados | **COMPLETADO (100%)** | Lista notificaciones del usuario/rol con filtro opcional de no leídas |
| `GET` | `/api/notificaciones/resumen-no-leidas` | Todos los autenticados | **COMPLETADO (100%)** | Conteo optimizado para badge en el Navbar |
| `PATCH`| `/api/notificaciones/{id}/marcar-leida` | Todos los autenticados | **COMPLETADO (100%)** | Marca una notificación específica como leída |
| `POST` | `/api/notificaciones/marcar-todas-leidas` | Todos los autenticados | **COMPLETADO (100%)** | Marca todas las notificaciones pendientes del usuario como leídas |
| `POST` | `/api/notificaciones/crear` | Todos los autenticados | **COMPLETADO (100%)** | Creación de eventos de notificación |
| `GET` | `/api/agenda/alertas-citas/configuracion` | RECEPCIONISTA, ADMIN | **COMPLETADO (100%)** | Consulta preferencias de anticipación (1 día antes), canales y plantilla |
| `PUT` | `/api/agenda/alertas-citas/configuracion` | RECEPCIONISTA, ADMIN | **COMPLETADO (100%)** | Actualiza preferencias de alerta del usuario |
| `GET` | `/api/agenda/alertas-citas/proximas` | RECEPCIONISTA, ADMIN | **COMPLETADO (100%)** | Citas programadas dentro de la ventana de anticipación |
| `GET` | `/api/agenda/alertas-citas/recordatorios` | RECEPCIONISTA, ADMIN | **COMPLETADO (100%)** | Carga citas del día siguiente (o fecha dada) formateadas para WhatsApp y Correo |
| `POST` | `/api/agenda/alertas-citas/{idCita}/enviar-recordatorio` | RECEPCIONISTA, ADMIN | **COMPLETADO (100%)** | Registra el envío del recordatorio individual (WhatsApp/Correo) |
| `POST` | `/api/agenda/alertas-citas/enviar-recordatorio-masivo` | RECEPCIONISTA, ADMIN | **COMPLETADO (100%)** | Envío masivo de recordatorios para una fecha o lote con notificación consolidada |
| `POST` | `/api/citas/sincronizar-vencidas` | RECEPCIONISTA, ADMIN | **COMPLETADO (100%)** | Sincronización forzada/manual de citas concluidas a `NO_ASISTIO` |
| `GET` | `/api/agenda/lista-espera` | RECEPCIONISTA, ADMIN, GERENTE | **COMPLETADO (100%)** | Consulta pacientes en lista de espera |
| `POST` | `/api/agenda/lista-espera` | RECEPCIONISTA, ADMIN | **COMPLETADO (100%)** | Registra paciente en lista de espera |
| `PATCH`| `/api/agenda/lista-espera/{id}/descartar`| RECEPCIONISTA, ADMIN | **COMPLETADO (100%)** | Descarta un paciente de la lista de espera |
| `DELETE`| `/api/agenda/lista-espera/{id}` | RECEPCIONISTA, ADMIN | **COMPLETADO (100%)** | Elimina permanentemente registro de la lista |

---

## 4. REGISTRO DE DECISIONES DE ARQUITECTURA DE SOFTWARE (ADRS) — FASE 1

### ADR-S5-01: Arquitectura de Notificaciones Desacoplada y Polling Suave
* **Problema:** Los eventos de citas próximas, cancelaciones de agenda y avisos clínicos deben ser consumidos en tiempo real por el frontend sin sobrecargar la base de datos Aiven.
* **Solución Implementada:** Entidad `notificaciones` con endpoint ligero `GET /api/notificaciones/resumen-no-leidas` que realiza una consulta `COUNT` indexada con polling suave de 30 segundos en `NotificationBell.jsx`.

### ADR-S5-02: Detección Automática de Pacientes en Lista de Espera ante Cancelaciones
* **Problema:** Cuando una cita es cancelada, la recepcionista no siempre recuerda consultar la lista de espera, lo que provoca que los cupos queden sin aprovechar.
* **Solución Implementada:** Hook en `CitaService` que intercepta cancelaciones y ejecuta `ListaEsperaService.evaluarCupoLiberadoPorCancelacion`, alertando a `RECEPCIONISTA` con los datos del paciente compatible.

### ADR-S5-03: Aviso Sonoro Integrado sin Dependencias Externas (Web Audio API)
* **Problema:** Los archivos de audio estáticos pueden fallar por problemas de ruta relativa o políticas de CORS en navegadores modernos.
* **Solución Implementada:** En `CitaProximaAlertBanner.jsx` se implementó un oscilador sintetizado nativo mediante `AudioContext` de HTML5.

### ADR-S5-04: Gestión de Recordatorios Proactivos con Regla del Día Anterior (24 Horas) y Envío Masivo
* **Problema:** Una alerta a 30 minutos de la cita es tardía para evitar inasistencias. La recepcionista requiere contactar a todos los pacientes del día siguiente con opciones masivas sin bloqueos de navegador.
* **Solución Implementada:** `RecordatoriosCitasPanel.jsx` con botón `[Enviar a Todos]` (Correo Masivo instantáneo y Asistente Secuencial de WhatsApp).

### ADR-S5-05: Cierre Automático Nocturno de Citas Vencidas a NO_ASISTIO con Triple Mecanismo (Cron + Hook JIT + Cold Start)
* **Problema:** Las citas pasadas que los pacientes no atendieron ni reprogramaron permanecían indefinidamente en estado `PROGRAMADA` en la base de datos, distorsionando las estadísticas de asistencia y la agenda clínica.
* **Solución Implementada:** Se diseñó una estrategia de triple garantía:
  1. **Scheduler Cron (00:01 AM):** Tarea programada en `CitaScheduler.java` con `@Scheduled(cron = "0 1 0 * * ?", zone = "America/El_Salvador")` que corre automáticamente tras finalizar el día (después de las 11:59 PM).
  2. **Hook Just-In-Time:** Al consultar la agenda (`CitaService.obtenerTodas`), se ejecuta una actualización atómica en base de datos para que los usuarios vean el estado real al instante sin desfases.
  3. **Arranque en Frío (Cold Start):** Evento `@EventListener(ApplicationReadyEvent.class)` para regularizar citas pendientes si el servidor estuvo apagado durante la medianoche.
  4. La regla aplica exclusivamente a citas con `fechaCita < hoy` en estado `PROGRAMADA` o `PENDIENTE`, preservando intactas las citas `COMPLETADA`, `FINALIZADA`, `CANCELADA` y `REPROGRAMADA`.

---

## 5. BITÁCORA DE CONTROL DE CALIDAD Y PRUEBAS DE REGRESIÓN

| Escenario de Verificación | Subsistema | Resultado Técnico | Estado |
| :--- | :--- | :--- | :---: |
| **Compilación Maven Backend** | Spring Boot 3 / Java 17 | `BUILD SUCCESS - 125 source files compiled cleanly` | **APROBADO** |
| **Build Vite Frontend** | React 18 / Tailwind | `96 modules transformed -> dist/index.html built in 2.54s` | **APROBADO** |
| **Transición Nocturna a NO_ASISTIO** | Backend / Scheduler | Citas pasadas en `PROGRAMADA` cambian automáticamente a `NO_ASISTIO` | **APROBADO** |
| **Cron Diario 00:01 AM** | Backend / CitaScheduler | Disparo a medianoche con zona horaria `America/El_Salvador` | **APROBADO** |
| **Hook Just-In-Time Citas** | Backend / CitaService | Actualización transparente al listar citas sin recargar | **APROBADO** |
| **Resumen Diario Agenda** | Frontend / AppointmentPage | Píldora y contador numérico específico de `No asistió` en rojo | **APROBADO** |
| **Reprogramación de Citas NO_ASISTIO** | Agenda / Modal | Si el paciente llama después, se reprograma con nueva fecha futura | **APROBADO** |
| **Campana y Badge en Layout** | Notificaciones UI | Badge numérico reactivo, dropdown con pestañas y marcado de lectura | **APROBADO** |
| **Botón Enviar a Todos** | Agenda / Recordatorios | Diálogo de selección multicanal (Correo Masivo / Asistente WhatsApp) | **APROBADO** |
| **Regresión: Flujo Clínico Sprint 3** | Consultas Odontológicas | Stepper clínico, odontograma y prescripciones operan con total normalidad | **APROBADO** |
| **Regresión: Respaldos Sprint 4** | Seguridad TI / Inventario | Módulos de dump SQL, reset de contraseña e inventario intactos | **APROBADO** |
