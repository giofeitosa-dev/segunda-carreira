package com.reconecta.ageblind.carreira.dto;

import java.math.BigDecimal;

public record VagaExternaResponse(
		String id,
		String titulo,
		String empresa,
		String local,
		String link,
		String publicadoEm,
		BigDecimal salarioMinimo,
		BigDecimal salarioMaximo,
		String descricao,
		String fonte) {
}
