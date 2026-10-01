package com.reconecta.ageblind.escudo.dto;

import jakarta.validation.constraints.NotBlank;

public record VagaAuditoriaRequest(
		@NotBlank(message = "texto é obrigatório") String texto,
		String titulo) {
}
