package com.reconecta.ageblind.shared.config;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Matriz de acesso JWT (spec US2): leitura pública de vagas/trilhas/mentorias
 * e /auth/** abertos; /funil/** e auditoria de linguagem só ROLE_RH; todo o
 * resto exige token — posse de dados pessoais é checada por PosseChecker.
 * HTTP Basic e usuário in-memory removidos (FR-007); erros 401/403 em
 * problem+json com WWW-Authenticate Bearer (FR-008).
 * Rate limit em /api/v1/auth/** via RateLimitFilter (US4.5) e security
 * headers padrão (US4.3).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(
			HttpSecurity http,
			JwtAuthenticationConverter jwtAuthenticationConverter,
			RateLimitFilter rateLimitFilter,
			CorsConfigurationSource corsConfigurationSource) throws Exception {
		http
				.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.cors(c -> c.configurationSource(corsConfigurationSource))
				.headers(h -> h
						.frameOptions(f -> f.deny())
						.contentTypeOptions(c -> c.disable())
						.httpStrictTransportSecurity(s -> s.includeSubDomains(true).maxAgeInSeconds(31536000)))
				.addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/api/v1/auth/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/v1/vagas/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/v1/trilhas/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/v1/mentorias", "/api/v1/mentorias/**")
						.permitAll()
						.requestMatchers("/actuator/health", "/h2-console/**", "/swagger-ui/**",
								"/swagger-ui.html", "/v3/api-docs/**", "/error")
						.permitAll()
						.requestMatchers(HttpMethod.POST, "/api/v1/vagas/auditoria-linguagem")
						.hasRole("RH")
						.requestMatchers("/api/v1/funil/**").hasRole("RH")
						.anyRequest().authenticated())
				.oauth2ResourceServer(o -> o
						.authenticationEntryPoint(SecurityConfig::problem401)
						.accessDeniedHandler(SecurityConfig::problem403)
						.jwt(j -> j.jwtAuthenticationConverter(jwtAuthenticationConverter)))
				.exceptionHandling(e -> e
						.authenticationEntryPoint(SecurityConfig::problem401)
						.accessDeniedHandler(SecurityConfig::problem403));
		return http.build();
	}

	private static void problem401(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException ex) throws IOException {
		response.setHeader("WWW-Authenticate",
				"Bearer realm=\"Segunda Carreira\", error=\"invalid_token\"");
		escreverProblem(response, HttpServletResponse.SC_UNAUTHORIZED, "Não autenticado",
				"Token ausente, expirado ou inválido");
	}

	private static void problem403(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException ex) throws IOException {
		escreverProblem(response, HttpServletResponse.SC_FORBIDDEN, "Acesso negado",
				"Sem permissão para este recurso");
	}

	private static void escreverProblem(HttpServletResponse response, int status, String title,
			String detail) throws IOException {
		response.setStatus(status);
		response.setCharacterEncoding(java.nio.charset.StandardCharsets.UTF_8.name());
		response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
		response.getWriter().write("{\"type\":\"about:blank\",\"title\":\"" + title
				+ "\",\"status\":" + status + ",\"detail\":\"" + detail + "\"}");
	}
}
