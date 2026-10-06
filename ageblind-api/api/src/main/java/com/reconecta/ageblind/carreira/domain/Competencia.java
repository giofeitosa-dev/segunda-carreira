package com.reconecta.ageblind.carreira.domain;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;

/** Catálogo de competências (candidato, vaga e trilha compartilham). */
@Entity
@Table(name = "competencia", uniqueConstraints = @UniqueConstraint(columnNames = { "nome", "area" }))
public class Competencia {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	private String nome;

	@NotBlank
	private String area;

	@ManyToMany(mappedBy = "requisitos")
	private Set<Vaga> vagas = new HashSet<>();

	public Competencia() {
	}

	public Competencia(String nome, String area) {
		this.nome = nome;
		this.area = area;
	}

	public Long getId() { return id; }
	public String getNome() { return nome; }
	public void setNome(String nome) { this.nome = nome; }
	public String getArea() { return area; }
	public void setArea(String area) { this.area = area; }
}
