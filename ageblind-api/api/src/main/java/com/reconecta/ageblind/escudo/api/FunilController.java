package com.reconecta.ageblind.escudo.api;

import com.reconecta.ageblind.escudo.dto.MetricasFunilResponse;
import com.reconecta.ageblind.escudo.service.FunilMetricasService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/funil")
@Tag(name = "Escudo Anti-Etarismo", description = "ODS 10+5 — métricas de funil e adverse impact")
public class FunilController {

	private final FunilMetricasService metricas;

	public FunilController(FunilMetricasService metricas) {
		this.metricas = metricas;
	}

	@GetMapping("/metricas")
	@Operation(summary = "Taxa de avanço por faixa etária + alerta de adverse impact (razão < 0,80)")
	public MetricasFunilResponse metricas(
			@Parameter(required = true) @RequestParam long vagaId,
			@RequestParam(required = false) String periodo) {
		return metricas.metricas(vagaId, periodo);
	}
}
