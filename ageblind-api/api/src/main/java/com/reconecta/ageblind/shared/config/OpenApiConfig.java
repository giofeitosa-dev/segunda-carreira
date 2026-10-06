package com.reconecta.ageblind.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI ageBlindOpenApi() {
		return new OpenAPI().info(new Info()
				.title("AgeBlind API")
				.description("""
						Segunda Carreira — API anti-etarismo para empregabilidade (ODS 10 + 5).
						Detecção e geração de currículos age-blind, auditoria de vagas,
						métricas de funil com adverse impact e recomendação de vagas
						por competências — sem usar idade.
						""")
				.version("1.0.0")
				.contact(new Contact().name("Equipe Reconecta (ReCode)")));
	}
}
