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

/**
 * Configuracion central de Spring Security con autenticacion basada en JWT.
 */
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

    /**
     * Define el algoritmo de hashing de passwords.
     * BCrypt con factor de costo 10 (balance entre seguridad y rendimiento).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Cadena de filtros de seguridad HTTP.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Aplica la configuracion CORS global (CorsConfig)
            .cors(cors -> cors.configurationSource(corsConfigurationSource))

            // API Stateless con JWT, deshabilitamos CSRF
            .csrf(csrf -> csrf.disable())

            // Manejo de errores de autenticacion (401 JSON)
            .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthEntryPoint))

            // Sin sesion HTTP (stateless)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .authorizeHttpRequests(auth -> auth
                // Peticiones preflight CORS siempre permitidas
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Endpoints de autenticacion publicos
                .requestMatchers("/api/auth/**").permitAll()

                // Endpoint de errores de Spring Boot
                .requestMatchers("/error").permitAll()

                // Restricciones de roles administrativas
                .requestMatchers("/api/usuarios/**").hasAnyRole("ADMIN", "GERENTE")
                .requestMatchers("/api/registros-acceso/**").hasRole("ADMIN")

                // El resto de endpoints requiere autenticacion con JWT
                .anyRequest().authenticated()
            )

            // Registrar el filtro JWT antes del filtro estandar de autenticacion
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}