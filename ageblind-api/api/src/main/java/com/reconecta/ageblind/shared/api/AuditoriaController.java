package com.reconecta.ageblind.shared.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.reconecta.ageblind.auth.domain.AuditoriaSeguranca;
import com.reconecta.ageblind.auth.repository.AuditoriaSegurancaRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Endpoint de consulta ao log de auditoria de segurança (spec US1).
 * Apenas usuários com papel RH podem listar os registros.
 */
@RestController
@RequestMapping("/api/v1/auditoria")
@Tag(name = "Auth", description = "Cadastro, login e auditoria de segurança (papéis CANDIDATO/RH)")
public class AuditoriaController {

	private final AuditoriaSegurancaRepository auditoriaRepository;

	public AuditoriaController(AuditoriaSegurancaRepository auditoriaRepository) {
		this.auditoriaRepository = auditoriaRepository;
	}

	@GetMapping
	@Operation(summary = "Lista registros de auditoria de segurança (só RH)")
	public ResponseEntity<Page<AuditoriaResponse>> listar(
			@PageableDefault(page = 0, size = 20) Pageable pageable,
			@RequestParam(required = false) String acao) {
		Page<AuditoriaSeguranca> pagina;
		if (acao != null && !acao.isBlank()) {
			pagina = auditoriaRepository.findByAcao(acao, pageable);
		} else {
			pagina = auditoriaRepository.findAll(pageable);
		}
		Page<AuditoriaResponse> response = pagina.map(this::paraResponse);
		return ResponseEntity.ok(response);
	}

	private AuditoriaResponse paraResponse(AuditoriaSeguranca a) {
		return new AuditoriaResponse(
				a.getId(), a.getUsuarioId(), a.getAcao(), a.getRecurso(),
				a.getIp(), a.getDataHora(), a.getCorrelationId());
	}
}
