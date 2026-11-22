package com.reconecta.ageblind.auth.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Registro imutável de evento de segurança (audit log).
 * Armazena apenas metadados (usuario_id, acao, recurso, ip, data_hora, correlation_id)
 * — nunca corpo da requisição/resposta, senha ou token (pesquisa R5).
 */
@Entity
@Table(name = "auditoria_seguranca")
public class AuditoriaSeguranca {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "usuario_id")
	private Long usuarioId;

	@Column(nullable = false, length = 100)
	private String acao;

	@Column(nullable = false, length = 500)
	private String recurso;

	@Column(nullable = false, length = 45)
	private String ip;

	@Column(nullable = false)
	private LocalDateTime dataHora;

	@Column(nullable = false, length = 64, name = "correlation_id")
	private String correlationId;

	// --- construtor vazio (JPA) ---
	public AuditoriaSeguranca() {
	}

	public AuditoriaSeguranca(Long usuarioId, String acao, String recurso, String ip,
			LocalDateTime dataHora, String correlationId) {
		this.usuarioId = usuarioId;
		this.acao = acao;
		this.recurso = recurso;
		this.ip = ip;
		this.dataHora = dataHora;
		this.correlationId = correlationId;
	}

	// --- getters/setters ---
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public Long getUsuarioId() { return usuarioId; }
	public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

	public String getAcao() { return acao; }
	public void setAcao(String acao) { this.acao = acao; }

	public String getRecurso() { return recurso; }
	public void setRecurso(String recurso) { this.recurso = recurso; }

	public String getIp() { return ip; }
	public void setIp(String ip) { this.ip = ip; }

	public LocalDateTime getDataHora() { return dataHora; }
	public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }

	public String getCorrelationId() { return correlationId; }
	public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

	@Override
	public String toString() {
		return "AuditoriaSeguranca{id=" + id + ", usuarioId=" + usuarioId
				+ ", acao='" + acao + "', recurso='" + recurso + "', ip='" + ip
				+ "', dataHora=" + dataHora + ", correlationId='" + correlationId + "'}";
	}
}
