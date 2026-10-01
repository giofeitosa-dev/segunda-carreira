package com.reconecta.ageblind.shared.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Faixa etária usada apenas em métricas agregadas (funil).
 * Regra de ouro do projeto: NUNCA armazenar idade ou data de nascimento.
 */
public enum FaixaEtaria {

	ATE_39("ATE_39"),
	DE_40_A_49("40_49"),
	MAIS_50("50_MAIS");

	private final String codigo;

	FaixaEtaria(String codigo) {
		this.codigo = codigo;
	}

	@JsonValue
	public String getCodigo() {
		return codigo;
	}

	@JsonCreator
	public static FaixaEtaria fromCodigo(String codigo) {
		for (FaixaEtaria f : values()) {
			if (f.codigo.equalsIgnoreCase(codigo) || f.name().equalsIgnoreCase(codigo)) {
				return f;
			}
		}
		throw new IllegalArgumentException("Faixa etária desconhecida: " + codigo);
	}
}
