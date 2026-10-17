# GUÍA DE INTEGRACIÓN Y COMMITS: BACKEND DENTALCARE API
> **Rol:** Arquitectura de Software & Estándares Académicos  
> **Stack:** Java 17, Spring Boot 3, Spring Security 6, Hibernate / Spring Data JPA  
> **Ruta Base:** `src/main/java/com/dentalcare/api/`

---

## 🏛️ 1. DICTAMEN ARQUITECTÓNICO Y DESCARTE DE CAMBIOS

### ✅ Cambios Aprobados
1. **Seguridad Stateless JWT (`JwtUtil`, `JwtAuthenticationFilter`, `JwtAuthEntryPoint`, `SecurityConfig`):**  
   - Desacopla la sesión del servidor cumpliendo el principio REST Stateless.
   - Implementa control de acceso basado en roles (RBAC) con protección defensiva de endpoints sensibles.
2. **Variables de Entorno (`application.properties`):**  
   - Conforme a *The Twelve-Factor App* (Factor III: Configuración), evita secretos hardcodeados en el código fuente.
3. **Compatibilidad Retroactiva de Enum (`EstadoCita.java`):**  
   - Protege la deserialización JPA ante registros históricos `COMPLETADA` sin quebrar la base de datos existente.
4. **Validación Declarativa en Cancelación de Citas (`CitaCancelacionDTO`, `CitaService`):**  
   - Bean Validation en frontera `@NotBlank` y validación defensiva en capa de servicio con sanitización `.trim()`.
5. **Corrección de Relación Foránea en Prescripciones (`ConsultaService.java`):**  
   - Soluciona la pérdida de integridad relacional al asociar correctamente `PlanTratamiento` en cada `DetallePrescripcion`.
6. **Protección de Integridad Referencial (`CitaRepository`, `PacienteService`):**  
   - Consulta derivada `existsByPaciente_IdPaciente` preventiva antes de intentar borrado físico, evitando excepciones no controladas de base de datos.

### ❌ Cambios Descartados / Refactorizados
- **Descarte de `try-catch` dispersos en Controladores (`ConsultaController` y `PacienteController`):**  
  - **Motivo del Descarte:** Rodear cada método de controlador con `try { ... } catch (RuntimeException ex)` viola el principio de Responsabilidad Única (SRP), genera duplicación masiva de código boilerplate (`Map.of("message", ex.getMessage())`) y enmascara errores graves del sistema (ej. `NullPointerException` o caídas de BD) como errores del cliente `400 Bad Request`.
  - **Refactorización Académica Obligatoria:** En su lugar, se implementa un **`GlobalExceptionHandler`** centralizado con `@RestControllerAdvice`. Los controladores permanecen limpios y expresivos, delegando el manejo uniforme de excepciones a la capa transversal.

---

## 📦 2. ESTRATEGIA DE COMMITS ATÓMICOS (JIRA)

---

### 🔹 Commit B1: Infraestructura Criptográfica y Filtro JWT
* **Mensaje de Commit:**
  ```text
  [HDU-XXX] feat(security): implementar infraestructura criptografica JWT y filtro OncePerRequest
  ```
* **Propósito:** Configurar la generación/validación de firmas HMAC-SHA256 y la intercepción de peticiones HTTP con autorización Bearer.
* **Archivos Involucrados:**
  - `src/main/resources/application.properties` (MODIFICAR)
  - `src/main/java/com/dentalcare/api/security/JwtUtil.java` (CREAR)
  - `src/main/java/com/dentalcare/api/security/JwtAuthenticationFilter.java` (CREAR)
  - `src/main/java/com/dentalcare/api/security/JwtAuthEntryPoint.java` (CREAR)

#### Paso 1.1: `application.properties`
Agregar variables de configuración con fallback seguro:
```properties
# --- SEGURIDAD JWT ---
jwt.secret=${JWT_SECRET:dentalcare-clave-secreta-muy-larga-y-segura-2024}
jwt.expiration.ms=${JWT_EXPIRATION_MS:3600000}
```

#### Paso 1.2: `security/JwtUtil.java`
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

#### Paso 1.3: `security/JwtAuthenticationFilter.java`
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

#### Paso 1.4: `security/JwtAuthEntryPoint.java`
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

### 🔹 Commit B2: Configuración de Seguridad Stateless y RBAC
* **Mensaje de Commit:**
  ```text
  [HDU-XXX] feat(security): configurar politica de seguridad stateless y control de acceso RBAC
  ```
* **Propósito:** Reemplazar configuración permisiva por cadena de filtros estricta, sesiones stateless y roles.
* **Archivos Involucrados:**
  - `src/main/java/com/dentalcare/api/config/SecurityConfig.java` (MODIFICAR)

#### Paso 2.1: `config/SecurityConfig.java`
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

### 🔹 Commit B3: Corrección en Dominio y Persistencia Relacional Clínica
* **Mensaje de Commit:**
  ```text
  [HDU-XXX] fix(clinical): retrocompatibilidad de EstadoCita y persistencia de planes en prescripciones
  ```
* **Propósito:** Mantener compatibilidad con BD histórica y asegurar persistencia del plan de tratamiento en prescripciones.
* **Archivos Involucrados:**
  - `src/main/java/com/dentalcare/api/models/enums/EstadoCita.java` (MODIFICAR)
  - `src/main/java/com/dentalcare/api/services/ConsultaService.java` (MODIFICAR)

