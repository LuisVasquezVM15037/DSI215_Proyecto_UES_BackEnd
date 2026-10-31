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
| **Fase 3** | **Seguridad Avanzada, Reseteo y Contingencia BD** | **Administrador TI** | `DENTALCARE-93`, `DENTALCARE-53`, `DENTALCARE-64` | **11.0 SP** | **COMPLETADA (100%)** |
| **Transversal** | Directrices de Calidad Transversal (Lab II) | Equipo General | `DENTALCARE-97` | — | **COMPLETADA (100%)** |

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
  - [x] **Validación Defensiva en Backend:** `CitaService` valida formalmente las colisiones de intervalos horarios (`horaInicio < citaFin && horaFin > citaInicio`) tanto para el odontólogo como para el paciente (excluyendo la cita activa si es actualización/reprogramación). Ante cualquier intento de solapamiento, responde con código semántico **`HTTP 409 Conflict`**.
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
    - Endpoint `GET /api/agenda/disponibilidad`.

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

## 🔐 4. CONSOLIDADO DETALLADO — FASE 3: SEGURIDAD AVANZADA, RESETEO DE CLAVE Y CONTINGENCIA DE BD

### 4.1 Historias de Usuario Abordadas y Verificación de Criterios de Aceptación (AC)

#### 1. `DENTALCARE-93` — PBI-AdminTI-06: Restablecer Contraseña de Usuario (5.0 SP)
* **Rol:** Administrador TI.
* **Objetivo:** Brindar asistencia técnica inmediata a cualquier miembro del personal dental que haya olvidado sus credenciales o requiera reinicio de acceso.
* **Criterios de Aceptación Cumplidos:**
  - [x] **Generación de Contraseña Temporal y Token Criptográfico:** El sistema genera una clave temporal aleatoria (ej: `Temp#849201`) y un token seguro de 128 bits con fecha de caducidad estricta de 24 horas (`expira_en`).
  - [x] **Notificación Institucional:** Se simula y genera el envío formal del correo institucional con las credenciales temporales y el enlace tokenizado de acceso directo `/reset-password?token=XYZ`.
  - [x] **Forzado Obligatorio de Cambio de Contraseña:** Se persiste la bandera `debe_cambiar_password = true` en la tabla `usuario`. Al iniciar sesión con la clave provisional, el sistema detecta la condición y redirige forzosamente a la pantalla `/cambiar-password-obligatorio`.
  - [x] **Validación Defensiva de Credenciales:** La interfaz y el backend exigen que la nueva contraseña posea un mínimo de 6 caracteres, confirmación idéntica y que difiera de la clave temporal. Al completarse con éxito, se actualiza el hash BCrypt, se remueve la bandera (`debe_cambiar_password = false`), se inhabilita el token y se permite el acceso al panel.
  - [x] **Integración en Panel de Usuarios:** Se agregó el botón contextual `Restablecer Clave` en `UserManagementPage.jsx`, abriendo el modal `ModalRestablecerPassword.jsx` con copiado rápido con un clic de la clave y del enlace.

#### 2. `DENTALCARE-53` — PBI-AdminTi-03: Ejecutar Respaldos de Base de Datos (3.0 SP)
* **Rol:** Administrador TI.
* **Objetivo:** Facilitar la ejecución manual de volcados completos de la base de datos relacional MySQL (`BD_DentalCareWeb`) sin interrumpir la operación clínica.
* **Criterios de Aceptación Cumplidos:**
  - [x] **Ejecución a Petición:** Botón *"Generar Respaldo Manual"* en `/mantenimiento/respaldos` que desencadena la extracción completa del esquema DDL y todos los registros relacionales.
  - [x] **Motor Portátil de Dump JDBC:** Implementado mediante `SeguridadMantenimientoService` a través de metadata de conexiones JDBC y `Statement` batch, garantizando generación consistente e independiente de utilitarios externos del sistema operativo.
  - [x] **Nomenclatura Estandarizada:** Los archivos se generan bajo el patrón `backup_dentalcare_YYYYMMDD_HHMMSS.sql` y se guardan en el directorio seguro `backups/`.
  - [x] **Trazabilidad Histórica:** Cada respaldo queda asentado en la tabla `respaldos` con su nombre, tamaño en bytes, ruta, usuario generador y estado `EXITOSO`.
  - [x] **Descarga Directa:** Cada registro cuenta con botón de descarga que transmite el archivo `.sql` al navegador del administrador.

