package com.reconecta.ageblind.escudo.dto;

import jakarta.validation.constraints.NotBlank;

public record CurriculoRequest(
		@NotBlank(message = "curriculo é obrigatório") String curriculo,
		String vagaAlvo) {
}
