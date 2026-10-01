package com.reconecta.ageblind.escudo.dto;

import java.util.List;

public record AuditoriaCurriculoResponse(
		int scoreAts,
		List<SinalAuditoria> sinaisQueRevelamIdade,
		List<String> sugestoes) {
}
