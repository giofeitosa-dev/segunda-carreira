package com.reconecta.ageblind.shared.config;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Rate limit em /api/v1/auth/** (R2 / US4.5): janela fixa em memória,
 * limite e janela configuráveis via env. Responde 429 problem+json quando
 * excedido. Sem Redis — single instance é suficiente para o ciclo (Constituição V).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RateLimitFilter extends OncePerRequestFilter {

	private static final String PATH_PREFIX = "/api/v1/auth/";

	private final int limite;
	private final int janelaSegundos;

	private final Map<String, Janela> contador = new ConcurrentHashMap<>();

	public RateLimitFilter(Environment environment) {
		this.limite = Integer.parseInt(
				environment.getProperty("app.ratelimit.auth.limite", "10"));
		this.janelaSegundos = Integer.parseInt(
				environment.getProperty("app.ratelimit.auth.janela-segundos", "60"));
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		String caminho = request.getRequestURI();
		if (!caminho.startsWith(PATH_PREFIX) || "OPTIONS".equalsIgnoreCase(request.getMethod())) {
			filterChain.doFilter(request, response);
			return;
		}

		String ip = request.getRemoteAddr();
		Instant agora = Instant.now();
		Janela janela = contador.compute(ip, (k, existente) -> {
			if (existente == null || existente.expirouEm().isBefore(agora)) {
				return new Janela(agora.plusSeconds(janelaSegundos));
			}
			return existente;
		});

		if (janela.contagem() >= limite) {
			escrever429(response);
			return;
		}

		janela.incrementar();
		filterChain.doFilter(request, response);
	}

	private void escrever429(HttpServletResponse response) throws IOException {
		response.setStatus(429);
		response.setCharacterEncoding("UTF-8");
		response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
		response.getWriter()
				.write("{\"type\":\"about:blank\",\"title\":\"Muitas requisições\",\"status\":429,"
						+ "\"detail\":\"Limite de " + limite + " tentativas por "
						+ janelaSegundos + "s excedido em /api/v1/auth/**\"}");
	}

	private static class Janela {
		private final Instant expirouEm;
		private int contagem;

		Janela(Instant expirouEm) {
			this.expirouEm = expirouEm;
			this.contagem = 0;
		}

		Instant expirouEm() {
			return expirouEm;
		}

		int contagem() {
			return contagem;
		}

		void incrementar() {
			contagem++;
		}
	}
}
