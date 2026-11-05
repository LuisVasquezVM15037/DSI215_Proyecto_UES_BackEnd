# REGISTRO HISTÓRICO Y CONSOLIDADO DE CAMBIOS — SPRINT 5

> **Proyecto:** Sistema Informático para la Gestión de Servicios Médicos en la Clínica Odontológica DentalCare ERP  
> **Cátedra:** Diseño de Sistemas II (DSI215) — Ciclo II-2026  
> **Institución:** Universidad de El Salvador (UES)  
> **Documento Oficial de Evaluación:** Bitácora consolidada de modificaciones, componentes, servicios, endpoints y estado de implementación del Sprint 5 para la evaluación de cátedra.

---

## 1. TABLERO GENERAL DE AVANCE DEL SPRINT 5

| Fase | Subsistema / Módulo | Rol Responsable | PBIs Cubiertos | Story Points | Estado Técnico |
| :---: | :--- | :--- | :--- | :---: | :---: |
| **Fase 1** | **Agenda Avanzada, Lista de Espera y Notificaciones** | **Recepcionista / Secretaria** | `DENTALCARE-71`, `DENTALCARE-96`, `DENTALCARE-92` | **7.0 SP** | **COMPLETADA (100%)** |
| **Fase 2** | **Monitoreo Operativo & Auditoría de Stock Crítico** | **Asistente Dental & Gerente** | `DENTALCARE-49`, `DENTALCARE-73` | **5.0 SP** | **PLANIFICADO / LISTO PARA FASE 2** |
| **Fase 3** | **Inteligencia de Negocios, Reportes y Rendimiento** | **Gerente** | `DENTALCARE-70`, `DENTALCARE-72`, `DENTALCARE-74`, `DENTALCARE-95` | **9.0 SP** | **PLANIFICADO / LISTO PARA FASE 3** |
| **TOTAL** | **Consolidado General Sprint 5** | **Recepcionista, Asistente, Gerente** | **9 Historias de Usuario (PBIs)** | **21.0 SP** | **FASE 1 COMPLETADA (7 SP)** |

---

## 2. CONSOLIDADO DETALLADO — FASE 1: AGENDA AVANZADA, LISTA DE ESPERA Y NOTIFICACIONES

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
* **Objetivo:** Alertar a la recepción sobre las citas que están a 30 minutos de su hora de inicio para preparar la llegada del paciente y el consultorio odontológico.
* **Criterios de Aceptación Cumplidos:**
  - [x] **Emisión Automática de Recordatorio:** Servicio `AlertaCitaService.obtenerCitasProximas` que filtra citas programadas del día actual dentro de la ventana de anticipación (`hora_inicio BETWEEN NOW() AND NOW() + X MIN`).
  - [x] **Alerta a 30 Minutos:** Ventana predeterminada fijada en 30 minutos configurables (15, 30, 45, 60 min).
  - [x] **Banner Visual Flotante:** Componente `CitaProximaAlertBanner.jsx` que despliega el recordatorio en la agenda con minutos restantes, paciente, doctor y teléfono.
  - [x] **Aviso Sonoro:** Tono discreto sintetizado mediante Web Audio API al detectar citas inminentes.
  - [x] **Panel de Configuración Interactivo:** Componente `ModalConfigAlertaCita.jsx` accesible desde el encabezado de la agenda para encender/apagar alertas, cambiar tiempo y sonido.
  - [x] **Persistencia por Usuario:** Entidad `configuracion_alerta_cita` con endpoints `GET` y `PUT` en `/api/agenda/alertas-citas/configuracion`.

