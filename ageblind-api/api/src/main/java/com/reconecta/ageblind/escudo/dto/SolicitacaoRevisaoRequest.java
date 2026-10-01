package com.reconecta.ageblind.escudo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SolicitacaoRevisaoRequest(
		@NotNull(message = "perfilId é obrigatório") Long perfilId,
		Long vagaId,
		@NotBlank(message = "motivo é obrigatório")
		@Size(max = 1000, message = "motivo deve ter no máximo 1000 caracteres") String motivo) {
}
