package com.reconecta.ageblind.shared.config;

import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filtro de correlation-id (pesquisa R2).
 * Lê o header X-Correlation-Id da requisição; se ausente, gera um UUID v4.
 * Coloca o valor no MDC para que os logs estruturados o incluam e adiciona o
 * header X-Correlation-Id na resposta para que o cliente possa correlacionar.
 * Registrado antes do RateLimitFilter no SecurityConfig (ordem mais alta).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

	private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
	private static final String MDC_KEY = "correlationId";

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		String correlationId = request.getHeader(CORRELATION_ID_HEADER);
		if (correlationId == null || correlationId.isBlank()) {
			correlationId = UUID.randomUUID().toString();
		}

		MDC.put(MDC_KEY, correlationId);
		try {
			response.setHeader(CORRELATION_ID_HEADER, correlationId);
			filterChain.doFilter(request, response);
		} finally {
			MDC.remove(MDC_KEY);
		}
	}
}
