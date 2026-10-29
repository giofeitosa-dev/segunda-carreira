package com.reconecta.ageblind.carreira.service;

import java.time.LocalDateTime;
import java.util.List;

import com.reconecta.ageblind.carreira.domain.Inscricao;
import com.reconecta.ageblind.carreira.domain.Vaga;
import com.reconecta.ageblind.carreira.enums.TipoInscricao;
import com.reconecta.ageblind.carreira.repository.InscricaoRepository;
import com.reconecta.ageblind.carreira.repository.VagaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class LembreteJob {

	private static final Logger log = LoggerFactory.getLogger(LembreteJob.class);

	private final InscricaoRepository inscricaoRepository;
	private final VagaRepository vagaRepository;
	private final LembreteDispatcher dispatcher;

	public LembreteJob(InscricaoRepository inscricaoRepository, VagaRepository vagaRepository,
					   LembreteDispatcher dispatcher) {
		this.inscricaoRepository = inscricaoRepository;
		this.vagaRepository = vagaRepository;
		this.dispatcher = dispatcher;
	}

	@Scheduled(fixedDelayString = "${ageblind.lembrete.intervalo-ms:60000}")
	public void executar() {
		int identificados = disparar();
		if (identificados > 0) {
			log.info("Lembretes de prazo identificados na janela: {}", identificados);
		}
	}

	/** Encerra lembretes cujo prazo da vaga está dentro da antecedência (uma única vez). */
	@Transactional
	public int disparar() {
		LocalDateTime agora = LocalDateTime.now();
		int identificados = 0;
		List<Inscricao> agendadas = inscricaoRepository.findByLembreteAgendadoTrue();
		for (Inscricao inscricao : agendadas) {
			if (inscricao.getTipo() != TipoInscricao.VAGA) {
				continue;
			}
			Long vagaId;
			try {
				vagaId = Long.parseLong(inscricao.getReferenciaId());
			} catch (NumberFormatException e) {
				continue;
			}
			var vaga = vagaRepository.findById(vagaId).orElse(null);
			if (vaga == null || vaga.getPrazoInscricao() == null) {
				continue;
			}
			LocalDateTime limite = agora.plusHours(inscricao.getLembreteAntecedenciaHoras());
			if (vaga.getPrazoInscricao().isAfter(agora) && !vaga.getPrazoInscricao().isAfter(limite)) {
				dispatcher.despachar(inscricao, vaga);
				inscricao.setLembreteAgendado(false);
				inscricaoRepository.save(inscricao);
				identificados++;
			}
		}
		return identificados;
	}
}