#### 3. `DENTALCARE-92` — PBI-Secret-14: Gestionar Lista de Espera (3.0 SP)
* **Objetivo:** Administrar pacientes que solicitan cita pero no hallaron cupo inmediato, permitiendo su contacto y reasignación automática ante cancelaciones en la agenda.
* **Criterios de Aceptación Cumplidos:**
  - [x] **Registro con Horario Preferido:** Formulario modal `ModalListaEspera.jsx` que permite asociar paciente, doctor preferido, rango de horario (Mañana, Tarde, Mediodía), días y prioridad (`ALTA`, `MEDIA`, `BAJA`).
  - [x] **Detección y Notificación Automática de Cupos Liberados:** Al cancelar una cita en `CitaService.cancelarCita(...)` o cambiar su estado a `CANCELADA`, se ejecuta `ListaEsperaService.evaluarCupoLiberadoPorCancelacion(cita)`. El sistema identifica al primer paciente en cola compatible (`findCandidatosParaCupo`) y despacha una notificación prioritaria a `RECEPCIONISTA`:
    ```
    "¡Espacio Liberado en Agenda! Se canceló una cita para el [Fecha] a las [Hora] con [Doctor]. Primer paciente elegible en Lista de Espera: [Paciente] (Tel: [Teléfono])."
    ```
  - [x] **Remoción / Descarte Manual:** Opciones para descartar (`PATCH /api/agenda/lista-espera/{id}/descartar`) o eliminar (`DELETE /api/agenda/lista-espera/{id}`) registros de la cola.
  - [x] **Asignación Directa de Cupo:** Botón `[Agendar]` en la tabla de lista de espera que pre-carga el paciente en el formulario de citas y cambia su estado a `AGENDADO`.
  - [x] **Pestaña Integrada:** Nueva pestaña `Lista de Espera` dentro de `AppointmentPage.jsx` con panel interactivo (`ListaEsperaPanel.jsx`) y filtrado por estado (`EN_ESPERA`, `NOTIFICADO`, `AGENDADO`).

---

### 2.2 Inventario de Archivos Creados y Modificados en Fase 1

#### A. Backend (Spring Boot 3 + Java 17 + MySQL 8.0)
* **Nuevos Modelos y Enums JPA:**
  - `src/main/java/com/dentalcare/api/models/enums/TipoNotificacion.java` (Valores: `CITA_PROXIMA`, `LISTA_ESPERA`, `STOCK_CRITICO`, `RENDIMIENTO`, `SISTEMA`).
  - `src/main/java/com/dentalcare/api/models/enums/EstadoListaEspera.java` (Valores: `EN_ESPERA`, `NOTIFICADO`, `AGENDADO`, `DESCARTADO`).
  - `src/main/java/com/dentalcare/api/models/enums/PrioridadListaEspera.java` (Valores: `ALTA`, `MEDIA`, `BAJA`).
  - `src/main/java/com/dentalcare/api/models/Notificacion.java` (Mapeo de la tabla `notificaciones`).
  - `src/main/java/com/dentalcare/api/models/ConfiguracionAlertaCita.java` (Mapeo de la tabla `configuracion_alerta_cita`).
  - `src/main/java/com/dentalcare/api/models/ListaEspera.java` (Mapeo de la tabla `lista_espera`).
* **Nuevos Data Transfer Objects (DTOs):**
  - `src/main/java/com/dentalcare/api/dtos/Notificacion/NotificacionResponseDto.java`
  - `src/main/java/com/dentalcare/api/dtos/Notificacion/ResumenNotificacionesDto.java`
  - `src/main/java/com/dentalcare/api/dtos/Cita/ConfiguracionAlertaCitaDto.java`
  - `src/main/java/com/dentalcare/api/dtos/Cita/CitaProximaDto.java`
  - `src/main/java/com/dentalcare/api/dtos/Cita/ListaEsperaRequestDto.java`
  - `src/main/java/com/dentalcare/api/dtos/Cita/ListaEsperaResponseDto.java`
* **Nuevos Repositorios Spring Data JPA:**
  - `src/main/java/com/dentalcare/api/repositories/NotificacionRepository.java` (Consultas por usuario/rol, conteo de no leídas y actualización masiva).
  - `src/main/java/com/dentalcare/api/repositories/ConfiguracionAlertaCitaRepository.java`
  - `src/main/java/com/dentalcare/api/repositories/ListaEsperaRepository.java` (Ordenamiento FIFO ponderado por prioridad).
  - Extensión en `src/main/java/com/dentalcare/api/repositories/CitaRepository.java` con consulta `findCitasProximas(...)`.
* **Nuevos Servicios de Negocio:**
  - `src/main/java/com/dentalcare/api/services/NotificacionService.java`
  - `src/main/java/com/dentalcare/api/services/AlertaCitaService.java`
  - `src/main/java/com/dentalcare/api/services/ListaEsperaService.java`
  - Modificación en `src/main/java/com/dentalcare/api/services/CitaService.java` (Integración del disparador automático de lista de espera en `cancelarCita` y `actualizarEstado`).