#### 3. `DENTALCARE-64` — PBI-AdminTi-05: Restaurar Información desde Respaldo (3.0 SP)
* **Rol:** Administrador TI.
* **Objetivo:** Proveer un mecanismo confiable de recuperación ante incidentes o desastres tecnológicos, restituyendo la base de datos a un punto de control previo.
* **Criterios de Aceptación Cumplidos:**
  - [x] **Selección de Respaldo:** Permite restaurar desde cualquier copia del listado histórico o mediante la subida de un archivo externo `.sql`.
  - [x] **Doble Confirmación Crítica:** Modal con advertencia destructiva en rojo:
    - Exige escribir exactamente la palabra clave en mayúsculas `RESTAURAR`.
    - Exige ingresar la **contraseña actual del Administrador TI** autenticado.
    - El botón de restauración permanece bloqueado mientras ambos requisitos no se cumplan.
  - [x] **Restauración Segura en Backend:** `SeguridadMantenimientoService` valida la contraseña del admin mediante `PasswordEncoder.matches(...)`, deshabilita temporalmente `FOREIGN_KEY_CHECKS = 0;`, ejecuta secuencialmente el volcado DDL/DML, y reactiva la integridad relacional `FOREIGN_KEY_CHECKS = 1;`.
  - [x] **Feedback y Notificación:** Notificaciones toast y modales informativos que notifican el éxito de la restauración.

---

### 4.2 Inventario de Archivos Creados y Modificados en Fase 3

#### A. Backend (Spring Boot 3 + Java 17 + Hibernate 6)
* **Modificación de Entidades Existentes:**
  - `src/main/java/com/dentalcare/api/models/Usuario.java`:
    - Adición de `@Column(name = "debe_cambiar_password") private Boolean debeCambiarPassword = false;`.
* **Nuevas Entidades JPA:**
  - `src/main/java/com/dentalcare/api/models/PasswordResetToken.java` (Tabla `password_resets`: `usuario`, `token`, `claveTemporal`, `expiraEn`, `utilizado`, `creadoEn`).
  - `src/main/java/com/dentalcare/api/models/Respaldo.java` (Tabla `respaldos`: `nombreArchivo`, `rutaAlmacenamiento`, `tamanioBytes`, `estado`, `usuario`, `creadoEn`).
* **Nuevos DTOs (`dtos/Seguridad/` y `dtos/Login/`):**
  - `RestablecerPasswordResponseDTO.java` (`idUsuario`, `username`, `email`, `claveProvisional`, `token`, `expiraEn`, `enlaceRestablecimiento`, `mensajeNotificacion`).
  - `CambiarPasswordDTO.java` (`username`, `passwordActual`, `token`, `passwordNueva`, `confirmarPasswordNueva`).
  - `RespaldoResponseDTO.java` (`id`, `nombreArchivo`, `rutaAlmacenamiento`, `tamanioBytes`, `tamanioFormateado`, `estado`, `creadoPor`, `creadoEn`).
  - `RestaurarRespaldoRequestDTO.java` (`adminPassword`, `confirmacionTexto`, `adminUsername`).
  - `src/main/java/com/dentalcare/api/dtos/Login/LoginResponseDto.java`: soporte para `debeCambiarPassword` y `username`.
* **Nuevos Repositorios Spring Data JPA:**
  - `src/main/java/com/dentalcare/api/repositories/PasswordResetRepository.java`.
  - `src/main/java/com/dentalcare/api/repositories/RespaldoRepository.java`.
