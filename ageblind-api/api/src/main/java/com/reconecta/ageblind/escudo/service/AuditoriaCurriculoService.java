package com.reconecta.ageblind.escudo.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.reconecta.ageblind.escudo.dominio.SinalIdade;
import com.reconecta.ageblind.escudo.dto.AuditoriaCurriculoResponse;
import com.reconecta.ageblind.escudo.dto.SinalAuditoria;
import com.reconecta.ageblind.escudo.enums.Severidade;

import org.springframework.stereotype.Service;

/**
 * Auditoria de currículo: score ATS + lista de sinais que permitem inferir idade.
 * Base: 05-ETARISMO.md §1 (proxies) e §5.5 (checklist ATS).
 */
@Service
public class AuditoriaCurriculoService {

	private static final int PONTOS_ALTA = 20;
	private static final int PONTOS_MEDIA = 10;
	private static final int PONTOS_BAIXA = 5;

	private final MotorRegrasIdade motor;

	public AuditoriaCurriculoService(MotorRegrasIdade motor) {
		this.motor = motor;
	}

	public AuditoriaCurriculoResponse auditar(String curriculo, String vagaAlvo) {
		List<SinalIdade> sinais = motor.detectar(curriculo);

		List<SinalAuditoria> detalhe = sinais.stream()
				.map(s -> new SinalAuditoria(
						s.codigo() + " — " + s.descricao(),
						localizar(curriculo, s.trecho()),
						s.severidade().name(),
						s.acao()))
				.toList();

		int penalidade = sinais.stream().mapToInt(s -> pontos(s.severidade())).sum();
		int score = Math.max(0, 100 - penalidade);

		return new AuditoriaCurriculoResponse(score, detalhe, sugestoes(sinais, vagaAlvo));
	}

	private int pontos(Severidade severidade) {
		return switch (severidade) {
			case ALTA -> PONTOS_ALTA;
			case MEDIA -> PONTOS_MEDIA;
			case BAIXA -> PONTOS_BAIXA;
		};
	}

	private String localizar(String texto, String trecho) {
		int idx = texto.indexOf(trecho);
		if (idx < 0) {
			return "texto";
		}
		long linha = texto.substring(0, idx).chars().filter(c -> c == '\n').count() + 1;
		return "linha " + linha;
	}

	private List<String> sugestoes(List<SinalIdade> sinais, String vagaAlvo) {
		Set<String> sugestoes = new LinkedHashSet<>();
		for (SinalIdade s : sinais) {
			sugestoes.add(s.codigo() + ": " + s.acao());
		}
		if (vagaAlvo == null || vagaAlvo.isBlank()) {
			sugestoes.add("Informe vagaAlvo para espelhar as keywords da descrição (checklist ATS §5.5)");
		} else {
			sugestoes.add("Espelhe as keywords da vaga informada nas 3 primeiras linhas do currículo");
		}
		if (sinais.isEmpty()) {
			sugestoes.add("Nenhum proxy de idade detectado — mantenha certificações recentes visíveis");
		}
		return new ArrayList<>(sugestoes);
	}
}
