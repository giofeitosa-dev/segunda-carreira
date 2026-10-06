package com.reconecta.ageblind.carreira.dto;

import java.math.BigDecimal;
import java.util.List;

import com.reconecta.ageblind.carreira.enums.Disponibilidade;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record PerfilRequest(
		@NotBlank(message = "nome é obrigatório") String nome,
		@NotBlank(message = "email é obrigatório") @Email(message = "email inválido") String email,
		String areaAtual,
		@NotBlank(message = "areaAlvo é obrigatória") String areaAlvo,
		@PositiveOrZero(message = "pretensaoSalarial deve ser ≥ 0") BigDecimal pretensaoSalarial,
		List<Disponibilidade> disponibilidade) {
}
