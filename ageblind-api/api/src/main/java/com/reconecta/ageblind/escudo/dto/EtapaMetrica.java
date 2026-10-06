package com.reconecta.ageblind.escudo.dto;

import java.util.Map;

import com.reconecta.ageblind.shared.enums.FaixaEtaria;

public record EtapaMetrica(
		String nome,
		Map<FaixaEtaria, Double> aprovacao,
		double razaoAdverseImpact,
		boolean alerta) {
}