* **Capa de Servicios y Controladores:**
  - `src/main/java/com/dentalcare/api/services/SeguridadMantenimientoService.java`:
    - Reseteo administrativo, generación de claves provisionales, validación y forzado de cambio de clave.
    - Generación portátil de volcados SQL, descarga y ejecución controlada de restauración con validación de credenciales admin y desactivación temporal de foreign keys.
  - `src/main/java/com/dentalcare/api/services/AutentificacionService.java`:
    - Inclusión de bandera `debeCambiarPassword` en el payload de respuesta de login.
  - `src/main/java/com/dentalcare/api/controllers/SeguridadMantenimientoController.java`:
    - Controlador REST centralizado para seguridad, tokens, respaldos y restauraciones.
* **Ajustes de Base de Datos y Migración:**
  - `src/main/java/com/dentalcare/api/config/DatabaseSchemaMigrationRunner.java`:
    - Chequeo defensivo y agregación DDL de la columna `debe_cambiar_password` en `usuario`.

#### B. Frontend (React 18 + Vite + Tailwind CSS)
* **Servicios de Red:**
  - `src/services/seguridad.service.js`: métodos `restablecerPassword`, `validarTokenReset`, `cambiarPasswordObligatorio`, `generarRespaldo`, `listarRespaldos`, `descargarRespaldo`, `restaurarRespaldo`, `restaurarDesdeArchivo`.
  - `src/services/auth.service.js`: almacenamiento y lectura de `debeCambiarPassword` e identificador de usuario.
* **Componentes y Modales:**
  - `src/components/seguridad/ModalRestablecerPassword.jsx`: modal de confirmación y presentación de clave provisional con botones de copiado y vista previa del correo.
* **Vistas de Seguridad y Mantenimiento:**
  - `src/views/CambiarPasswordObligatorioPage.jsx`: vista protegida de cambio forzado de clave con validaciones cromáticas en tiempo real (`#ef4444`).
  - `src/views/ResetPasswordTokenPage.jsx`: vista pública tokenizada para restablecer contraseña mediante enlace `/reset-password?token=...`.
  - `src/views/MantenimientoRespaldosPage.jsx`: panel completo de administración TI para respaldos, métricas de almacenamiento, descarga de archivos `.sql` y modal de doble confirmación crítica con palabra clave `RESTAURAR` y clave de administrador.
  - `src/views/UserManagementPage.jsx`: integración de botón *"Restablecer Clave"* en la ficha del personal.
  - `src/views/LoginPage.jsx`: intercepción de inicio de sesión que redirige a `/cambiar-password-obligatorio` si `debeCambiarPassword === true`.
* **Navegación y Rutas:**
  - `src/App.jsx`: registro de rutas `/cambiar-password-obligatorio`, `/reset-password` y `/mantenimiento/respaldos`.
  - `src/components/Layout.jsx`: adición de la opción *"Respaldos y BD"* (`bi-database-gear`) en la barra lateral exclusiva para rol `ADMIN`.

---

### 4.3 Catálogo Consolidado de Endpoints REST — Fase 3

