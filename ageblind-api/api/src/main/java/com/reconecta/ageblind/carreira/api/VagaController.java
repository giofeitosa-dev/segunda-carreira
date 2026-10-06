package com.reconecta.ageblind.carreira.api;

import com.reconecta.ageblind.carreira.dto.VagaRecomendadaResponse;
import com.reconecta.ageblind.carreira.dto.VagaResponse;
import com.reconecta.ageblind.carreira.enums.ModeloVaga;
import com.reconecta.ageblind.carreira.service.RecomendacaoVagaService;
import com.reconecta.ageblind.carreira.service.VagaService;
import com.reconecta.ageblind.shared.dto.PaginaResponse;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vagas")
@Tag(name = "Carreira", description = "ODS 10+5 — perfis, vagas e trilhas (40+)")
public class VagaController {

	private final VagaService vagaService;
	private final RecomendacaoVagaService recomendacaoService;

	public VagaController(VagaService vagaService, RecomendacaoVagaService recomendacaoService) {
		this.vagaService = vagaService;
		this.recomendacaoService = recomendacaoService;
	}

	@GetMapping("/recomendadas")
	@Operation(summary = "Vagas ranqueadas só por competências — sem idade, faixa ou tempo de casa")
	public List<VagaRecomendadaResponse> recomendadas(
			@RequestParam long perfilId) {
		return recomendacaoService.recomendar(perfilId);
	}

	@GetMapping
	@Operation(summary = "Explora vagas por área/modelo com paginação (sem filtro de idade)")
	public PaginaResponse<VagaResponse> listar(
			@RequestParam(required = false) String area,
			@RequestParam(required = false) ModeloVaga modelo,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return vagaService.listar(area, modelo, page, size);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Detalhe da vaga")
	public VagaResponse obter(@PathVariable Long id) {
		return vagaService.obter(id);
	}
}
