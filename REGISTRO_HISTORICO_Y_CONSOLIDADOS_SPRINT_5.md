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

#### 3. `DENTALCARE-92` — PBI-Secret-14: Gestionar Lista de Espera y Recordatorios de Citas (3.0 SP)
* **Objetivo:** Cargar la lista de citas programadas para el día de mañana (o fecha seleccionada), permitiendo simular y despachar recordatorios directamente vía WhatsApp y Correo Electrónico (tanto individual como masivamente), además de la reasignación automática de cupos liberados.
* **Criterios de Aceptación Cumplidos:**
  - [x] **Carga Automática de Citas de Mañana:** Endpoint `GET /api/agenda/alertas-citas/recordatorios?fecha=YYYY-MM-DD` que lee las citas activas programadas para el día seleccionado (con filtros rápidos: *Citas de Mañana*, *Citas de Hoy*, o selector de fecha).
  - [x] **Botón de Envío Masivo "Enviar a Todos":** Botón de acción destacado en `RecordatoriosCitasPanel.jsx` que permite procesar de un solo golpe a todos los pacientes de la fecha.
  - [x] **Envío Masivo por Correo (Simulado en 1 Clic):** Endpoint `POST /api/agenda/alertas-citas/enviar-recordatorio-masivo?canal=CORREO` que despacha simultáneamente todos los correos institucionales, actualiza los indicadores visuales y genera una notificación consolidadas en el sistema.
  - [x] **Asistente Secuencial de WhatsApp:** Modal interactivo para envío masivo por WhatsApp que presenta a los pacientes en orden con un botón `[Abrir Chat]` individual, evitando bloqueos de pop-ups por parte del navegador web.
  - [x] **Integración con WhatsApp Web Individual:** Enlace `https://api.whatsapp.com/send?phone=...&text=...` que abre WhatsApp Web con el mensaje pre-cargado listo para enviar.
  - [x] **Simulación de Correo Electrónico Individual:** Modal interactivo con remitente, destinatario, asunto y cuerpo para previsualizar y simular el envío.
  - [x] **Trazabilidad de Envíos:** Endpoints para registrar envíos individuales y por lote, garantizando bitácora operativa para la recepción.
  - [x] **Detección Automática de Cupos Liberados:** Al cancelar una cita en `CitaService.cancelarCita(...)` o cambiar su estado a `CANCELADA`, se ejecuta `ListaEsperaService.evaluarCupoLiberadoPorCancelacion(cita)` alertando inmediatamente a recepción sobre el paciente en espera compatible.
  - [x] **Pestaña Integrada en Agenda:** Pestaña `Recordatorios (Mañana)` en `AppointmentPage.jsx` (`RecordatoriosCitasPanel.jsx`), eliminando duplicidad de formularios y enfocándose en la operación diaria de recepción.

---

## 2.2 Inventario de Archivos Creados y Modificados en Fase 1

### A. Backend (Spring Boot 3 + Java 17 + MySQL 8.0)
* **Nuevos Modelos y Enums JPA:**
  - `src/main/java/com/dentalcare/api/models/enums/TipoNotificacion.java` (Valores: `CITA_PROXIMA`, `LISTA_ESPERA`, `STOCK_CRITICO`, `RENDIMIENTO`, `SISTEMA`).
  - `src/main/java/com/dentalcare/api/models/enums/EstadoListaEspera.java` (Valores: `EN_ESPERA`, `NOTIFICADO`, `AGENDADO`, `DESCARTADO`).
  - `src/main/java/com/dentalcare/api/models/enums/PrioridadListaEspera.java` (Valores: `ALTA`, `MEDIA`, `BAJA`).
  - `src/main/java/com/dentalcare/api/models/Notificacion.java` (Mapeo tabla `notificaciones`).
  - `src/main/java/com/dentalcare/api/models/ConfiguracionAlertaCita.java` (Mapeo tabla `configuracion_alerta_cita` con `anticipacionTipo`, `canalWhatsApp`, `canalCorreo`, `plantillaMensaje`).
  - `src/main/java/com/dentalcare/api/models/ListaEspera.java` (Mapeo tabla `lista_espera`).
* **Nuevos Data Transfer Objects (DTOs):**
  - `src/main/java/com/dentalcare/api/dtos/Notificacion/NotificacionResponseDto.java`
  - `src/main/java/com/dentalcare/api/dtos/Notificacion/ResumenNotificacionesDto.java`
  - `src/main/java/com/dentalcare/api/dtos/Cita/ConfiguracionAlertaCitaDto.java`
  - `src/main/java/com/dentalcare/api/dtos/Cita/CitaProximaDto.java`
  - `src/main/java/com/dentalcare/api/dtos/Cita/CitaRecordatorioDto.java` (DTO para recordatorios con enlace WhatsApp y cuerpo de correo).
  - `src/main/java/com/dentalcare/api/dtos/Cita/ListaEsperaRequestDto.java`
  - `src/main/java/com/dentalcare/api/dtos/Cita/ListaEsperaResponseDto.java`
