package com.reconecta.ageblind.carreira.domain;

import com.reconecta.ageblind.carreira.enums.NivelIdioma;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Idioma do currículo (idioma + nível de proficiência). */
@Entity
@Table(name = "curriculo_idioma")
public class Idioma {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "perfil_id")
	private Perfil perfil;

	@NotBlank
	private String idioma;

	@NotNull
	@Enumerated(EnumType.STRING)
	private NivelIdioma nivel;

	public Long getId() { return id; }
	public Perfil getPerfil() { return perfil; }
	public void setPerfil(Perfil perfil) { this.perfil = perfil; }
	public String getIdioma() { return idioma; }
	public void setIdioma(String idioma) { this.idioma = idioma; }
	public NivelIdioma getNivel() { return nivel; }
	public void setNivel(NivelIdioma nivel) { this.nivel = nivel; }
}
