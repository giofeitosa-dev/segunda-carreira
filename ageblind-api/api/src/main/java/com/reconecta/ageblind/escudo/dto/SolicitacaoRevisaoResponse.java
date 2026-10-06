package com.reconecta.ageblind.escudo.dto;

import java.time.LocalDateTime;

import com.reconecta.ageblind.escudo.enums.StatusRevisao;

public record SolicitacaoRevisaoResponse(
		Long id,
		Long perfilId,
		Long vagaId,
		String motivo,
		StatusRevisao status,
		String protocolo,
		LocalDateTime criadoEm) {
}
