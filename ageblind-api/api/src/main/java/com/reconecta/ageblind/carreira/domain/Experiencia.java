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
 * Experiência profissional do currículo.
 * REGRA: sem data de nascimento e sem idade — só anos de atuação.
 */
@Entity
@Table(name = "curriculo_experiencia")
public class Experiencia {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "perfil_id")
	private Perfil perfil;

	@NotBlank
	private String cargo;

	@NotBlank
	private String empresa;

	private int anoInicio;

	/** null = em andamento. */
	private Integer anoFim;

	@Column(length = 2000)
	private String descricao;

	public Long getId() { return id; }
	public Perfil getPerfil() { return perfil; }
	public void setPerfil(Perfil perfil) { this.perfil = perfil; }
	public String getCargo() { return cargo; }
	public void setCargo(String cargo) { this.cargo = cargo; }
	public String getEmpresa() { return empresa; }
	public void setEmpresa(String empresa) { this.empresa = empresa; }
	public int getAnoInicio() { return anoInicio; }
	public void setAnoInicio(int anoInicio) { this.anoInicio = anoInicio; }
	public Integer getAnoFim() { return anoFim; }
	public void setAnoFim(Integer anoFim) { this.anoFim = anoFim; }
	public String getDescricao() { return descricao; }
	public void setDescricao(String descricao) { this.descricao = descricao; }
}
