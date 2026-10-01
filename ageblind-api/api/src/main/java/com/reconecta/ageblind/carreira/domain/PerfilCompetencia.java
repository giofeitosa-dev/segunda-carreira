package com.reconecta.ageblind.carreira.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/** Associação perfil ↔ competência com nível de domínio (0–5). */
@Entity
@Table(name = "perfil_competencia")
public class PerfilCompetencia {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "perfil_id")
	private Perfil perfil;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "competencia_id")
	private Competencia competencia;

	@Min(0)
	@Max(5)
	private int nivel;

	public Long getId() { return id; }
	public Perfil getPerfil() { return perfil; }
	public void setPerfil(Perfil perfil) { this.perfil = perfil; }
	public Competencia getCompetencia() { return competencia; }
	public void setCompetencia(Competencia c) { this.competencia = c; }
	public int getNivel() { return nivel; }
	public void setNivel(int nivel) { this.nivel = nivel; }
}
