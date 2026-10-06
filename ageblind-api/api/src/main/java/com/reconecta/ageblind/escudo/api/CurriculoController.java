package com.reconecta.ageblind.escudo.api;

import com.reconecta.ageblind.escudo.dto.AuditoriaCurriculoRequest;
import com.reconecta.ageblind.escudo.dto.AuditoriaCurriculoResponse;
import com.reconecta.ageblind.escudo.dto.CurriculoRequest;
import com.reconecta.ageblind.escudo.dto.VersaoCurriculoResponse;
import com.reconecta.ageblind.escudo.service.AuditoriaCurriculoService;
import com.reconecta.ageblind.escudo.service.MotorRegrasIdade;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/curriculo")
@Tag(name = "Escudo Anti-Etarismo", description = "ODS 10+5 — currículo age-blind, auditorias e métricas")
public class CurriculoController {

	private final MotorRegrasIdade motor;
	private final AuditoriaCurriculoService auditoria;

	public CurriculoController(MotorRegrasIdade motor, AuditoriaCurriculoService auditoria) {
		this.motor = motor;
		this.auditoria = auditoria;
	}

	@PostMapping("/age-blind")
	@Operation(summary = "Gera versão do currículo sem proxies de idade (15 regras)")
	public VersaoCurriculoResponse ageBlind(@Valid @RequestBody CurriculoRequest req) {
		MotorRegrasIdade.ResultadoAgeBlind resultado = motor.aplicar(req.curriculo());
		return new VersaoCurriculoResponse("AGE_BLIND", resultado.conteudo(), resultado.regrasAplicadas());
	}

	@PostMapping("/auditoria")
	@Operation(summary = "Score ATS + sinais que revelam idade + sugestões de correção")
	public AuditoriaCurriculoResponse auditar(@Valid @RequestBody AuditoriaCurriculoRequest req) {
		return auditoria.auditar(req.curriculo(), req.vagaAlvo());
	}
}
