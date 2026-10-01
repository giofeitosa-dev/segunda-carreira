package com.reconecta.ageblind.carreira.dto;

import java.math.BigDecimal;
import java.util.List;

public record TrilhaResponse(
		String id,
		String area,
		String nome,
		int duracaoSemanas,
		BigDecimal custo,
		String instituicao,
		boolean certificado,
		List<ModuloTrilhaResponse> modulos) {
}