* **Nuevos Controladores REST:**
  - `src/main/java/com/dentalcare/api/controllers/NotificacionController.java` (`/api/notificaciones`)
  - `src/main/java/com/dentalcare/api/controllers/AlertaCitaController.java` (`/api/agenda/alertas-citas`)
  - `src/main/java/com/dentalcare/api/controllers/ListaEsperaController.java` (`/api/agenda/lista-espera`)
* **Script de Base de Datos:**
  - `database/MIGRACION_SPRINT5_AIVEN.sql` (DDL para `notificaciones`, `configuracion_alerta_cita`, `lista_espera`, `reglas_alerta_rendimiento` y sembrado de prueba).

#### B. Frontend (React 18 + Vite + TailwindCSS)
* **Nuevos Servicios API:**
  - `src/services/notificacion.service.js`
  - `src/services/alertaCita.service.js`
  - `src/services/listaEspera.service.js`
* **Nuevos Componentes y Vistas:**
  - `src/components/NotificationBell.jsx` (Campana en Header con badge animado, polling de 30s y dropdown interactivo con filtros).
  - `src/components/CitaProximaAlertBanner.jsx` (Banner superior en Agenda ante citas inminentes con sintetizador de audio).
  - `src/components/ModalConfigAlertaCita.jsx` (Modal de preferencias de alerta).
  - `src/components/ModalListaEspera.jsx` (Modal para dar de alta pacientes en espera).
  - `src/components/ListaEsperaPanel.jsx` (Panel con tabla de pacientes en cola, filtrado por estado y botón agendar).
  - `src/views/NotificacionesPage.jsx` (Vista completa de historial de notificaciones).
* **Componentes Modificados:**
  - `src/components/Layout.jsx` (Reemplazo del botón estático de campana por `<NotificationBell />`).
  - `src/views/AppointmentPage.jsx` (Inclusión de pestaña `Lista de Espera`, banner de citas próximas y botón de configuración).
  - `src/App.jsx` (Registro de la ruta `/notificaciones`).

---

## 3. MATRIZ MAESTRA DE ENDPOINTS — SPRINT 5 (FASE 1 COMPLETADA)

| Método | Endpoint URI | Roles Autorizados | Estado Técnico | Descripción |
| :---: | :--- | :--- | :---: | :--- |
| `GET` | `/api/notificaciones` | Todos los autenticados | **COMPLETADO (100%)** | Lista notificaciones del usuario/rol con filtro opcional de no leídas |
| `GET` | `/api/notificaciones/resumen-no-leidas` | Todos los autenticados | **COMPLETADO (100%)** | Conteo optimizado para badge en el Navbar |
| `PATCH`| `/api/notificaciones/{id}/marcar-leida` | Todos los autenticados | **COMPLETADO (100%)** | Marca una notificación específica como leída |
| `POST` | `/api/notificaciones/marcar-todas-leidas` | Todos los autenticados | **COMPLETADO (100%)** | Marca todas las notificaciones pendientes del usuario como leídas |
| `POST` | `/api/notificaciones/crear` | Todos los autenticados | **COMPLETADO (100%)** | Creación manual/programática de eventos de notificación |
| `GET` | `/api/agenda/alertas-citas/configuracion` | RECEPCIONISTA, ADMIN | **COMPLETADO (100%)** | Consulta preferencias de anticipación y sonido de alertas |
| `PUT` | `/api/agenda/alertas-citas/configuracion` | RECEPCIONISTA, ADMIN | **COMPLETADO (100%)** | Actualiza preferencias de alerta del usuario |
| `GET` | `/api/agenda/alertas-citas/proximas` | RECEPCIONISTA, ADMIN | **COMPLETADO (100%)** | Citas programadas de hoy dentro de la ventana de aviso (30 min) |
| `GET` | `/api/agenda/lista-espera` | RECEPCIONISTA, ADMIN, GERENTE | **COMPLETADO (100%)** | Consulta pacientes en lista de espera filtrados por estado |
| `GET` | `/api/agenda/lista-espera/{id}` | RECEPCIONISTA, ADMIN | **COMPLETADO (100%)** | Detalle de un registro de lista de espera |
| `POST` | `/api/agenda/lista-espera` | RECEPCIONISTA, ADMIN | **COMPLETADO (100%)** | Registra paciente en lista de espera con preferencias horarias |
| `PATCH`| `/api/agenda/lista-espera/{id}/descartar`| RECEPCIONISTA, ADMIN | **COMPLETADO (100%)** | Cambia estado a `DESCARTADO` |
| `DELETE`| `/api/agenda/lista-espera/{id}` | RECEPCIONISTA, ADMIN | **COMPLETADO (100%)** | Elimina permanentemente registro de la lista |
| `POST` | `/api/agenda/lista-espera/{id}/asignar` | RECEPCIONISTA, ADMIN | **COMPLETADO (100%)** | Vincula cita formal y cambia estado a `AGENDADO` |

