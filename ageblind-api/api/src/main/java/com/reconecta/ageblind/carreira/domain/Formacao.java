package com.reconecta.ageblind.carreira.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

/**
 * Formação acadêmica / cursos do currículo.
 * REGRA: sem data de nascimento e sem idade — só ano de conclusão.
 */
@Entity
@Table(name = "curriculo_formacao")
public class Formacao {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "perfil_id")
	private Perfil perfil;

	@NotBlank
	private String curso;

	@NotBlank
	private String instituicao;

	/** null = em andamento. */
	private Integer anoConclusao;

	@Column(length = 1000)
	private String descricao;

	public Long getId() { return id; }
	public Perfil getPerfil() { return perfil; }
	public void setPerfil(Perfil perfil) { this.perfil = perfil; }
	public String getCurso() { return curso; }
	public void setCurso(String curso) { this.curso = curso; }
	public String getInstituicao() { return instituicao; }
	public void setInstituicao(String instituicao) { this.instituicao = instituicao; }
	public Integer getAnoConclusao() { return anoConclusao; }
	public void setAnoConclusao(Integer anoConclusao) { this.anoConclusao = anoConclusao; }
	public String getDescricao() { return descricao; }
	public void setDescricao(String descricao) { this.descricao = descricao; }
}