* **Nuevos Repositorios Spring Data JPA:**
  - `src/main/java/com/dentalcare/api/repositories/NotificacionRepository.java` (Consultas por usuario/rol, conteo no leídas, actualización masiva).
  - `src/main/java/com/dentalcare/api/repositories/ConfiguracionAlertaCitaRepository.java`
  - `src/main/java/com/dentalcare/api/repositories/ListaEsperaRepository.java` (Cola FIFO ponderada).
  - `src/main/java/com/dentalcare/api/repositories/CitaRepository.java` (Consultas `findCitasActivasPorFecha` y `findCitasProximas`).
* **Nuevos Servicios de Negocio:**
  - `src/main/java/com/dentalcare/api/services/NotificacionService.java`
  - `src/main/java/com/dentalcare/api/services/AlertaCitaService.java` (Incluye `obtenerCitasParaRecordatorio`, `registrarEnvioRecordatorio` y `registrarEnvioRecordatorioMasivo`).
  - `src/main/java/com/dentalcare/api/services/ListaEsperaService.java`
  - Modificación en `src/main/java/com/dentalcare/api/services/CitaService.java` (Hook reactivo de evaluación de cupos ante cancelación).
* **Nuevos Controladores REST:**
  - `src/main/java/com/dentalcare/api/controllers/NotificacionController.java` (`/api/notificaciones`)
  - `src/main/java/com/dentalcare/api/controllers/AlertaCitaController.java` (`/api/agenda/alertas-citas`, con `/enviar-recordatorio-masivo`)
  - `src/main/java/com/dentalcare/api/controllers/ListaEsperaController.java` (`/api/agenda/lista-espera`)
* **Script de Base de Datos:**
  - `database/MIGRACION_SPRINT5_AIVEN.sql` (DDL para tablas nuevas y columnas ampliadas).

### B. Frontend (React 18 + Vite + TailwindCSS)
* **Nuevos Servicios API:**
  - `src/services/notificacion.service.js`
  - `src/services/alertaCita.service.js` (Incluye `getCitasParaRecordatorio`, `enviarRecordatorioCita` y `enviarRecordatorioMasivo`).
  - `src/services/listaEspera.service.js`
* **Nuevos Componentes y Vistas:**
  - `src/components/NotificationBell.jsx` (Campana reactiva en Navbar con badge, dropdown y pestañas Todas / No Leídas).
  - `src/components/RecordatoriosCitasPanel.jsx` (Panel de recordatorios para mañana/hoy con WhatsApp directo, correo simulado, botón masivo y asistente secuencial de WhatsApp).
  - `src/components/CitaProximaAlertBanner.jsx` (Banner superior en Agenda que alerta sobre citas de mañana por confirmar).
  - `src/components/ModalConfigAlertaCita.jsx` (Modal de configuración de canales, anticipación de 1 día y plantilla).
  - `src/views/NotificacionesPage.jsx` (Bandeja unificada `/notificaciones` con filtros por tipo y búsqueda).
* **Componentes Modificados:**
  - `src/components/Layout.jsx` (Montaje de `<NotificationBell />` en barra superior).
  - `src/views/AppointmentPage.jsx` (Pestaña `Recordatorios (Mañana)`, banner superior de aviso y modal de alertas).
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
| `GET` | `/api/agenda/lista-espera` | RECEPCIONISTA, ADMIN, GERENTE | **COMPLETADO (100%)** | Consulta pacientes en lista de espera |
| `POST` | `/api/agenda/lista-espera` | RECEPCIONISTA, ADMIN | **COMPLETADO (100%)** | Registra paciente en lista de espera |
| `PATCH`| `/api/agenda/lista-espera/{id}/descartar`| RECEPCIONISTA, ADMIN | **COMPLETADO (100%)** | Descarta un paciente de la lista de espera |
| `DELETE`| `/api/agenda/lista-espera/{id}` | RECEPCIONISTA, ADMIN | **COMPLETADO (100%)** | Elimina permanentemente registro de la lista |

---

## 4. REGISTRO DE DECISIONES DE ARQUITECTURA DE SOFTWARE (ADRS) — FASE 1

