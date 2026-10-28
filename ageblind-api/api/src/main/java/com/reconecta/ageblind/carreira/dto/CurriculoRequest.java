package com.reconecta.ageblind.carreira.dto;

import java.util.List;

import com.reconecta.ageblind.carreira.enums.NivelIdioma;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Currículo estruturado enviado pelo formulário (substitui a estrutura inteira).
 * REGRA: nenhum campo de data de nascimento ou idade.
 */
public record CurriculoRequest(
		List<@Valid ExperienciaRequest> experiencias,
		List<@Valid FormacaoRequest> formacoes,
		List<@Valid ProjetoRequest> projetos,
		List<@Valid IdiomaRequest> idiomas,
		List<@Valid CertificacaoRequest> certificacoes) {

	public record ExperienciaRequest(
			@NotBlank String cargo,
			@NotBlank String empresa,
			@NotNull @Min(1900) Integer anoInicio,
			Integer anoFim,
			String descricao) {
	}

	public record FormacaoRequest(
			@NotBlank String curso,
			@NotBlank String instituicao,
			Integer anoConclusao,
			String descricao) {
	}

	public record ProjetoRequest(
			@NotBlank String nome,
			String descricao,
			String link) {
	}

	public record IdiomaRequest(
			@NotBlank String idioma,
			@NotNull NivelIdioma nivel) {
	}

	public record CertificacaoRequest(
			@NotBlank String nome,
			String instituicao,
			Integer anoConclusao,
			String descricao,
			String link) {
	}
}
