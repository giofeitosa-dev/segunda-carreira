package com.reconecta.ageblind.carreira.service;

import com.reconecta.ageblind.carreira.domain.Inscricao;
import com.reconecta.ageblind.carreira.domain.Perfil;
import com.reconecta.ageblind.carreira.dto.InscricaoRequest;
import com.reconecta.ageblind.carreira.dto.InscricaoResponse;
import com.reconecta.ageblind.carreira.dto.LembreteRequest;
import com.reconecta.ageblind.carreira.repository.InscricaoRepository;
import com.reconecta.ageblind.carreira.repository.PerfilRepository;
import com.reconecta.ageblind.carreira.repository.TrilhaRepository;
import com.reconecta.ageblind.carreira.repository.VagaRepository;
import com.reconecta.ageblind.shared.api.RecursoNaoEncontradoException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InscricaoService {

	private final InscricaoRepository inscricaoRepository;
	private final PerfilRepository perfilRepository;
	private final VagaRepository vagaRepository;
	private final TrilhaRepository trilhaRepository;

	public InscricaoService(
			InscricaoRepository inscricaoRepository,
			PerfilRepository perfilRepository,
			VagaRepository vagaRepository,
			TrilhaRepository trilhaRepository) {
		this.inscricaoRepository = inscricaoRepository;
		this.perfilRepository = perfilRepository;
		this.vagaRepository = vagaRepository;
		this.trilhaRepository = trilhaRepository;
	}

	@Transactional
	public InscricaoResponse criar(InscricaoRequest req) {
		Perfil perfil = perfilRepository.findById(req.perfilId())
				.orElseThrow(() -> new RecursoNaoEncontradoException("Perfil", req.perfilId()));
		validarReferencia(req);

		Inscricao inscricao = new Inscricao();
		inscricao.setPerfil(perfil);
		inscricao.setTipo(req.tipo());
		inscricao.setReferenciaId(req.referenciaId().trim());
		Inscricao salva = inscricaoRepository.save(inscricao);
		return paraResponse(salva);
	}

	private void validarReferencia(InscricaoRequest req) {
		String referencia = req.referenciaId().trim();
		try {
			Long id = Long.parseLong(referencia);
			boolean existe = switch (req.tipo()) {
				case VAGA -> vagaRepository.existsById(id);
				case TRILHA -> trilhaRepository.existsById(id);
			};
			if (!existe) {
				throw new RecursoNaoEncontradoException(
						req.tipo() == com.reconecta.ageblind.carreira.enums.TipoInscricao.VAGA
								? "Vaga" : "Trilha", referencia);
			}
		} catch (NumberFormatException e) {
			throw new RecursoNaoEncontradoException(
					req.tipo() == com.reconecta.ageblind.carreira.enums.TipoInscricao.VAGA
							? "Vaga" : "Trilha", referencia);
		}
	}

	@Transactional
	public void agendarLembrete(LembreteRequest req) {
		Inscricao inscricao = inscricaoRepository.findById(req.inscricaoId())
				.orElseThrow(() -> new RecursoNaoEncontradoException("Inscrição", req.inscricaoId()));
		inscricao.setLembreteAgendado(true);
		inscricao.setLembreteAntecedenciaHoras(req.antecedenciaEfetiva());
		inscricaoRepository.save(inscricao);
	}

	private InscricaoResponse paraResponse(Inscricao i) {
		return new InscricaoResponse(i.getId(), i.getPerfil().getId(), i.getTipo(), i.getStatus(), i.getCriadoEm());
	}
}
