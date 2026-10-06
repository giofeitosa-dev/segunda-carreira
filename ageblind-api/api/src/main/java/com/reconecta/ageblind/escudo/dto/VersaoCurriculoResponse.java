package com.reconecta.ageblind.escudo.dto;

import java.util.List;

public record VersaoCurriculoResponse(
		String tipo,
		String conteudo,
		List<RegraAplicada> regrasAplicadas) {
}
