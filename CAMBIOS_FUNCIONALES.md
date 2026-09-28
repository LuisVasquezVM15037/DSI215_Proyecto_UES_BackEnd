# Guía de Cambios Funcionales: Seguridad JWT, Hardening y Bugfixes — Backend DentalCare

> **Propósito:** Documento técnico para que otro agente pueda replicar con precisión todos los cambios netamente funcionales realizados en el código del backend DentalCare (Spring Boot 3, Java 17).
>
> **Alcance:** Exclusivamente código funcional (lógica de negocio, configuración de seguridad, validaciones de entrada, manejo de excepciones y control de integridad referencial). No incluye cambios de documentación ni JavaDoc.
>
> **Ruta raíz del proyecto:** `src/main/java/com/dentalcare/api/`

---

## 1. [NUEVO] `security/JwtUtil.java`

**Acción:** Crear la clase utilitaria para la generación, firmado HMAC-SHA256, extracción de reclamos (claims) y validación de tokens JWT.

```java
package com.dentalcare.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration.ms}")
    private long expirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String username, String rol) {
        return Jwts.builder()
                .subject(username)
                .claim("rol", rol)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractRole(String token) {
        try {
            return parseClaims(token).get("rol", String.class);
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
```

---

## 2. [NUEVO] `security/JwtAuthenticationFilter.java`

**Acción:** Crear el filtro HTTP `OncePerRequestFilter` que intercepta las peticiones entrantes, extrae el token del encabezado `Authorization: Bearer <token>`, valida la firma/expiración y establece la autenticación en `SecurityContextHolder` con las autoridades `ROLE_<ROL>` y `<ROL>`.

```java
package com.dentalcare.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);

        try {
            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.extractUsername(token);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    String rol = jwtUtil.extractRole(token);

                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    if (rol != null && !rol.trim().isEmpty()) {
                        String cleanRol = rol.trim().toUpperCase();
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + cleanRol));
                        authorities.add(new SimpleGrantedAuthority(cleanRol));
                    }

                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);

                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
```

---

## 3. [NUEVO] `security/JwtAuthEntryPoint.java`

**Acción:** Crear el manejador de fallos de autenticación que implementa `AuthenticationEntryPoint`, emitiendo una respuesta HTTP 401 (Unauthorized) en formato JSON estandarizado en lugar de redirecciones HTML.

```java
package com.dentalcare.api.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("{\"message\": \"No autorizado. Se requiere un token válido para acceder a este recurso.\"}");
    }
}
```

---

## 4. [MODIFICAR] `config/SecurityConfig.java`

**Acción:** Reemplazar la configuración permisiva original (`anyRequest().permitAll()` sin filtro) por una política de seguridad estricta:
- Inyección de dependencias: `JwtAuthenticationFilter`, `JwtAuthEntryPoint`, `CorsConfigurationSource`.
- Desactivación de CSRF (`csrf.disable()`) por tratarse de una API REST stateless.
- Política de sesiones: `SessionCreationPolicy.STATELESS`.
- Configuración de autorización:
  - Públicos: `HttpMethod.OPTIONS /**` (preflight CORS), `/api/auth/**` (login), `/error`.
  - Roles protegidos: `/api/usuarios/**` para `ADMIN` y `GERENTE`; `/api/registros-acceso/**` para `ADMIN`.
  - Resto de endpoints: `anyRequest().authenticated()`.
- Registro de `JwtAuthenticationFilter` antes de `UsernamePasswordAuthenticationFilter`.

