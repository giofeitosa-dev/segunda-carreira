package com.reconecta.ageblind.carreira.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.reconecta.ageblind.carreira.enums.Disponibilidade;
import com.reconecta.ageblind.carreira.enums.StatusCuradoria;

public record PerfilResponse(
		Long id,
		String nome,
		String email,
		String areaAtual,
		String areaAlvo,
		BigDecimal pretensaoSalarial,
		List<Disponibilidade> disponibilidade,
		StatusCuradoria statusCuradoria,
		LocalDateTime criadoEm) {
}
