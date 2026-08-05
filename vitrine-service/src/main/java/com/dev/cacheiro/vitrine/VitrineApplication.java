package com.dev.cacheiro.vitrine;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
@OpenAPIDefinition(info = @Info(
		title = "Cacheiro — vitrine-service",
		version = "0.0.1",
		description = "Leitura cacheada do catálogo em Redis, com lock anti-stampede e rate limit de 100 req/min por IP."))
public class VitrineApplication {

	public static void main(String[] args) {
		SpringApplication.run(VitrineApplication.class, args);
	}

}