| Método | Endpoint | Roles Permitidos | Payload / Parámetros | Descripción |
| :---: | :--- | :--- | :--- | :--- |
| `POST` | `/api/seguridad/restablecer-password/{idUsuario}` | Administrador TI | ID en ruta de usuario | Genera clave provisional, token 24h y setea `debe_cambiar_password = 1`. |
| `GET` | `/api/seguridad/validar-token/{token}` | Público / Autenticado | Token en ruta | Valida vigencia (24h) y no utilización previa del token criptográfico. |
| `POST` | `/api/seguridad/cambiar-password-obligatorio` | Público / Autenticado | `{ username, passwordActual, token, passwordNueva, confirmarPasswordNueva }` | Actualiza contraseña en BCrypt y retira la bandera de cambio obligatorio. |
| `POST` | `/api/mantenimiento/respaldos/generar` | Administrador TI | Ninguno | Genera volcado `.sql` completo de MySQL en el directorio `backups/`. |
| `GET` | `/api/mantenimiento/respaldos` | Administrador TI | Ninguno | Retorna historial cronológico de respaldos con tamaños formateados. |
| `GET` | `/api/mantenimiento/respaldos/{id}/descargar` | Administrador TI | ID en ruta | Descarga en streaming el archivo `.sql` como archivo adjunto (`attachment`). |
| `POST` | `/api/mantenimiento/respaldos/{id}/restaurar` | Administrador TI | `{ adminPassword, confirmacionTexto: "RESTAURAR" }` | Ejecuta la restauración total de la BD a partir del archivo registrado. |
| `POST` | `/api/mantenimiento/respaldos/restaurar-archivo` | Administrador TI | `multipart/form-data` (file, adminPassword, confirmacionTexto) | Restaura la BD a partir de un archivo `.sql` subido manualmente. |

---

### 4.4 Verificación Técnica de Compilación

1. **Backend (Maven / Spring Boot 3 / Java 17):**
   ```text
   [INFO] Compiling 105 source files with javac [debug parameters release 17] to target\classes
   [INFO] BUILD SUCCESS
   [INFO] Total time: 15.651 s
   ```
2. **Frontend (Vite / React 18 / Tailwind CSS):**
   ```text
   ✓ 89 modules transformed.
   dist/assets/MantenimientoRespaldosPage-Bfcpw1Dm.js       15.74 kB │ gzip:  3.95 kB
   dist/assets/UserManagementPage-C-CzOSDV.js               18.52 kB │ gzip:  5.06 kB
   dist/assets/CambiarPasswordObligatorioPage-DVrwpAbB.js    7.88 kB │ gzip:  2.32 kB
   dist/assets/ResetPasswordTokenPage-Cm4LfF6f.js            6.31 kB │ gzip:  2.11 kB
   dist/assets/seguridad.service-79M5AxyD.js                 1.52 kB │ gzip:  0.71 kB
   ✓ built in 2.89s
   ```

---
*Fin del registro oficial de la Fase 3 — DentalCare ERP (Sprint 4).*

---

## ??? 5. CONTROL DE ACCESO BASADO EN ROLES (RBAC) � ROL ASISTENTE ODONTOL�GICO (ASISTENTEODONTOLOGO)

### 5.1 Contexto y Necesidad de Integraci�n
En la entrega y consolidaci�n final del Sprint 4 se identific� la incorporaci�n en base de datos del rol cl�nico **`ASISTENTEODONTOLOGO`** (responsable del subsistema de insumos, materiales, medicamentos y consumos). Para garantizar la integridad del sistema y una experiencia de usuario segura, se aplicaron las siguientes reglas de control de acceso (RBAC):

### 5.2 Matriz de Autorizaci�n por Rutas y Endpoints

| M�dulo / Ruta | Permiso para `ASISTENTEODONTOLOGO` | Comportamiento del Sistema |
| :--- | :---: | :--- |
| **/dashboard** | **Permitido** | Panel adaptado con accesos r�pidos tem�ticos a inventario y suministros cl�nicos. |
| **/inventario** | **Permitido** | Control total sobre cat�logo de materiales, ingreso de medicamentos, stock y confirmaci�n mensual de consumos. |
| **/consulta** | *Restringido* | Redirecci�n preventiva autom�tica a /dashboard v�a ProtectedRoute. |
| **/agenda** | *Restringido* | Protegido para Administrador, Recepcionista y Gerencia. |
| **/pacientes** | *Restringido* | Protegido para Administrador, Recepcionista y Gerencia. |
| **/usuarios** | *Restringido* | Protegido exclusivamente para Administrador y Gerencia. |
| **/revisar-accesos** | *Restringido* | Protegido exclusivamente para Administrador TI. |
| **/mantenimiento/respaldos** | *Restringido* | Protegido exclusivamente para Administrador TI (auditor�a y contingencia de BD). |

