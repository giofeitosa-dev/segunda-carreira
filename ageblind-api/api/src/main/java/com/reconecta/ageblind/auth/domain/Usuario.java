package com.reconecta.ageblind.auth.domain;

import java.time.LocalDateTime;

import com.reconecta.ageblind.carreira.domain.Perfil;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Identidade autenticável (JWT). Senha sempre BCrypt — nunca em resposta.
 * Vínculo 1:1 opcional com Perfil: a posse de dados pessoais é resolvida
 * comparando usuario.perfilId com o id do recurso (pesquisa R3).
 */
@Entity
@Table(name = "usuario")
public class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@Email
	@Column(length = 250, nullable = false, unique = true)
	private String email;

	/** Hash BCrypt (≥60 chars). Nunca devolver em DTO de resposta. */
	@NotBlank
	@Size(min = 8, max = 72)
	@Column(nullable = false, length = 100)
	private String senha;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Papel papel;

	/** Dono dos dados pessoais — NULL enquanto o candidato não criou Perfil. */
	@OneToOne
	@JoinColumn(name = "perfil_id")
	private Perfil perfil;

	private LocalDateTime criadoEm;

	@PrePersist
	void aoPersistir() {
		if (this.criadoEm == null) {
			this.criadoEm = LocalDateTime.now();
		}
	}

	// --- getters/setters ---
	public Long getId() { return id; }
	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }
	public String getSenha() { return senha; }
	public void setSenha(String senha) { this.senha = senha; }
	public Papel getPapel() { return papel; }
	public void setPapel(Papel papel) { this.papel = papel; }
	public Perfil getPerfil() { return perfil; }
	public void setPerfil(Perfil perfil) { this.perfil = perfil; }
	public LocalDateTime getCriadoEm() { return criadoEm; }
}
