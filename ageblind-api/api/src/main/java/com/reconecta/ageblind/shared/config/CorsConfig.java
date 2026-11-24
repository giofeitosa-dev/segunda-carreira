package com.reconecta.ageblind.shared.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * CORS allowlist explícita (R9 / US4.4): apenas origens de
 * app.cors.allowed-origins (env CORS_ALLOWED_ORIGINS, default
 * http://localhost:3000,http://localhost:5173) passam no preflight.
 */
@Configuration
public class CorsConfig {

	@Bean
	CorsConfigurationSource corsConfigurationSource(Environment environment) {
		String allowedOrigins = environment.getProperty(
				"app.cors.allowed-origins",
				"http://localhost:3000,http://localhost:5173");
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(List.of(allowedOrigins.split(",")));
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
		config.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/api/v1/**", config);
		return source;
	}
}
