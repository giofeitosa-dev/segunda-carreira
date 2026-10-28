package com.reconecta.ageblind.shared.config;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * HTTP Basic stateless. Protege só os endpoints do painel RH (ROLE_RH);
 * o restante da API fica aberto (MVP da sessão 06). Credenciais demo via
 * env RH_USER/RH_PASSWORD (padrão rh / rh-demo).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.headers(h -> h.frameOptions(f -> f.sameOrigin()))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.POST, "/api/v1/vagas/auditoria-linguagem").hasRole("RH")
						.requestMatchers("/api/v1/funil/**").hasRole("RH")
						.requestMatchers("/actuator/health", "/h2-console/**", "/swagger-ui/**",
								"/swagger-ui.html", "/v3/api-docs/**", "/error")
						.permitAll()
						.anyRequest().permitAll())
				.httpBasic(b -> b.authenticationEntryPoint(SecurityConfig::problem401))
				.exceptionHandling(e -> e.accessDeniedHandler(SecurityConfig::problem403));
		return http.build();
	}

	@Bean
	InMemoryUserDetailsManager userDetailsService(
			@Value("${app.rh.usuario:rh}") String usuario,
			@Value("${app.rh.senha:rh-demo}") String senha) {
		return new InMemoryUserDetailsManager(User.withUsername(usuario)
				.password("{noop}" + senha)
				.roles("RH")
				.build());
	}

	private static void problem401(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException ex) throws IOException {
		response.setHeader("WWW-Authenticate", "Basic realm=\"Segunda Carreira\"");
		escreverProblem(response, HttpServletResponse.SC_UNAUTHORIZED, "Não autenticado",
				"Informe as credenciais de acesso (HTTP Basic)");
	}

	private static void problem403(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException ex) throws IOException {
		escreverProblem(response, HttpServletResponse.SC_FORBIDDEN, "Acesso negado",
				"Perfil sem permissão para este recurso");
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
