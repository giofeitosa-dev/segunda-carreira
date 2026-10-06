package com.reconecta.ageblind.carreira.domain;

import java.time.LocalDateTime;

import com.reconecta.ageblind.carreira.enums.StatusInscricao;
import com.reconecta.ageblind.carreira.enums.TipoInscricao;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "inscricao")
public class Inscricao {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "perfil_id")
	private Perfil perfil;

	@Enumerated(EnumType.STRING)
	private TipoInscricao tipo;

	/** id da vaga ou da trilha conforme o tipo. */
	private String referenciaId;

	@Enumerated(EnumType.STRING)
	private StatusInscricao status = StatusInscricao.ENVIADA;

	private boolean lembreteAgendado;

	/** Antecedência do lembrete em horas (padrão 24). */
	private int lembreteAntecedenciaHoras = 24;

	private LocalDateTime criadoEm;

	@PrePersist
	void aoPersistir() {
		this.criadoEm = LocalDateTime.now();
	}

	public Long getId() { return id; }
	public Perfil getPerfil() { return perfil; }
	public void setPerfil(Perfil perfil) { this.perfil = perfil; }
	public TipoInscricao getTipo() { return tipo; }
	public void setTipo(TipoInscricao tipo) { this.tipo = tipo; }
	public String getReferenciaId() { return referenciaId; }
	public void setReferenciaId(String referenciaId) { this.referenciaId = referenciaId; }
	public StatusInscricao getStatus() { return status; }
	public void setStatus(StatusInscricao status) { this.status = status; }
	public boolean isLembreteAgendado() { return lembreteAgendado; }
	public void setLembreteAgendado(boolean l) { this.lembreteAgendado = l; }
	public int getLembreteAntecedenciaHoras() { return lembreteAntecedenciaHoras; }
	public void setLembreteAntecedenciaHoras(int h) { this.lembreteAntecedenciaHoras = h; }
	public LocalDateTime getCriadoEm() { return criadoEm; }
}
