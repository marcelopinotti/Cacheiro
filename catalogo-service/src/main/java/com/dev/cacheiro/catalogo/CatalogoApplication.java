package com.dev.cacheiro.catalogo;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(
		title = "Cacheiro — catalogo-service",
		version = "0.0.1",
		description = "Fonte da verdade dos produtos (Postgres). Publica invalidação de cache no Redis a cada escrita."))
public class CatalogoApplication {

	public static void main(String[] args) {
		SpringApplication.run(CatalogoApplication.class, args);
	}

}