```java
package com.dentalcare.api.config;

import com.dentalcare.api.security.JwtAuthEntryPoint;
import com.dentalcare.api.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final JwtAuthEntryPoint jwtAuthEntryPoint;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter,
                          JwtAuthEntryPoint jwtAuthEntryPoint,
                          CorsConfigurationSource corsConfigurationSource) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.jwtAuthEntryPoint = jwtAuthEntryPoint;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(csrf -> csrf.disable())
            .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthEntryPoint))
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/api/usuarios/**").hasAnyRole("ADMIN", "GERENTE")
                .requestMatchers("/api/registros-acceso/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

---

## 5. [MODIFICAR] `src/main/resources/application.properties`

**Acción:** Reemplazar el secreto JWT hardcodeado por soporte a variables de entorno con fallback para ejecución local:

```properties
# --- JWT ---
# Esta clave debe ser larga (minimo 32 caracteres),
# aleatoria y nunca expuesta en repositorios publicos.
jwt.secret=${JWT_SECRET:dentalcare-clave-secreta-muy-larga-y-segura-2024}
# Expiracion en milisegundos: 3600000 = 1 hora
jwt.expiration.ms=${JWT_EXPIRATION_MS:3600000}
```

---

## 6. [MODIFICAR] `models/enums/EstadoCita.java`

**Acción:** Agregar el valor `COMPLETADA` para garantizar retrocompatibilidad con registros existentes en la base de datos previa a la migración a `FINALIZADA`.

```java
public enum EstadoCita {
    PROGRAMADA, 
    FINALIZADA, 
    COMPLETADA,   // <-- Retrocompatibilidad con BD histórica
    CANCELADA, 
    PENDIENTE, 
    EN_PROGRESO,
    REPROGRAMADA,
    NO_ASISTIO,
    OTRO
}
```

---

## 7. [MODIFICAR] `dtos/Cita/CitaCancelacionDTO.java`

**Acción:** Añadir validación Bean Validation `@NotBlank` para asegurar que ninguna solicitud de cancelación viaje sin justificación.

```java
package com.dentalcare.api.dtos.Cita;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CitaCancelacionDTO {

    @NotBlank(message = "El motivo de cancelación es obligatorio")
    private String motivoCancelacion;
}
```

---

## 8. [MODIFICAR] `controllers/CitaController.java`

**Acción:** Activar la validación Bean Validation en el endpoint de cancelación agregando la anotación `@Valid`.

```java
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<CitaResponseDTO> cancelarCita(
            @PathVariable Integer id,
            @Valid @RequestBody CitaCancelacionDTO cancelacionRequest) {
        CitaResponseDTO citaCancelada = citaService.cancelarCita(id, cancelacionRequest);
        return ResponseEntity.ok(citaCancelada);
    }
```

---

## 9. [MODIFICAR] `services/CitaService.java`

**Acción:** En el método `cancelarCita()`, añadir validación defensiva en la capa de servicio y sanitización con `.trim()` al persistir el motivo:

```java
    public CitaResponseDTO cancelarCita(Integer id, CitaCancelacionDTO cancelacionRequest) {
        if (cancelacionRequest == null || cancelacionRequest.getMotivoCancelacion() == null || cancelacionRequest.getMotivoCancelacion().trim().isEmpty()) {
            throw new IllegalArgumentException("El motivo de cancelación es obligatorio.");
        }

        // Buscar la cita existente
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Cita no encontrada con el ID: " + id));

        // Aplicar los cambios de estado y motivo
        cita.setEstadoCita(com.dentalcare.api.models.enums.EstadoCita.CANCELADA);
        cita.setMotivoCancelacion(cancelacionRequest.getMotivoCancelacion().trim());

        // Guardar los cambios en la BD
        Cita citaActualizada = citaRepository.save(cita);
        return convertirAResponseDTO(citaActualizada);
    }
