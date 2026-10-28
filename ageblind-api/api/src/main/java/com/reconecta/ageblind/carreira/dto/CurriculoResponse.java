package com.reconecta.ageblind.carreira.dto;

import java.util.List;

import com.reconecta.ageblind.carreira.enums.NivelIdioma;

/** Currículo estruturado do perfil (experiências, formações, projetos, idiomas, certificações). */
public record CurriculoResponse(
		List<ExperienciaResponse> experiencias,
		List<FormacaoResponse> formacoes,
		List<ProjetoResponse> projetos,
		List<IdiomaResponse> idiomas,
		List<CertificacaoResponse> certificacoes) {

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

	public record IdiomaResponse(
			Long id,
			String idioma,
			NivelIdioma nivel) {
	}

	public record CertificacaoResponse(
			Long id,
			String nome,
			String instituicao,
			Integer anoConclusao,
			String descricao,
			String link) {
	}
}
