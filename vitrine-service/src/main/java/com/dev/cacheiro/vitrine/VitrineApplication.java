package com.dev.cacheiro.vitrine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class VitrineApplication {

	public static void main(String[] args) {
		SpringApplication.run(VitrineApplication.class, args);
	}

}
