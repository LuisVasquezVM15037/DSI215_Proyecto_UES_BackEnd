package com.dentalcare.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Propósito: Clase principal y punto de entrada para el arranque del framework Spring Boot.
 * Inicializa el contexto de la aplicación, configura el contenedor IoC (Inversión de Control)
 * y despliega el servidor web embebido (Tomcat).
 * 
 * Ubicación y Rol: Capa raíz / Bootstrap de la arquitectura backend.
 * 
 * Trazabilidad (Referencias):
 * - Invocado por: La Máquina Virtual de Java (JVM) al ejecutar la aplicación.
 * - Consume / Desencadena: @SpringBootApplication desencadena @Configuration,
 *   @EnableAutoConfiguration y @ComponentScan en todo el paquete com.dentalcare.api.
 */
@SpringBootApplication
public class ApiApplication {

	/**
	 * Punto de entrada estándar para la ejecución de la aplicación.
	 * 
	 * @param args Argumentos pasados por línea de comandos durante el arranque del proceso.
	 */
	public static void main(String[] args) {
		SpringApplication.run(ApiApplication.class, args);
	}

}

