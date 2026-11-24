package com.reconecta.ageblind.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuração do Spring MVC para o ciclo 002.
 * Registra o AuditoriaInterceptor para log de auditoria de segurança.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

	private final AuditoriaInterceptor auditoriaInterceptor;

	public WebConfig(AuditoriaInterceptor auditoriaInterceptor) {
		this.auditoriaInterceptor = auditoriaInterceptor;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(auditoriaInterceptor)
				.addPathPatterns("/api/v1/**");
	}
}
