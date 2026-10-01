package com.reconecta.ageblind.escudo.dominio;

import com.reconecta.ageblind.escudo.enums.CategoriaRegra;
import com.reconecta.ageblind.escudo.enums.Severidade;

/** Regra da técnica age-blind (fonte: 05-ETARISMO.md, seção 5). */
public record RegraIdade(
		String codigo,
		CategoriaRegra categoria,
		String descricao,
		Severidade severidade,
		String acao,
		String regex,
		String substituicao,
		boolean automatica) {
}
