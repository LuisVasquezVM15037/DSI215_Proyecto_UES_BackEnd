# REGISTRO HISTÓRICO Y CONSOLIDADO DE CAMBIOS — SPRINT 4
> **Proyecto:** Sistema Informático para la Gestión de Servicios Médicos en la Clínica Odontológica DentalCare ERP  
> **Cátedra:** Diseño de Sistemas II (DSI215) — Ciclo II-2026  
> **Institución:** Universidad de El Salvador (UES)  
> **Documento Oficial de Evaluación:** Bitácora consolidada de modificaciones, componentes, servicios y endpoints implementados para la evaluación final de cátedra.

---

## 📊 1. TABLERO GENERAL DE AVANCE DEL SPRINT 4

| Fase | Subsistema / Módulo | Rol Responsable | PBIs Cubiertos | Story Points | Estado Técnico |
| :---: | :--- | :--- | :--- | :---: | :---: |
| **Fase 1** | **Gestión de Suministros, Inventario y Consumo** | **Asistente Dental** / Admin | `DENTALCARE-26`, `DENTALCARE-48`, `DENTALCARE-46`, `DENTALCARE-47` | **11.0 SP** | **COMPLETADA (100%)** |
| **Fase 2** | **Agenda Inteligente y Control de Conflictos** | **Recepcionista** / Secretaria | `DENTALCARE-62`, `DENTALCARE-60` | **4.0 SP** | **COMPLETADA (100%)** |
| **Fase 3** | Seguridad Avanzada y Contingencia de Datos | Administrador TI | `DENTALCARE-93`, `DENTALCARE-53`, `DENTALCARE-64` | 11.0 SP | *Pendiente* |
| **Transversal** | Directrices de Calidad Transversal (Lab II) | Equipo General | `DENTALCARE-97` | — | *En curso / Aplicada* |

---

## 🛠️ 2. CONSOLIDADO DETALLADO — FASE 1: SUBSISTEMA DE INVENTARIO Y CONSUMOS

### 2.1 Historias de Usuario Abordadas y Verificación de Criterios de Aceptación (AC)

#### 1. `DENTALCARE-26` — PBI-AsisDent-01: Registrar Materiales (3.0 SP)
* **Objetivo:** Permitir al Asistente Dental registrar nuevos insumos y materiales clínicos (resinas, algodón, etc.) en el catálogo.
* **Criterios de Aceptación Cumplidos:**
  - [x] Registro con código de formato alfanumérico con prefijo obligatorio `MT` (ej: `MT78`, regex `^MT\d+$`).
  - [x] Validación estricta de longitud de nombre entre 5 y 100 caracteres.
  - [x] Casillas con **borde rojo reactivo** (`border-2 border-red-500 bg-red-50 text-red-900`) e ícono de advertencia ante entradas no válidas.
  - [x] Notificación toast verde de éxito al guardar.
  - [x] Notificación modal/toast descriptiva si ocurre un error (ej. código duplicado en base de datos).
  - [x] Botón/enlace de redirección directa al listado general de materiales tras el guardado exitoso.

#### 2. `DENTALCARE-48` — PBI-AsisDent-04: Registrar Ingreso de Medicamentos (4.0 SP)
* **Objetivo:** Controlar el abastecimiento de fármacos, anestésicos y antibióticos en la clínica.
* **Criterios de Aceptación Cumplidos:**
  - [x] Registro con código único con prefijo `MD` (ej: `MD45`, regex `^MD\d+$`).
  - [x] Nombre del medicamento validado entre 5 y 100 caracteres.
  - [x] Campo de cantidad de ingreso mayor que cero (`cantidadIngreso >= 1`).
  - [x] Manejo de presentación comercial, lote y fecha de vencimiento.
  - [x] Borde rojo y feedback visual inmediato ante entradas no válidas.
  - [x] Notificación de confirmación amigable al ingresar y redirección directa al catálogo.

#### 3. `DENTALCARE-46` — PBI-AsisDent-02: Consultar y Actualizar Inventario (2.0 SP)
* **Objetivo:** Proveer una cuadrícula centralizada de consulta y actualización rápida de stock físico.
* **Criterios de Aceptación Cumplidos:**
  - [x] Listado tabular unificado que consolida tanto materiales como medicamentos.
  - [x] Buscador reactivo en tiempo real con debounce por código o nombre.
  - [x] Filtros por categorías: *Todos*, *Solo Materiales* y *Solo Medicamentos*.
  - [x] Indicadores cromáticos de stock: Verde (*Óptimo*), Amarillo (*Stock Bajo*) y Rojo pulsante (*Agotado / Crítico*).
  - [x] Modal de edición rápida para ajustar stock físico actual y stock mínimo sin recargar la página.
  - [x] Notificación de actualización y refresco inmediato en la vista mediante TanStack Query.

