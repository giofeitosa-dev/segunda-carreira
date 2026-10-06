package com.reconecta.ageblind.escudo.dto;

import jakarta.validation.constraints.NotBlank;

public record AuditoriaCurriculoRequest(
		@NotBlank(message = "curriculo é obrigatório") String curriculo,
		String vagaAlvo) {
}
