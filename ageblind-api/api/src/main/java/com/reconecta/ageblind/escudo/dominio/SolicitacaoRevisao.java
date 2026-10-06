package com.reconecta.ageblind.escudo.dominio;

import java.time.LocalDateTime;
import java.util.UUID;

import com.reconecta.ageblind.escudo.enums.StatusRevisao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Pedido de revisão humana de decisão automatizada — LGPD art. 20.
 * O titular pode exigir revisão por pessoa humana e informação dos critérios.
 */
@Entity
@Table(name = "solicitacao_revisao")
public class SolicitacaoRevisao {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long perfilId;

	private Long vagaId;

	@Column(length = 1000)
	private String motivo;

	@Enumerated(EnumType.STRING)
	private StatusRevisao status = StatusRevisao.PENDENTE;

	@Column(unique = true)
	private String protocolo;

	private LocalDateTime criadoEm;

	@PrePersist
	void aoPersistir() {
		this.criadoEm = LocalDateTime.now();
		if (protocolo == null) {
			this.protocolo = "REV-" + LocalDateTime.now().getYear() + "-"
					+ UUID.randomUUID().toString().substring(0, 6).toUpperCase();
		}
	}

	public Long getId() { return id; }
	public Long getPerfilId() { return perfilId; }
	public Long getVagaId() { return vagaId; }
	public String getMotivo() { return motivo; }
	public StatusRevisao getStatus() { return status; }
	public String getProtocolo() { return protocolo; }
	public LocalDateTime getCriadoEm() { return criadoEm; }

	public void setPerfilId(Long perfilId) { this.perfilId = perfilId; }
	public void setVagaId(Long vagaId) { this.vagaId = vagaId; }
	public void setMotivo(String motivo) { this.motivo = motivo; }
}
