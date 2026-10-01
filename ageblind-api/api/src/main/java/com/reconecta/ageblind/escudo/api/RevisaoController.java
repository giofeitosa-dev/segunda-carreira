package com.reconecta.ageblind.escudo.api;

import com.reconecta.ageblind.escudo.dto.SolicitacaoRevisaoRequest;
import com.reconecta.ageblind.escudo.dto.SolicitacaoRevisaoResponse;
import com.reconecta.ageblind.escudo.service.RevisaoSolicitacaoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/revisao")
@Tag(name = "Escudo Anti-Etarismo", description = "ODS 10+5 — direitos do titular (LGPD art. 20)")
public class RevisaoController {

	private final RevisaoSolicitacaoService servico;

	public RevisaoController(RevisaoSolicitacaoService servico) {
		this.servico = servico;
	}

	@PostMapping("/solicitacao")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Pedido de revisão humana da decisão automatizada (LGPD art. 20)")
	public SolicitacaoRevisaoResponse solicitar(@Valid @RequestBody SolicitacaoRevisaoRequest req) {
		return servico.solicitar(req);
	}
}
