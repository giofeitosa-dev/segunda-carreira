package com.reconecta.ageblind.escudo.dto;

import java.util.List;

public record MetricasFunilResponse(
		Long vagaId,
		List<EtapaMetrica> etapas,
		String interpretacao,
		String caminhoParaRevisao) {
}