#### 4. `DENTALCARE-47` — PBI-AsisDent-03: Confirmar Consumo Mensual (2.0 SP)
* **Objetivo:** Registro, ajuste y confirmación de consumos mensuales con descuento atómico de stock.
* **Criterios de Aceptación Cumplidos:**
  - [x] Interfaz basada en **recuadros / tarjetas mensuales** que detallan cada consumo realizado en el mes.
  - [x] Selector dinámico de periodos mensuales (ej. `2026-10`, `2026-09`, etc.).
  - [x] Opción `[Modificar Datos]` en cada tarjeta para corregir cantidades o justificaciones antes de confirmar.
  - [x] Botón `[Confirmar Consumo]`: valida que existan existencias suficientes en almacén, ejecuta transacción atómica en backend (`@Transactional`), descuenta el stock físico de la entidad correspondiente y actualiza la tarjeta a estado `CONFIRMADO`.
  - [x] Notificación de error si se intenta confirmar un consumo que supere las existencias físicas en bodega.
  - [x] Modal `[+ Registrar Consumo]` para dar de alta consumos manuales de insumos.

---

### 2.2 Inventario de Archivos Creados y Modificados en Fase 1

#### A. Backend (Spring Boot 3 + Java 17 + Hibernate 6)
* **Nuevos Modelos y Enums JPA:**
  - `src/main/java/com/dentalcare/api/models/enums/TipoItem.java` (Valores: `MATERIAL`, `MEDICAMENTO`).
  - `src/main/java/com/dentalcare/api/models/enums/EstadoConsumo.java` (Valores: `PENDIENTE`, `CONFIRMADO`, `ANULADO`).
  - `src/main/java/com/dentalcare/api/models/Material.java` (Mapeo a tabla `materiales`).
  - `src/main/java/com/dentalcare/api/models/MedicamentoItem.java` (Mapeo a tabla `medicamentos`).
  - `src/main/java/com/dentalcare/api/models/Consumo.java` (Mapeo a tabla `consumos`, con FK a `usuario` y `cita`).
* **Nuevos DTOs con Jakarta Bean Validation (`dtos/Inventario/`):**
  - `MaterialRequestDTO.java`, `MaterialResponseDTO.java`, `MedicamentoItemRequestDTO.java`, `MedicamentoItemResponseDTO.java`, `ItemInventarioResumenDTO.java`, `AjusteStockDTO.java`, `ConsumoRequestDTO.java`, `ConsumoResponseDTO.java`.
* **Nuevos Repositorios Spring Data JPA (`repositories/`):**
  - `MaterialRepository.java`, `MedicamentoItemRepository.java`, `ConsumoRepository.java`.
* **Capa de Negocio y Controladores:**
  - `src/main/java/com/dentalcare/api/services/InventarioService.java`.
  - `src/main/java/com/dentalcare/api/controllers/InventarioController.java` (`/api/inventario/**`).
* **Migración y Sembrado Inicial Defensivo:**
  - `src/main/java/com/dentalcare/api/config/DatabaseSchemaMigrationRunner.java`.

#### B. Frontend (React 18 + Vite + Tailwind CSS)
* **Servicios y Hooks:** `src/services/inventario.service.js`, `src/hooks/useInventario.js`.
* **Componentes:** `MaterialForm.jsx`, `MedicamentoIngresoForm.jsx`, `InventarioTable.jsx`, `ConsumosMensuales.jsx`.
* **Vistas y Navegación:** `src/views/InventarioPage.jsx`, `src/App.jsx` (`/inventario`), `src/components/Layout.jsx`.

---

## 📅 3. CONSOLIDADO DETALLADO — FASE 2: AGENDA INTELIGENTE Y DETECCIÓN DE CONFLICTOS

### 3.1 Historias de Usuario Abordadas y Verificación de Criterios de Aceptación (AC)

#### 1. `DENTALCARE-62` — PBI-Secret-09: Identificar Horarios Disponibles (2.0 SP)
* **Rol:** Recepcionista / Secretaria.
* **Objetivo:** Mostrar de forma visual y dinámica únicamente los bloques horarios libres en la jornada laboral del odontólogo seleccionado.
* **Criterios de Aceptación Cumplidos:**
  - [x] El sistema calcula y muestra **únicamente los horarios libres** correspondientes al odontólogo y fecha elegidos.
  - [x] Actualización de la disponibilidad en **tiempo real** al cambiar el doctor o la fecha de la cita mediante el endpoint `/api/citas/disponibilidad` (y `/api/agenda/disponibilidad`).
  - [x] Permite la **selección directa con un solo clic** sobre la franja horaria deseada, inyectando de forma instantánea y automática tanto la `horaInicioCita` como la `horaFinCita` en el formulario.
  - [x] Las franjas horarias ocupadas se renderizan visualmente deshabilitadas con estilo tachado y tooltip informativo (*"Ocupado: Cita #..."*).