### 5.3 Implementaciones T�cnicas Aplicadas

1. **Normalizaci�n Can�nica de Roles (oles.constants.js):**
   - Creaci�n de la funci�n 
ormalizeRole(role) que transforma insensiblemente variaciones (ej: 'ASISTENTEODONTOLOGO', 'asistenteOdontologo', 'asistente dental', 'asistente') a la clave can�nica unificada 'asistenteodontologo'.
   - Implementaci�n de hasRoleAccess(userRole, allowedRoles) para evaluaciones robustas y libres de errores tipogr�ficos o de may�sculas/min�sculas.
   - Creaci�n de ormatRoleName(role) para representaci�n est�tica y profesional en la interfaz (mostrando *"Asistente Odontol�gico"* en badges de usuario, listas y selects).
2. **Blindaje en Guardi�n de Rutas (ProtectedRoute.jsx):**
   - Migraci�n de verificaci�n manual a hasRoleAccess(userRole, allowedRoles) asegurando que ning�n usuario con rol ASISTENTEODONTOLOGO sea bloqueado err�neamente en /inventario ni pueda vulnerar rutas administrativas o m�dicas.
3. **Filtrado Din�mico del Men� Lateral (Layout.jsx):**
   - El men� de navegaci�n eval�a reactivamente los permisos con hasRoleAccess. Al autenticarse como ASISTENTEODONTOLOGO, la barra lateral muestra �nicamente los accesos a **Inicio** e **Inventario**, ocultando opciones m�dicas y administrativas.
   - El avatar y el men� desplegable del encabezado muestran el rol formateado *"Asistente Odontol�gico"*.
4. **Dashboard Contextual Personalizado (DashboardPage.jsx):**
   - Si el usuario autenticado posee el rol ASISTENTEODONTOLOGO, el encabezado de bienvenida adapta su bot�n de acci�n principal a *"Gesti�n de Insumos"* (/inventario).
   - La cuadr�cula de **Acciones Frecuentes** se adapta din�micamente para desplegar los 4 flujos prioritarios del Sprint 4:
     1. *Cat�logo de Materiales (MT)*
     2. *Ingreso de Medicamentos (MD)*
     3. *Supervisi�n de Stock Cl�nico*
     4. *Confirmar Consumos Mensuales*
5. **Defensa de Esquema en Base de Datos (DatabaseSchemaMigrationRunner.java):**
   - Verificaci�n y aseguramiento del registro ASISTENTEODONTOLOGO dentro de la tabla ol de MySQL para consistencia relacional.

---
*Fin del registro oficial de consolidaci�n y RBAC � DentalCare ERP (Sprint 4).*


## 6. NORMALIZACIÓN ARQUITECTÓNICA Y UNIFICACIÓN DE MEDICAMENTOS

### 6.1 Problema Identificado
Se detectó una discrepancia en el modelo relacional entre el subsistema clínico (Sprint 2/3 - Consultas y Prescripciones) y el subsistema de inventario (Sprint 4 - Suministros Clínicos):
1. Existía una tabla medicamento (singular) histórica utilizada por detalle_prescripcion y movimiento_inventario.
2. En Sprint 4 se creó la tabla medicamentos (plural) con los nuevos atributos de inventario (codigo MDxx, presentacion, lote, echa_vencimiento, stock_minimo, stock_actual).
3. El Step 3 de consulta odontológica (StepPrescripcion.jsx) consumía el endpoint de la tabla singular, impidiendo que el odontólogo visualizara los medicamentos reales ingresados por el Asistente Dental en la base de datos.

### 6.2 Solución Aplicada (Unificación en Tabla Canónica 'medicamentos')
Se procedió a unificar y normalizar el modelo de datos en una sola tabla canónica física:
1. **Esquema de Base de Datos (BD_DentalCareWeb):**
   - La tabla medicamentos fue enriquecida con los atributos clínicos complementarios: componente_activo, concentracion, costo_medicamento, id_proveedor.
   - Se actualizaron las llaves foráneas de detalle_prescripcion y movimiento_inventario para referenciar formalmente a medicamentos(id).
   - Se eliminó la tabla redundante medicamento.
   - Se creó una vista SQL medicamento (CREATE OR REPLACE VIEW medicamento) como capa de compatibilidad hacia atrás para salvaguardar cualquier consulta o script de evaluación de la cátedra.
