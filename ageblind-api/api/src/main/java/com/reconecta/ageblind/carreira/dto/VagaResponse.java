package com.reconecta.ageblind.carreira.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.reconecta.ageblind.carreira.enums.ModeloVaga;

public record VagaResponse(
		Long id,
		String titulo,
		String empresa,
		String area,
		String descricao,
		BigDecimal salario,
		ModeloVaga modelo,
		LocalDateTime prazoInscricao,
		List<String> requisitos) {
}
