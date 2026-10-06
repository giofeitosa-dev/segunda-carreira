package com.reconecta.ageblind.carreira.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.reconecta.ageblind.carreira.enums.Disponibilidade;
import com.reconecta.ageblind.carreira.enums.StatusCuradoria;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Perfil profissional do candidato 40+.
 * REGRA: sem data de nascimento e sem idade — anti-vies por design (LGPD + CF art. 7º XXX).
 */
@Entity
@Table(name = "perfil")
public class Perfil {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	private String nome;

	@Email
	private String email;

	/** CBO simplificado. */
	private String areaAtual;
	private String areaAlvo;

	private BigDecimal pretensaoSalarial;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "perfil_disponibilidade", joinColumns = @JoinColumn(name = "perfil_id"))
	@Enumerated(EnumType.STRING)
	private Set<Disponibilidade> disponibilidade = new HashSet<>();

	@Enumerated(EnumType.STRING)
	private StatusCuradoria statusCuradoria = StatusCuradoria.EM_CURADORIA;

	@OneToMany(mappedBy = "perfil", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
	private Set<PerfilCompetencia> competencias = new HashSet<>();

	private LocalDateTime criadoEm;

	@PrePersist
	void aoPersistir() {
		this.criadoEm = LocalDateTime.now();
	}

	public void adicionarCompetencia(Competencia competencia, int nivel) {
		PerfilCompetencia pc = new PerfilCompetencia();
		pc.setPerfil(this);
		pc.setCompetencia(competencia);
		pc.setNivel(nivel);
		this.competencias.add(pc);
	}

	// --- getters/setters ---
	public Long getId() { return id; }
	public String getNome() { return nome; }
	public void setNome(String nome) { this.nome = nome; }
	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }
	public String getAreaAtual() { return areaAtual; }
	public void setAreaAtual(String areaAtual) { this.areaAtual = areaAtual; }
	public String getAreaAlvo() { return areaAlvo; }
	public void setAreaAlvo(String areaAlvo) { this.areaAlvo = areaAlvo; }
	public BigDecimal getPretensaoSalarial() { return pretensaoSalarial; }
	public void setPretensaoSalarial(BigDecimal p) { this.pretensaoSalarial = p; }
	public Set<Disponibilidade> getDisponibilidade() { return disponibilidade; }
	public StatusCuradoria getStatusCuradoria() { return statusCuradoria; }
	public void setStatusCuradoria(StatusCuradoria s) { this.statusCuradoria = s; }
	public Set<PerfilCompetencia> getCompetencias() { return competencias; }
	public LocalDateTime getCriadoEm() { return criadoEm; }
}