2. **Backend (Spring Boot):**
   - La entidad MedicamentoItem.java se consolidó como modelo único representativo de la tabla medicamentos.
   - Se actualizaron DetallePrescripcion.java y MovimientoInventario.java para asociarse a MedicamentoItem.
   - Se eliminaron las clases redundantes Medicamento.java y MedicamentoRepository.java.
   - En ConsultaService.java, los métodos listarMedicamentos(), crearPrescripcion() y mapearPrescripcion() fueron migrados a MedicamentoItemRepository, mapeando dinámicamente nombres, presentaciones y concentraciones.
   - En DatabaseSchemaMigrationRunner.java, se agregaron verificaciones automáticas idempotentes de esquema DDL para asegurar que cualquier despliegue mantenga la unificación intacta.
3. **Frontend:**
   - **Cero modificaciones visuales**: No se alteró ningún archivo ni estilo de la interfaz de usuario, garantizando la preservación total de los cambios visuales aplicados por el usuario.
   - El selector del Step 3 (StepPrescripcion.jsx) recibe de inmediato todos los medicamentos registrados en el catálogo de inventario con sus respectivas concentraciones y componentes activos.

---


## 7. DESCARGA AUTOMÁTICA DE MEDICAMENTOS RECETADOS AL FINALIZAR CONSULTA

### 7.1 Requerimiento y Flujo de Negocio
Al emitir una prescripción médica desde el Step 3 de la consulta odontológica (StepPrescripcion.jsx), cuando el doctor finaliza la consulta (transición de la cita a estado COMPLETADA):
1. El sistema debe descargar automáticamente del inventario físico (stock_actual en tabla medicamentos) los fármacos prescritos al paciente.
2. Debe registrarse el consumo clínico correspondiente en la tabla consumos con estado CONFIRMADO para que el Asistente Dental y Administración dispongan de trazabilidad en el módulo de suministros.

### 7.2 Implementación Técnica Aplicada
1. **Lógica de Negocio en Backend (CitaService.java):**
   - Se interceptó el cambio de estado en ctualizarEstado(Integer id, EstadoCita nuevoEstado). Cuando el estado cambia a COMPLETADA, se invoca descargarInventarioPorPrescripcion(Cita cita).
   - La operación recupera la prescripción y los detalles asociados a la cita.
   - Para cada medicamento prescrito:
     - Se descuenta 1 unidad de presentación del stock_actual en medicamentos (con salvaguarda Math.max(0, stock - 1)).
     - Se registra un registro en la entidad Consumo con 	ipoItem = TipoItem.MEDICAMENTO, estado = EstadoConsumo.CONFIRMADO, vinculando la cita, el odontólogo responsable y notas clínicas de la posología.
     - **Idempotencia:** Se verifica mediante ConsumoRepository.existsByCita_IdCitasAndTipoItemAndItemId para garantizar que un medicamento no sea descontado dos veces si se consulta o recarga la cita.
2. **Repositorio de Consumos (ConsumoRepository.java):**
   - Se añadió el método derivado existsByCita_IdCitasAndTipoItemAndItemId(Integer idCitas, TipoItem tipoItem, Integer itemId).
3. **Flujo de Usuario en Frontend (ActiveConsultationPage.jsx):**
   - Se sincronizó el evento onFinalizar para que, si el doctor tiene medicamentos añadidos al borrador de la receta y pulsa *Finalizar Consulta y Ver Resumen*, primero guarde automáticamente la receta en la base de datos y luego proceda con la finalización y descarga de inventario sin requerir pasos manuales adicionales ni alterar la interfaz visual.

---