#### 2. `DENTALCARE-60` — PBI-Secret-08: Detectar Conflictos de Duplicación de Citas (2.0 SP)
* **Rol:** Recepcionista / Secretaria.
* **Objetivo:** Evitar solapamientos, citas duplicadas o asignaciones incompatibles para un doctor o un paciente en el mismo horario.
* **Criterios de Aceptación Cumplidos:**
  - [x] **Detección Preventiva en Frontend:** En cuanto el usuario introduce una hora que colisiona con una cita existente del especialista, el sistema despliega un banner de alerta visible: *"⚠️ Conflicto de Horario Detectado: El odontólogo ya cuenta con una cita en el bloque seleccionado"*.
  - [x] **Bloqueo de Asignación en Interfaz:** El botón de envío ("Confirmar Cita" o "Guardar Cambios") queda deshabilitado mientras persista un solapamiento con horarios ocupados.
  - [x] **Validación Defensiva en Backend:** `CitaService` valida formalmente las colisiones de intervalos horVictor (`horaInicio < citaFin && horaFin > citaInicio`) tanto para el odontólogo como para el paciente (excluyendo la cita activa si es actualización/reprogramación). Ante cualquier intento de solapamiento, responde con código semántico **`HTTP 409 Conflict`**.
  - [x] **Blindaje en Reprogramación:** Se extendió el modal `ReprogramModal.jsx` con el selector de franjas horarias y el validador de conflictos para evitar solapamientos al mover citas de fecha/hora.

---

### 3.2 Inventario de Archivos Creados y Modificados en Fase 2

#### A. Backend (Spring Boot 3 + Java 17 + Hibernate 6)
* **Nuevos DTOs (`dtos/Cita/`):**
  - `FranjaHorariaDTO.java` (`horaInicio`, `horaFin`, `horaInicioIso`, `horaFinIso`, `disponible`, `motivo`, `idCita`).
  - `DisponibilidadHorarioDTO.java` (`idOdontologo`, `nombreOdontologo`, `fecha`, `horariosDisponibles`).
* **Modificación de Repositorio:**
  - `src/main/java/com/dentalcare/api/repositories/CitaRepository.java`:
    - `findCitasActivasPorOdontologoYFecha(...)`
    - `findConflictosOdontologo(...)`
    - `findConflictosOdontologoExcluyendo(...)`
    - `findConflictosPaciente(...)`
    - `findConflictosPacienteExcluyendo(...)`
* **Capa de Servicios y Controladores:**
  - `src/main/java/com/dentalcare/api/services/CitaService.java`:
    - Implementación del algoritmo de partición y cálculo de disponibilidad horaria `obtenerDisponibilidad(idOdontologo, fecha)` (jornada 08:00 a 17:00).
    - Implementación de la validación defensiva `validarConflictoHorario(...)` conectada en `crearCita` y `actualizarCita` con respuestas `409 Conflict`.
  - `src/main/java/com/dentalcare/api/controllers/CitaController.java`:
    - Endpoint `GET /api/citas/disponibilidad`.
  - `src/main/java/com/dentalcare/api/controllers/AgendaController.java`:
    - Endpoint `GET /api/agenda/disponibilidad` (alias directo de la guía).

#### B. Frontend (React 18 + Vite + Tailwind CSS)
* **Servicios HTTP:**
  - `src/services/cita.service.js`: adición del método `getDisponibilidad(idOdontologo, fecha)`.
* **Componentes de Agenda:**
  - `src/components/AppointmentForm.jsx`:
    - Integración de consulta reactiva de disponibilidad al cambiar doctor o fecha.
    - Cuadrícula interactiva de selección directa con un clic.
    - Banner reactivo de conflicto de horario y bloqueo de confirmación.
  - `src/components/ReprogramModal.jsx`:
    - Integración de selección de franjas horarias libres y prevención de colisión al reprogramar citas.

---

### 3.3 Catálogo de Endpoints REST Actualizados (Fase 2)

| Método | Endpoint | Roles Permitidos | Parámetros / Query Clave | Código Respuesta |
| :---: | :--- | :--- | :--- | :---: |
| `GET` | `/api/citas/disponibilidad` | Recepcionista, Admin, Odontólogo | `?idOdontologo=4&fecha=2026-10-24` | `200 OK` |
| `GET` | `/api/agenda/disponibilidad` | Recepcionista, Admin, Odontólogo | `?odontologo_id=4&fecha=2026-10-24` | `200 OK` |
| `POST` | `/api/citas` | Recepcionista, Admin | `{ idOdontologo, idPaciente, fechaCita, horaInicioCita, horaFinCita }` | `201 Created` / `409 Conflict` |
| `PUT` | `/api/citas/{id}` | Recepcionista, Admin | Payload con nuevos horarios de la cita | `200 OK` / `409 Conflict` |

---

### 3.4 Verificación Técnica de Compilación

1. **Backend (Maven / Spring Boot):**
   ```text
   [INFO] Compiling 95 source files with javac [debug parameters release 17] to target\classes
   [INFO] BUILD SUCCESS - Total time: 9.604 s
   ```
2. **Frontend (Vite / React 18):**
   ```text
   ✓ 84 modules transformed.
   dist/assets/AppointmentPage-D9sQM0ir.js  33.69 kB │ gzip: 8.50 kB
   ✓ built in 13.76s
   ```

---
*Fin del registro oficial de la Fase 2 — DentalCare ERP (Sprint 4).*