```

---

## 10. [MODIFICAR] `controllers/ConsultaController.java`

**Acción:** Estandarizar el manejo de errores en los endpoints de consulta capturando `RuntimeException`, `IllegalArgumentException` e `IllegalStateException`, devolviendo respuestas estructuradas `Map.of("message", ex.getMessage())` con códigos HTTP 400 (Bad Request) o 404 (Not Found) según corresponda:

```java
    @PostMapping("/tratamientos")
    public ResponseEntity<?> crearTratamiento(@Valid @RequestBody TratamientoRequestDTO request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(consultaService.crearTratamiento(request));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", ex.getMessage()));
        }
    }

    @PostMapping("/evaluacion")
    public ResponseEntity<?> guardarEvaluacion(@Valid @RequestBody EvaluacionClinicaRequestDTO request) {
        try {
            return ResponseEntity.ok(consultaService.guardarEvaluacion(request));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
        }
    }

    @PostMapping("/hallazgo")
    public ResponseEntity<?> registrarHallazgo(@Valid @RequestBody PlanTratamientoRequestDTO request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(consultaService.registrarHallazgo(request));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
        }
    }

    @DeleteMapping("/hallazgo/{id}")
    public ResponseEntity<?> eliminarHallazgo(@PathVariable Integer id) {
        try {
            consultaService.eliminarHallazgo(id);
            return ResponseEntity.ok(Map.of("message", "Hallazgo eliminado."));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
        }
    }

    @PatchMapping("/hallazgo/{id}/estado")
    public ResponseEntity<?> actualizarEstadoHallazgo(
            @PathVariable Integer id,
            @RequestBody Map<String, String> payload) {
        try {
            String nuevoEstado = (payload != null) ? payload.get("estado") : null;
            PlanTratamientoResponseDTO resultado = consultaService.actualizarEstadoHallazgo(id, nuevoEstado);
            return ResponseEntity.ok(resultado);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
        }
    }

    @PostMapping("/prescripcion")
    public ResponseEntity<?> crearPrescripcion(@Valid @RequestBody PrescripcionRequestDTO request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(consultaService.crearPrescripcion(request));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
        }
    }
```

---

## 11. [MODIFICAR] `services/ConsultaService.java`

**Acción:** Corregir bug en `crearPrescripcion()` donde se omitía persistir la relación con `PlanTratamiento` recibida en el DTO. Dentro del mapeo de cada `DetallePrescripcion`, agregar la resolución y vinculación del plan:

```java
    // Dentro de crearPrescripcion():
    List<DetallePrescripcion> detalles = request.getDetalles().stream().map(dto -> {
        Medicamento medicamento = medicamentoRepository.findById(dto.getIdMedicamento())
                .orElseThrow(() -> new RuntimeException("Medicamento no encontrado: " + dto.getIdMedicamento()));

        DetallePrescripcion detalle = new DetallePrescripcion();
        detalle.setPrescripcion(prescripcionGuardada);
        detalle.setMedicamento(medicamento);
        detalle.setDosisPrescripcion(dto.getDosis());
        detalle.setFrecuenciaPrescripcion(dto.getFrecuencia());
        detalle.setDuracionPrescripcion(dto.getDuracion());
        detalle.setIndicacionesPrescripcion(dto.getIndicaciones());

        // <-- CORRECCION: Persistir la relación foránea si se especifica un plan de tratamiento
        if (dto.getIdPlanTratamiento() != null) {
            PlanTratamiento plan = planTratamientoRepository.findById(dto.getIdPlanTratamiento())
                    .orElseThrow(() -> new RuntimeException("Plan de tratamiento no encontrado: " + dto.getIdPlanTratamiento()));
            detalle.setPlanTratamiento(plan);
        }

        return detalle;
    }).collect(Collectors.toList());
