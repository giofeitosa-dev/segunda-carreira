package com.reconecta.ageblind.carreira.api;

import java.util.List;

import com.reconecta.ageblind.carreira.dto.InscricaoRequest;
import com.reconecta.ageblind.carreira.dto.InscricaoResponse;
import com.reconecta.ageblind.carreira.dto.LembreteRequest;
import com.reconecta.ageblind.carreira.service.InscricaoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Carreira", description = "ODS 10+5 — perfis, vagas e trilhas (40+)")
public class InscricaoController {

	private final InscricaoService inscricaoService;

	public InscricaoController(InscricaoService inscricaoService) {
		this.inscricaoService = inscricaoService;
	}

	@PostMapping("/inscricoes")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Inscrição simplificada em vaga ou trilha")
	public InscricaoResponse criar(@Valid @RequestBody InscricaoRequest req) {
		return inscricaoService.criar(req);
	}

	@GetMapping("/inscricoes")
	@Operation(summary = "Lista inscrições de um perfil (cancelamento e lembrete)")
	public List<InscricaoResponse> listar(@RequestParam long perfilId) {
		return inscricaoService.listarPorPerfil(perfilId);
	}

	@DeleteMapping("/inscricoes/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Cancela uma inscrição (DELETE)")
	public void cancelar(@PathVariable Long id) {
		inscricaoService.cancelar(id);
	}

	@PostMapping("/webhooks/lembretes")
	@ResponseStatus(HttpStatus.ACCEPTED)
	@Operation(summary = "Agenda alerta de prazo de inscrição (202)")
	public void agendarLembrete(@Valid @RequestBody LembreteRequest req) {
		inscricaoService.agendarLembrete(req);
	}
}
