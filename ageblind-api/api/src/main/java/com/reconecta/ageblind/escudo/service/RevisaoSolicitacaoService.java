package com.reconecta.ageblind.escudo.service;

import com.reconecta.ageblind.carreira.gateway.PerfilGateway;
import com.reconecta.ageblind.escudo.dominio.SolicitacaoRevisao;
import com.reconecta.ageblind.escudo.dto.SolicitacaoRevisaoRequest;
import com.reconecta.ageblind.escudo.dto.SolicitacaoRevisaoResponse;
import com.reconecta.ageblind.escudo.repository.SolicitacaoRevisaoRepository;
import com.reconecta.ageblind.shared.api.RecursoNaoEncontradoException;

import org.springframework.stereotype.Service;

/**
 * Revisão humana de decisão automatizada — LGPD art. 20.
 * Registra o pedido com protocolo; a empresa não pode recusar nem retaliar
 * (05-ETARISMO.md §4.3).
 */
@Service
public class RevisaoSolicitacaoService {

	private final SolicitacaoRevisaoRepository repository;
	private final PerfilGateway perfilGateway;

	public RevisaoSolicitacaoService(SolicitacaoRevisaoRepository repository, PerfilGateway perfilGateway) {
		this.repository = repository;
		this.perfilGateway = perfilGateway;
	}

	public SolicitacaoRevisaoResponse solicitar(SolicitacaoRevisaoRequest req) {
		if (!perfilGateway.existe(req.perfilId())) {
			throw new RecursoNaoEncontradoException("Perfil", req.perfilId());
		}
		SolicitacaoRevisao entidade = new SolicitacaoRevisao();
		entidade.setPerfilId(req.perfilId());
		entidade.setVagaId(req.vagaId());
		entidade.setMotivo(req.motivo());
		SolicitacaoRevisao salva = repository.save(entidade);
		return paraResponse(salva);
	}

	private SolicitacaoRevisaoResponse paraResponse(SolicitacaoRevisao s) {
		return new SolicitacaoRevisaoResponse(
				s.getId(), s.getPerfilId(), s.getVagaId(), s.getMotivo(),
				s.getStatus(), s.getProtocolo(), s.getCriadoEm());
	}
}
