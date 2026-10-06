package com.reconecta.ageblind.carreira.api;

import java.util.List;

import com.reconecta.ageblind.carreira.dto.MentoriaResponse;
import com.reconecta.ageblind.carreira.service.MentoriaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/mentorias")
@Tag(name = "Carreira", description = "ODS 10+5 — perfis, vagas e trilhas (40+)")
public class MentoriaController {

	private final MentoriaService mentoriaService;

	public MentoriaController(MentoriaService mentoriaService) {
		this.mentoriaService = mentoriaService;
	}

	@GetMapping
	@Operation(summary = "Encaixe com mentores/pares da área (usa areaAlvo do perfil se informado)")
	public List<MentoriaResponse> listar(
			@RequestParam(required = false) Long perfilId,
			@RequestParam(required = false) String area) {
		return mentoriaService.listar(perfilId, area);
	}
}
