package com.reconecta.ageblind.escudo.api;

import com.reconecta.ageblind.escudo.dto.AuditoriaVagaResponse;
import com.reconecta.ageblind.escudo.dto.VagaAuditoriaRequest;
import com.reconecta.ageblind.escudo.service.AuditoriaVagaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vagas")
@Tag(name = "Escudo Anti-Etarismo", description = "ODS 10+5 — auditoria de linguagem de vagas")
public class VagaAuditoriaController {

	private final AuditoriaVagaService auditoria;

	public VagaAuditoriaController(AuditoriaVagaService auditoria) {
		this.auditoria = auditoria;
	}

	@PostMapping("/auditoria-linguagem")
	@Operation(summary = "Detecta termos com viés etário, risco legal e sugere reescrita")
	public AuditoriaVagaResponse auditar(@Valid @RequestBody VagaAuditoriaRequest req) {
		return auditoria.auditar(req.texto(), req.titulo());
	}
}
