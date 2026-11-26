package com.reconecta.ageblind.shared.api;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de exportação de portabilidade (LGPD art. 20).
 * Contém apenas dados pessoais do titular — NUNCA senha ou hash.
 */
public class DadosTitularResponse {

	private UsuarioDto usuario;
	private PerfilDto perfil;
	private List<ExperienciaDto> experiencias;
	private List<FormacaoDto> formacoes;
	private List<ProjetoDto> projetos;
	private List<IdiomaDto> idiomas;
	private List<CertificacaoDto> certificacoes;
	private List<CompetenciaDto> competencias;
	private List<InscricaoDto> inscricoes;

	// --- getters/setters ---
	public UsuarioDto getUsuario() { return usuario; }
	public void setUsuario(UsuarioDto usuario) { this.usuario = usuario; }

	public PerfilDto getPerfil() { return perfil; }
	public void setPerfil(PerfilDto perfil) { this.perfil = perfil; }

	public List<ExperienciaDto> getExperiencias() { return experiencias; }
	public void setExperiencias(List<ExperienciaDto> experiencias) { this.experiencias = experiencias; }

	public List<FormacaoDto> getFormacoes() { return formacoes; }
	public void setFormacoes(List<FormacaoDto> formacoes) { this.formacoes = formacoes; }

	public List<ProjetoDto> getProjetos() { return projetos; }
	public void setProjetos(List<ProjetoDto> projetos) { this.projetos = projetos; }

	public List<IdiomaDto> getIdiomas() { return idiomas; }
	public void setIdiomas(List<IdiomaDto> idiomas) { this.idiomas = idiomas; }

	public List<CertificacaoDto> getCertificacoes() { return certificacoes; }
	public void setCertificacoes(List<CertificacaoDto> certificacoes) { this.certificacoes = certificacoes; }

	public List<CompetenciaDto> getCompetencias() { return competencias; }
	public void setCompetencias(List<CompetenciaDto> competencias) { this.competencias = competencias; }

	public List<InscricaoDto> getInscricoes() { return inscricoes; }
	public void setInscricoes(List<InscricaoDto> inscricoes) { this.inscricoes = inscricoes; }

	// --- sub-DTOs (record para imutabilidade) ---

	public record UsuarioDto(Long id, String email, String papel, LocalDateTime criadoEm) {}

	public record PerfilDto(Long id, String nome, String email, String telefone, String cidade,
			String resumo, String linkedin, String github, LocalDateTime criadoEm) {}

	public record ExperienciaDto(Long id, String cargo, String empresa, Integer anoInicio, Integer anoFim,
			String descricao) {}

	public record FormacaoDto(Long id, String curso, String instituicao, Integer anoConclusao) {}

	public record ProjetoDto(Long id, String nome, String descricao, String link) {}

	public record IdiomaDto(Long id, String idioma, String nivel) {}

	public record CertificacaoDto(Long id, String nome, String instituicao, Integer anoObtencao) {}

	public record CompetenciaDto(Long id, String nome, String nivel, Integer anosExperiencia) {}

	public record InscricaoDto(Long id, String tipo, String referenciaId, String status,
			LocalDateTime criadoEm) {}
}
