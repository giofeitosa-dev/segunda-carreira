package com.reconecta.ageblind.carreira.dto;

import java.util.List;

public record VagaRecomendadaResponse(
		VagaResponse vaga,
		double score,
		List<String> competenciasAtendidas) {
}
