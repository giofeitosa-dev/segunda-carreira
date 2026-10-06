package com.reconecta.ageblind.escudo.dominio;

import com.reconecta.ageblind.shared.enums.FaixaEtaria;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Agregado anônimo do funil: candidatos/aprovados por etapa, faixa e período.
 * Nunca armazena idade ou data de nascimento — apenas faixa etária (regra de ouro).
 */
@Entity
@Table(name = "metrica_funil")
public class MetricaFunil {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long vagaId;

	private String etapa;

	@Enumerated(EnumType.STRING)
	private FaixaEtaria faixa;

	private int candidatos;

	private int aprovados;

	/** Formato AAAA-MM. */
	private String periodo;

	public Long getId() { return id; }
	public Long getVagaId() { return vagaId; }
	public String getEtapa() { return etapa; }
	public FaixaEtaria getFaixa() { return faixa; }
	public int getCandidatos() { return candidatos; }
	public int getAprovados() { return aprovados; }
	public String getPeriodo() { return periodo; }

	public void setVagaId(Long vagaId) { this.vagaId = vagaId; }
	public void setEtapa(String etapa) { this.etapa = etapa; }
	public void setFaixa(FaixaEtaria faixa) { this.faixa = faixa; }
	public void setCandidatos(int candidatos) { this.candidatos = candidatos; }
	public void setAprovados(int aprovados) { this.aprovados = aprovados; }
	public void setPeriodo(String periodo) { this.periodo = periodo; }
}
