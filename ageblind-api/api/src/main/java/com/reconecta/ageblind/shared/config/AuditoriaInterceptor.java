package com.reconecta.ageblind.shared.config;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.reconecta.ageblind.auth.domain.AuditoriaSeguranca;
import com.reconecta.ageblind.auth.repository.AuditoriaSegurancaRepository;
import com.reconecta.ageblind.shared.api.Auditar;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Interceptor de auditoria de segurança (spec US1).
 * Intercepta métodos de controller anotados com {@link Auditar} e grava um
 * registro imutável em auditoria_seguranca com falha-segura.
 *
 * <p>Extrai: usuario_id (sub do JWT), acao (anotação), recurso (anotação ou
 * caminho da requisição), ip (HttpServletRequest) e correlation_id (MDC).
 * O registro é gravado em afterCompletion, para que exceções (ex.: 403 de
 * autorização) também sejam auditadas.
 */
@Component
public class AuditoriaInterceptor implements HandlerInterceptor {

	private static final Logger log = LoggerFactory.getLogger(AuditoriaInterceptor.class);

	private final AuditoriaSegurancaRepository auditoriaRepository;

	private final ThreadLocal<Auditar> auditarAtual = new ThreadLocal<>();

	public AuditoriaInterceptor(AuditoriaSegurancaRepository auditoriaRepository) {
		this.auditoriaRepository = auditoriaRepository;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		if (handler instanceof org.springframework.web.method.HandlerMethod hm) {
			Auditar auditar = hm.getMethodAnnotation(Auditar.class);
			auditarAtual.set(auditar);
		} else {
			auditarAtual.set(null);
		}
		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception ex) throws Exception {
		Auditar auditar = auditarAtual.get();
		auditarAtual.remove();
		if (auditar == null) {
			return;
		}
		try {
			String acao = auditar.acao();
			if (response.getStatus() == HttpServletResponse.SC_FORBIDDEN) {
				acao = "ACESSO_NEGADO";
			}
			String ip = request.getRemoteAddr();
			String recurso = recurso(auditar, request);
			Long usuarioId = usuarioId();
			String correlationId = MDC.get("correlationId");

			AuditoriaSeguranca registro = new AuditoriaSeguranca(
					usuarioId, acao, recurso, ip, LocalDateTime.now(), correlationId);
			auditoriaRepository.save(registro);
		} catch (Exception ex2) {
			// Falha-segura: não derruba a requisição principal
			log.warn("Falha ao gravar registro de auditoria", ex2);
		}
	}

	private String recurso(Auditar auditar, HttpServletRequest request) {
		if (auditar.recurso() != null && !auditar.recurso().isBlank()) {
			return auditar.recurso();
		}
		return request.getRequestURI();
	}

	private Long usuarioId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated()) {
			return null;
		}
		Object principal = auth.getPrincipal();
		if (principal instanceof String) {
			try {
				return Long.valueOf((String) principal);
			} catch (NumberFormatException ex) {
				return null;
			}
		}
		return null;
	}
}
