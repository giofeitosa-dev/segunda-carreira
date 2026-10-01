package com.reconecta.ageblind.escudo.dto;

import java.util.List;

import com.reconecta.ageblind.escudo.enums.RiscoLegal;

public record AuditoriaVagaResponse(
		String titulo,
		List<String> termosEncontrados,
		RiscoLegal riscoLegal,
		String baseLegal,
		String sugestaoRedacao) {
}
