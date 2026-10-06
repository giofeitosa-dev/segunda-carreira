package com.reconecta.ageblind.escudo.dominio;

import com.reconecta.ageblind.escudo.enums.Severidade;

/** Sinal no currículo que permite inferir idade (proxy). */
public record SinalIdade(
		String codigo,
		String descricao,
		String trecho,
		Severidade severidade,
		String acao) {
}