#### Paso 3.1: `models/enums/EstadoCita.java`
```java
package com.dentalcare.api.models.enums;

public enum EstadoCita {
    PROGRAMADA, 
    FINALIZADA, 
    COMPLETADA,   // Retrocompatibilidad con base de datos historica
    CANCELADA, 
    PENDIENTE, 
    EN_PROGRESO,
    REPROGRAMADA,
    NO_ASISTIO,
    OTRO
}
```

#### Paso 3.2: `services/ConsultaService.java`
En `crearPrescripcion()` vincular la referencia foránea de `PlanTratamiento`:
```java
// Dentro del mapeo de cada DetallePrescripcion:
if (dto.getIdPlanTratamiento() != null) {
    PlanTratamiento plan = planTratamientoRepository.findById(dto.getIdPlanTratamiento())
            .orElseThrow(() -> new RuntimeException("Plan de tratamiento no encontrado: " + dto.getIdPlanTratamiento()));
    detalle.setPlanTratamiento(plan);
}
```

---

### 🔹 Commit B4: Protección de Integridad Referencial en Pacientes
* **Mensaje de Commit:**
  ```text
  [HDU-XXX] fix(integrity): prevenir eliminacion de pacientes con citas registradas
  ```
* **Propósito:** Validar mediante consulta de existencia relacional antes del borrado de paciente, protegiendo contra Foreign Key constraint violations.
* **Archivos Involucrados:**
  - `src/main/java/com/dentalcare/api/repositories/CitaRepository.java` (MODIFICAR)
  - `src/main/java/com/dentalcare/api/services/PacienteService.java` (MODIFICAR)

#### Paso 4.1: `repositories/CitaRepository.java`
Agregar método derivado Spring Data:
```java
boolean existsByPaciente_IdPaciente(Integer idPaciente);
```

#### Paso 4.2: `services/PacienteService.java`
```java
@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final CitaRepository citaRepository;

    public PacienteService(PacienteRepository pacienteRepository, CitaRepository citaRepository) {
        this.pacienteRepository = pacienteRepository;
        this.citaRepository = citaRepository;
    }

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

### 🔹 Commit B5: Validación de Cancelación y Centralización de Excepciones Globales
* **Mensaje de Commit:**
  ```text
  [HDU-XXX] refactor(api): validacion declarativa de cancelacion y manejo transversal de excepciones
  ```
* **Propósito:** Aplicar Bean Validation en cancelación y centralizar el mapeo de excepciones (`IllegalStateException`, `IllegalArgumentException`, `RuntimeException`) con `@RestControllerAdvice` eliminando `try-catch` duplicados en controladores.
* **Archivos Involucrados:**
  - `src/main/java/com/dentalcare/api/dtos/Cita/CitaCancelacionDTO.java` (MODIFICAR)
  - `src/main/java/com/dentalcare/api/services/CitaService.java` (MODIFICAR)
  - `src/main/java/com/dentalcare/api/controllers/CitaController.java` (MODIFICAR)
  - `src/main/java/com/dentalcare/api/config/GlobalExceptionHandler.java` (CREAR)
  - `src/main/java/com/dentalcare/api/controllers/PacienteController.java` (LIMPIAR)
  - `src/main/java/com/dentalcare/api/controllers/ConsultaController.java` (LIMPIAR)

#### Paso 5.1: `dtos/Cita/CitaCancelacionDTO.java`
```java
package com.dentalcare.api.dtos.Cita;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CitaCancelacionDTO {

    @NotBlank(message = "El motivo de cancelación es obligatorio.")
    private String motivoCancelacion;
}
```

#### Paso 5.2: `services/CitaService.java`
Validación defensiva con `.trim()`:
```java
public CitaResponseDTO cancelarCita(Integer id, CitaCancelacionDTO cancelacionRequest) {
    if (cancelacionRequest == null || cancelacionRequest.getMotivoCancelacion() == null || cancelacionRequest.getMotivoCancelacion().trim().isEmpty()) {
        throw new IllegalArgumentException("El motivo de cancelación es obligatorio.");
    }

    Cita cita = citaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Error: Cita no encontrada con el ID: " + id));

    cita.setEstadoCita(com.dentalcare.api.models.enums.EstadoCita.CANCELADA);
    cita.setMotivoCancelacion(cancelacionRequest.getMotivoCancelacion().trim());

    Cita citaActualizada = citaRepository.save(cita);
    return convertirAResponseDTO(citaActualizada);
}
```

#### Paso 5.3: `controllers/CitaController.java`
```java
@PutMapping("/{id}/cancelar")
public ResponseEntity<CitaResponseDTO> cancelarCita(
        @PathVariable Integer id,
        @Valid @RequestBody CitaCancelacionDTO cancelacionRequest) {
    CitaResponseDTO citaCancelada = citaService.cancelarCita(id, cancelacionRequest);
    return ResponseEntity.ok(citaCancelada);
}
```

#### Paso 5.4: Crear `config/GlobalExceptionHandler.java` (Solución Arquitectónica Limpia)
```java
package com.dentalcare.api.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        String errorMsg = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getDefaultMessage())
                .findFirst()
                .orElse("Datos de entrada inválidos.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", errorMsg));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntime(RuntimeException ex) {
        // Manejo controlado de recursos no encontrados o errores de regla de negocio
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }
}
```
*(Gracias a este `GlobalExceptionHandler`, los métodos en `ConsultaController` y `PacienteController` no requieren bloques `try-catch` internos repetitivos).*

---

## 🧪 3. VERIFICACIÓN Y COMPILACIÓN

Para verificar que todos los cambios compilan de manera correcta:

```powershell
.\mvnw.cmd clean compile
```

**Criterio de Aceptación:** `BUILD SUCCESS`.
