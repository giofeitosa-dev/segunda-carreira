package com.reconecta.ageblind.carreira.dto;

import java.util.List;

public record RespostaBuscaExternaResponse(
		String termo,
		String local,
		int pagina,
		long totalElementos,
		List<VagaExternaResponse> content) {
}
