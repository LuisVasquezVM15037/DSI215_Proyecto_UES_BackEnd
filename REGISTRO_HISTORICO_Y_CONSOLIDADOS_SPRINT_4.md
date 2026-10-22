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
| **Fase 2** | Agenda Inteligente y Detección de Conflictos | Recepcionista / Secretaria | `DENTALCARE-62`, `DENTALCARE-60` | 4.0 SP | *Pendiente* |
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
  - `MaterialRequestDTO.java` (Anotaciones: `@NotBlank`, `@Pattern(MT\d+)`, `@Size(5-100)`, `@Min(0)`).
  - `MaterialResponseDTO.java` (Respuesta con estado de alerta calculado).
  - `MedicamentoItemRequestDTO.java` (Anotaciones: `@Pattern(MD\d+)`, `@Size(5-100)`, `@Min(1)`).
  - `MedicamentoItemResponseDTO.java` (Respuesta de medicamentos con lote y fecha de vencimiento).
  - `ItemInventarioResumenDTO.java` (DTO unificado para búsqueda y filtrado general).
  - `AjusteStockDTO.java` (Payload para ajuste rápido de stock físico y mínimo).
  - `ConsumoRequestDTO.java` (Solicitud de registro o corrección de consumo).
  - `ConsumoResponseDTO.java` (Respuesta con datos de paciente, cita y stock disponible).
* **Nuevos Repositorios Spring Data JPA (`repositories/`):**
  - `MaterialRepository.java` (`findByCodigo`, `existsByCodigo`, consultas de búsqueda ignorando mayúsculas).
  - `MedicamentoItemRepository.java` (`findByCodigo`, `existsByCodigo`, listado alfabético).
  - `ConsumoRepository.java` (`findByMesConsumoOrderByFechaConsumoDesc`, listado cronológico).
* **Capa de Negocio y Controladores:**
  - `src/main/java/com/dentalcare/api/services/InventarioService.java` (Lógica de validación de nomenclaturas, listado unificado, transacciones atómicas de confirmación y descuento de stock).
  - `src/main/java/com/dentalcare/api/controllers/InventarioController.java` (Controlador REST bajo `/api/inventario/**`).
* **Migración y Sembrado Inicial Defensivo:**
  - `src/main/java/com/dentalcare/api/config/DatabaseSchemaMigrationRunner.java` (Sembrado automático de materiales, fármacos e insumos demo para Sprint 4 si las tablas se encuentran vacías).

#### B. Frontend (React 18 + Vite + Tailwind CSS)
* **Capa de Servicios y Hooks HTTP:**
  - `src/services/inventario.service.js` (Llamadas REST a `/inventario/materiales`, `/inventario/medicamentos`, `/inventario/items`, `/inventario/consumos`).
  - `src/hooks/useInventario.js` (Orquestador de estado, búsquedas debounceadas, carga reactiva y mutaciones con alertas).
* **Componentes de Dominio (`src/components/inventario/`):**
  - `MaterialForm.jsx` (Formulario con validación en vivo, borde rojo para errores, toast verde y tarjeta de redirección).
  - `MedicamentoIngresoForm.jsx` (Formulario de ingreso de medicamentos con validación de cantidad > 0 y prefijo MD).
  - `InventarioTable.jsx` (Tabla unificada, filtro por tipo, indicadores de stock crítico y modal de ajuste).
  - `ConsumosMensuales.jsx` (Interfaz de recuadros mensuales, modificación de datos y confirmación con descuento atómico).
* **Vistas y Enrutamiento:**
  - `src/views/InventarioPage.jsx` (Página principal con métricas rápidas y sistema de 4 pestañas interactivas).
  - `src/App.jsx` (Registro de la ruta `/inventario` protegida por RBAC para roles `ADMIN`, `ASISTENTEODONTOLOGO`, `GERENTE`).
  - `src/components/Layout.jsx` (Inclusión del enlace "Inventario" en `NAV_ITEMS` del sidebar).

---

### 2.3 Catálogo de Endpoints REST Creados (Fase 1)

| Método | Endpoint | Roles Permitidos | Parámetros / Payload Clave | Código Respuesta |
| :---: | :--- | :--- | :--- | :---: |
| `POST` | `/api/inventario/materiales` | Asistente, Admin | `{ codigo: "MT78", nombre: "...", unidadMedida: "...", stockActual: 25, stockMinimo: 5 }` | `201 Created` |
| `GET` | `/api/inventario/materiales` | Asistente, Admin | Listado completo de materiales ordenados alfabéticamente. | `200 OK` |
| `PATCH` | `/api/inventario/materiales/{id}` | Asistente, Admin | `{ stockActual: 30, stockMinimo: 8, motivoAjuste: "Conteo físico" }` | `200 OK` |
| `POST` | `/api/inventario/medicamentos` | Asistente, Admin | `{ codigo: "MD45", nombre: "...", cantidadIngreso: 50, lote: "LOT-X9", fechaVencimiento: "2027-08-31" }` | `201 Created` |
| `GET` | `/api/inventario/medicamentos` | Asistente, Admin | Listado completo de medicamentos del inventario. | `200 OK` |
| `PATCH` | `/api/inventario/medicamentos/{id}` | Asistente, Admin | `{ stockActual: 45, motivoAjuste: "Ajuste quincenal" }` | `200 OK` |
| `GET` | `/api/inventario/items` | Todos autenticados | Query params: `?search=resina&tipo=TODOS` | `200 OK` |
| `GET` | `/api/inventario/consumos` | Asistente, Admin | Query param opcional: `?mes=2026-10` | `200 OK` |
| `POST` | `/api/inventario/consumos` | Asistente, Admin | `{ tipoItem: "MATERIAL", itemId: 1, cantidad: 2, mesConsumo: "2026-10", notas: "..." }` | `201 Created` |
| `PUT` | `/api/inventario/consumos/{id}` | Asistente, Admin | `{ cantidad: 3, notas: "Se requirió unidad extra por fractura" }` | `200 OK` |
| `POST` | `/api/inventario/consumos/{id}/confirmar` | Asistente, Admin | Confirmación atómica que descuenta stock en MySQL y pasa a estado `CONFIRMADO`. | `200 OK` |

---

### 2.4 Verificación Técnica de Compilación

1. **Backend (Maven / Spring Boot):**
   ```text
   [INFO] Compiling 92 source files with javac [debug parameters release 17] to target\classes
   [INFO] BUILD SUCCESS - Total time: 13.050 s
   ```
2. **Frontend (Vite / React 18):**
   ```text
   ✓ 84 modules transformed.
   dist/assets/InventarioPage-DJivqIds.js  52.74 kB │ gzip: 9.61 kB
   ✓ built in 3.01s
   ```

---
*Fin del registro oficial de la Fase 1 — DentalCare ERP (Sprint 4).*
