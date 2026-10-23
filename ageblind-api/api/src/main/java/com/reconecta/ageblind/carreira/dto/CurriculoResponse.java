package com.reconecta.ageblind.carreira.dto;

import java.util.List;

/** Currículo estruturado do perfil (experiências, formações, projetos). */
public record CurriculoResponse(
		List<ExperienciaResponse> experiencias,
		List<FormacaoResponse> formacoes,
		List<ProjetoResponse> projetos) {

	public record ExperienciaResponse(
			Long id,
			String cargo,
			String empresa,
			int anoInicio,
			Integer anoFim,
			String descricao) {
	}

	public record FormacaoResponse(
			Long id,
			String curso,
			String instituicao,
			Integer anoConclusao,
			String descricao) {
	}

	public record ProjetoResponse(
			Long id,
			String nome,
			String descricao,
			String link) {
	}
}
