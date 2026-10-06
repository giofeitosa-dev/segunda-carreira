package com.reconecta.ageblind.carreira.service;

import java.time.LocalDateTime;
import java.util.List;

import com.reconecta.ageblind.carreira.domain.Inscricao;
import com.reconecta.ageblind.carreira.enums.TipoInscricao;
import com.reconecta.ageblind.carreira.repository.InscricaoRepository;
import com.reconecta.ageblind.carreira.repository.VagaRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Job de lembretes de prazo de inscrição (POST /webhooks/lembretes agenda;
 * este job dispara os que vencem na janela de antecedência).
 */
@Component
public class LembreteJob {

	private static final Logger log = LoggerFactory.getLogger(LembreteJob.class);

	private final InscricaoRepository inscricaoRepository;
	private final VagaRepository vagaRepository;

	public LembreteJob(InscricaoRepository inscricaoRepository, VagaRepository vagaRepository) {
		this.inscricaoRepository = inscricaoRepository;
		this.vagaRepository = vagaRepository;
	}

	@Scheduled(fixedDelayString = "${ageblind.lembrete.intervalo-ms:60000}")
	public void executar() {
		int disparados = disparar();
		if (disparados > 0) {
			log.info("Lembretes de prazo disparados: {}", disparados);
		}
	}

	/** Dispara lembretes cujo prazo da vaga está dentro da antecedência. */
	@Transactional
	public int disparar() {
		LocalDateTime agora = LocalDateTime.now();
		int disparados = 0;
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
				disparados++;
			}
		}
		return disparados;
	}
}