---

## 4. REGISTRO DE DECISIONES DE ARQUITECTURA DE SOFTWARE (ADRS) — FASE 1

### ADR-S5-01: Arquitectura de Notificaciones Desacoplada y Polling Suave
* **Problema:** Los eventos de citas próximas, cancelaciones de agenda y avisos clínicos deben ser consumidos en tiempo real por el frontend sin imponer sobrecarga de conexión WebSocket persistente al cluster de base de datos Aiven.
* **Solución Implementada:** Se diseñó la entidad `notificaciones` con un endpoint ligero `GET /api/notificaciones/resumen-no-leidas` que realiza una consulta `COUNT` indexada. En el frontend, `NotificationBell.jsx` ejecuta un polling suave cada 30 segundos, manteniendo el contador de alertas actualizado con impacto nulo en el rendimiento del servidor.

### ADR-S5-02: Detección Automática de Pacientes en Lista de Espera por Desacoplamiento de Eventos
* **Problema:** Cuando una cita es cancelada, la recepcionista no siempre recuerda consultar la lista de espera, lo que provoca que los cupos queden sin aprovechar.
* **Solución Implementada:** Se integró un hook en `CitaService` que intercepta tanto `cancelarCita(...)` como `actualizarEstado(..., CANCELADA)`. Al dispararse, `ListaEsperaService.evaluarCupoLiberadoPorCancelacion` busca en `lista_espera` registros con `estado = 'EN_ESPERA'` priorizando `ALTA` > `MEDIA` > `BAJA` y orden de solicitud (FIFO). Al encontrar un candidato, actualiza su estado a `NOTIFICADO` y genera automáticamente una notificación dirigida al rol `RECEPCIONISTA` con enlace directo para agendar.

### ADR-S5-03: Aviso Sonoro Integrado sin Dependencias Externas (Web Audio API)
* **Problema:** Los archivos de audio estáticos (`.mp3` / `.wav`) pueden fallar por problemas de ruta relativa o políticas de CORS en navegadores modernos.
* **Solución Implementada:** En `CitaProximaAlertBanner.jsx` se implementó un oscilador sintetizado nativo mediante `AudioContext` de HTML5, emitiendo un acorde armónico discreto (D5 a A5) que no requiere archivos externos ni dependencias pesadas.

---

## 5. BITÁCORA DE CONTROL DE CALIDAD Y PRUEBAS DE REGRESIÓN

| Escenario de Verificación | Subsistema | Resultado Técnico | Estado |
| :--- | :--- | :--- | :---: |
| **Compilación Maven Backend** | Spring Boot / Java 17 | `Compiling 123 source files with javac -> BUILD SUCCESS` | **APROBADO** |
| **Build Vite Frontend** | React 18 / Tailwind | `98 modules transformed -> dist/index.html built in 4.46s` | **APROBADO** |
| **Campana y Badge en Layout** | Notificaciones UI | Badge numérico reactivo, dropdown con pestañas y marcado de lectura | **APROBADO** |
| **Configuración Alerta Citas** | Agenda / Modal | Guardado de preferencias de tiempo (15, 30, 45, 60 min) y sonido | **APROBADO** |
| **Banner Citas a 30 Minutos** | Agenda / Alertas | Detección de citas del día dentro de la ventana de anticipación | **APROBADO** |
| **Cola de Lista de Espera** | Agenda / Espera | Registro con horarios/días preferidos, prioridades y descarte | **APROBADO** |
| **Intercepción Cancelar Cita** | Integración Cita-Espera | Disparo de notificación automática a recepcionista ante cancelación | **APROBADO** |
| **Regresión: Flujo Clínico Sprint 3** | Consultas Odontológicas | Stepper clínico y prescripciones operan con total normalidad | **APROBADO** |
| **Regresión: Respaldos Sprint 4** | Seguridad TI / Inventario | Módulos de dump SQL, reset de contraseña e inventario intactos | **APROBADO** |
