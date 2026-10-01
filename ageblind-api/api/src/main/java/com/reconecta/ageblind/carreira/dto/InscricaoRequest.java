package com.reconecta.ageblind.carreira.dto;

import com.reconecta.ageblind.carreira.enums.TipoInscricao;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InscricaoRequest(
		@NotNull(message = "perfilId é obrigatório") Long perfilId,
		@NotNull(message = "tipo é obrigatório") TipoInscricao tipo,
		@NotBlank(message = "referenciaId é obrigatório") String referenciaId) {
}
