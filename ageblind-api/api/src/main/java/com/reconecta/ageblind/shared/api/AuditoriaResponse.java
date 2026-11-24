package com.reconecta.ageblind.shared.api;

import java.time.LocalDateTime;

/**
 * Resposta paginada de entrada de auditoria de segurança.
 * Contém apenas metadados — nunca corpo da requisição/resposta, senha ou token.
 */
public class AuditoriaResponse {

	private Long id;
	private Long usuarioId;
	private String acao;
	private String recurso;
	private String ip;
	private LocalDateTime dataHora;
	private String correlationId;

	public AuditoriaResponse() {
	}

	public AuditoriaResponse(Long id, Long usuarioId, String acao, String recurso,
			String ip, LocalDateTime dataHora, String correlationId) {
		this.id = id;
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
}