```

---

## 12. [MODIFICAR] `repositories/CitaRepository.java`

**Acción:** Declarar método de consulta derivado para verificar existencia de citas de un paciente por clave foránea:

```java
boolean existsByPaciente_IdPaciente(Integer idPaciente);
```

---

## 13. [MODIFICAR] `services/PacienteService.java`

**Acción:** Proteger la eliminación de pacientes contra violaciones de integridad referencial (Foreign Key constraint):
1. Inyectar `CitaRepository` en el constructor.
2. En `eliminarPaciente()`, verificar antes del borrado si el paciente cuenta con citas agendadas o históricas, lanzando `IllegalStateException`.

```java
@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final CitaRepository citaRepository;

    public PacienteService(PacienteRepository pacienteRepository, CitaRepository citaRepository) {
        this.pacienteRepository = pacienteRepository;
        this.citaRepository = citaRepository;
    }

    // ...

    public void eliminarPaciente(Integer id) {
        if (!pacienteRepository.existsById(id)) {
            throw new RuntimeException("Paciente con id " + id + " no encontrado.");
        }
        if (citaRepository.existsByPaciente_IdPaciente(id)) {
            throw new IllegalStateException("No se puede eliminar el paciente porque tiene citas registradas en su historial.");
        }
        pacienteRepository.deleteById(id);
    }
}
```

---

## 14. [MODIFICAR] `controllers/PacienteController.java`

**Acción:** Capturar `IllegalStateException` en el método `eliminarPaciente()` para responder con código HTTP 400 (Bad Request) y mensaje descriptivo en lugar de un error 500 no controlado:

```java
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarPaciente(@PathVariable Integer id) {
        try {
            pacienteService.eliminarPaciente(id);
            return ResponseEntity.ok(Map.of("message", "Paciente eliminado correctamente."));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", ex.getMessage()));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", ex.getMessage()));
        }
    }
```

---

## Verificación de Compilación

Tras aplicar estos cambios, ejecutar en la raíz del proyecto:

```powershell
.\mvnw.cmd compile
```

**Resultado esperado:** `BUILD SUCCESS`.

---

## Matriz Resumen de Archivos Modificados

| # | Archivo | Acción | Descripción Funcional |
|---|---|---|---|
| 1 | `security/JwtUtil.java` | **NUEVO** | Componente criptográfico para generar, firmar, validar tokens JWT y extraer roles. |
| 2 | `security/JwtAuthenticationFilter.java` | **NUEVO** | Filtro `OncePerRequestFilter` para autenticación con cabecera `Authorization: Bearer`. |
| 3 | `security/JwtAuthEntryPoint.java` | **NUEVO** | Manejador HTTP 401 JSON para rechazo de accesos no autorizados. |
| 4 | `config/SecurityConfig.java` | **MODIFICAR** | Restricción RBAC, sesiones stateless, registro de filtro JWT y protección de endpoints. |
| 5 | `application.properties` | **MODIFICAR** | Variables de entorno `JWT_SECRET` y `JWT_EXPIRATION_MS` con fallback local. |
| 6 | `models/enums/EstadoCita.java` | **MODIFICAR** | Constante `COMPLETADA` para compatibilidad retroactiva de base de datos. |
| 7 | `dtos/Cita/CitaCancelacionDTO.java` | **MODIFICAR** | Validación `@NotBlank` obligatoria para el motivo de cancelación. |
| 8 | `controllers/CitaController.java` | **MODIFICAR** | Validación `@Valid` en endpoint PUT `/api/citas/{id}/cancelar`. |
| 9 | `services/CitaService.java` | **MODIFICAR** | Validación defensiva del motivo de cancelación y sanitización `.trim()`. |
| 10 | `controllers/ConsultaController.java` | **MODIFICAR** | Mapeo estructurado de excepciones a HTTP 400 y 404 en endpoints clínicos. |
| 11 | `services/ConsultaService.java` | **MODIFICAR** | Persistencia de relación foránea con `PlanTratamiento` en líneas de prescripción. |
| 12 | `repositories/CitaRepository.java` | **MODIFICAR** | Método `existsByPaciente_IdPaciente` para chequeo de integridad relacional. |
| 13 | `services/PacienteService.java` | **MODIFICAR** | Chequeo preventivo de citas antes de eliminar paciente (`IllegalStateException`). |
| 14 | `controllers/PacienteController.java` | **MODIFICAR** | Captura de `IllegalStateException` y retorno semántico de HTTP 400 Bad Request. |
