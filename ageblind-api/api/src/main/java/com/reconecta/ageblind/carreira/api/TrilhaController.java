package com.reconecta.ageblind.carreira.api;

import java.util.List;

import com.reconecta.ageblind.carreira.dto.TrilhaResponse;
import com.reconecta.ageblind.carreira.service.TrilhaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/trilhas")
@Tag(name = "Carreira", description = "ODS 10+5 — perfis, vagas e trilhas (40+)")
public class TrilhaController {

	private final TrilhaService trilhaService;

	public TrilhaController(TrilhaService trilhaService) {
		this.trilhaService = trilhaService;
	}

	@GetMapping("/{area}")
	@Operation(summary = "Trilhas de capacitação curtas da área (lista vazia se não houver)")
	public List<TrilhaResponse> listar(@PathVariable String area) {
		return trilhaService.listarPorArea(area);
	}
}
