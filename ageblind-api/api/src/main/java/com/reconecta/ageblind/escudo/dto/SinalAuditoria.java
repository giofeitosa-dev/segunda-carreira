package com.reconecta.ageblind.escudo.dto;

public record SinalAuditoria(
		String sinal,
		String localizacao,
		String severidade,
		String acao) {
}
