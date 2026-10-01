package com.reconecta.ageblind.carreira.dto;

import jakarta.validation.constraints.NotNull;

public record LembreteRequest(
		@NotNull(message = "inscricaoId é obrigatório") Long inscricaoId,
		Integer antecedenciaHoras) {

	public int antecedenciaEfetiva() {
		return antecedenciaHoras == null || antecedenciaHoras <= 0 ? 24 : antecedenciaHoras;
	}
}