### ADR-S5-01: Arquitectura de Notificaciones Desacoplada y Polling Suave
* **Problema:** Los eventos de citas próximas, cancelaciones de agenda y avisos clínicos deben ser consumidos en tiempo real por el frontend sin imponer sobrecarga de conexión WebSocket persistente al cluster de base de datos Aiven.
* **Solución Implementada:** Se diseñó la entidad `notificaciones` con un endpoint ligero `GET /api/notificaciones/resumen-no-leidas` que realiza una consulta `COUNT` indexada. En el frontend, `NotificationBell.jsx` ejecuta un polling suave cada 30 segundos, manteniendo el contador de alertas actualizado con impacto nulo en el rendimiento del servidor.

### ADR-S5-02: Detección Automática de Pacientes en Lista de Espera ante Cancelaciones
* **Problema:** Cuando una cita es cancelada, la recepcionista no siempre recuerda consultar la lista de espera, lo que provoca que los cupos queden sin aprovechar.
* **Solución Implementada:** Se integró un hook en `CitaService` que intercepta tanto `cancelarCita(...)` como `actualizarEstado(..., CANCELADA)`. Al dispararse, `ListaEsperaService.evaluarCupoLiberadoPorCancelacion` busca en `lista_espera` registros con `estado = 'EN_ESPERA'` priorizando `ALTA` > `MEDIA` > `BAJA` y orden de solicitud (FIFO). Al encontrar un candidato, actualiza su estado a `NOTIFICADO` y genera automáticamente una notificación dirigida al rol `RECEPCIONISTA`.

### ADR-S5-03: Aviso Sonoro Integrado sin Dependencias Externas (Web Audio API)
* **Problema:** Los archivos de audio estáticos (`.mp3` / `.wav`) pueden fallar por problemas de ruta relativa o políticas de CORS en navegadores modernos.
* **Solución Implementada:** En `CitaProximaAlertBanner.jsx` se implementó un oscilador sintetizado nativo mediante `AudioContext` de HTML5, emitiendo un acorde armónico discreto (D5 a A5) que no requiere archivos externos ni dependencias pesadas.

### ADR-S5-04: Gestión de Recordatorios Proactivos con Regla del Día Anterior (24 Horas), Envío Masivo y Asistente Secuencial
* **Problema:** Una alerta a 30 minutos de la cita es tardía para evitar inasistencias. La recepcionista requiere revisar las citas del día siguiente y contactar a todos los pacientes de manera eficiente sin que los navegadores bloqueen pestañas por pop-up blockers.
* **Solución Implementada:** Se implementó `RecordatoriosCitasPanel.jsx` con botón `[Enviar a Todos]`. Ofrece despacho masivo por correo electrónico en un solo clic (`POST /api/agenda/alertas-citas/enviar-recordatorio-masivo`) y un Asistente Secuencial de WhatsApp que lista ordenadamente a los pacientes con sus chats pre-armados, garantizando fluidez y cero bloqueos de navegador.

---

## 5. BITÁCORA DE CONTROL DE CALIDAD Y PRUEBAS DE REGRESIÓN

| Escenario de Verificación | Subsistema | Resultado Técnico | Estado |
| :--- | :--- | :--- | :---: |
| **Compilación Maven Backend** | Spring Boot 3 / Java 17 | `BUILD SUCCESS - 124 source files compiled cleanly` | **APROBADO** |
| **Build Vite Frontend** | React 18 / Tailwind | `96 modules transformed -> dist/index.html built in 2.69s` | **APROBADO** |
| **Campana y Badge en Layout** | Notificaciones UI | Badge numérico reactivo, dropdown con pestañas y marcado de lectura | **APROBADO** |
| **Botón Enviar a Todos** | Agenda / Recordatorios | Diálogo de selección multicanal (Correo Masivo / Asistente WhatsApp) | **APROBADO** |
| **Envío Masivo por Correo** | Agenda / Backend | Despacho por lote y notificación consolidada a recepción | **APROBADO** |
| **Asistente Secuencial WhatsApp** | Agenda / UI | Modal interactivo para abrir chats sin bloqueo de ventanas emergentes | **APROBADO** |
| **Configuración Alerta Citas** | Agenda / Modal | Guardado de regla del día anterior (1440 min), canales y plantilla | **APROBADO** |
| **Banner Citas de Mañana** | Agenda / Alertas | Detección reactiva de citas programadas del día siguiente con acceso directo | **APROBADO** |
| **Intercepción Cancelar Cita** | Integración Cita-Espera | Disparo de notificación automática a recepcionista ante cancelación de cupo | **APROBADO** |
| **Regresión: Flujo Clínico Sprint 3** | Consultas Odontológicas | Stepper clínico, odontograma y prescripciones operan con total normalidad | **APROBADO** |
| **Regresión: Respaldos Sprint 4** | Seguridad TI / Inventario | Módulos de dump SQL, reset de contraseña e inventario intactos | **APROBADO** |
