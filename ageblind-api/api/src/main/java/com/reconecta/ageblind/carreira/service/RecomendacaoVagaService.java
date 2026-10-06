package com.reconecta.ageblind.carreira.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.reconecta.ageblind.carreira.domain.Perfil;
import com.reconecta.ageblind.carreira.domain.PerfilCompetencia;
import com.reconecta.ageblind.carreira.domain.Vaga;
import com.reconecta.ageblind.carreira.dto.VagaRecomendadaResponse;
import com.reconecta.ageblind.carreira.dto.VagaResponse;
import com.reconecta.ageblind.carreira.repository.PerfilRepository;
import com.reconecta.ageblind.carreira.repository.VagaRepository;
import com.reconecta.ageblind.shared.api.CaminhosLgpd;
import com.reconecta.ageblind.shared.api.RecursoNaoEncontradoException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ranking de vagas SOLO por competências — regra de ouro do projeto.
 * score = Σ(nível das competências casadas) / (5 × nº de requisitos da vaga),
 * desempate por nível total e depois por id. Nenhum dado pessoal
 * (idade, faixa, salário, tempo de casa) entra na fórmula.
 */
@Service
public class RecomendacaoVagaService {

	private static final int NIVEL_MAXIMO = 5;

	private final PerfilRepository perfilRepository;
	private final VagaRepository vagaRepository;
	private final VagaService vagaService;

	public RecomendacaoVagaService(
			PerfilRepository perfilRepository,
			VagaRepository vagaRepository,
			VagaService vagaService) {
		this.perfilRepository = perfilRepository;
		this.vagaRepository = vagaRepository;
		this.vagaService = vagaService;
	}

	@Transactional
	public List<VagaRecomendadaResponse> recomendar(Long perfilId) {
		Perfil perfil = perfilRepository.findById(perfilId)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Perfil", perfilId));

		Map<String, Integer> niveisPorNome = perfil.getCompetencias().stream()
				.collect(Collectors.toMap(
						pc -> normalizar(pc.getCompetencia().getNome()),
						PerfilCompetencia::getNivel,
						(a, b) -> Math.max(a, b)));

		List<VagaRecomendadaResponse> recomendadas = new ArrayList<>();
		for (Vaga vaga : vagaRepository.findAll()) {
			VagaRecomendadaResponse avaliada = avaliar(vaga, niveisPorNome);
			if (avaliada != null) {
				recomendadas.add(avaliada);
			}
		}

		recomendadas.sort(Comparator
				.comparingDouble(VagaRecomendadaResponse::score).reversed()
				.thenComparing(r -> r.competenciasAtendidas().size(), Comparator.reverseOrder())
				.thenComparing(r -> r.vaga().id()));

		return recomendadas;
	}

	private VagaRecomendadaResponse avaliar(Vaga vaga, Map<String, Integer> niveisPorNome) {
		Set<String> atendidas = new TreeSet<>();
		int somaNiveis = 0;
		List<String> requisitos = vaga.getRequisitos().stream().map(r -> r.getNome()).toList();
		if (requisitos.isEmpty()) {
			return null;
		}
		for (String requisito : requisitos) {
			Integer nivel = niveisPorNome.get(normalizar(requisito));
			if (nivel != null) {
				atendidas.add(requisito);
				somaNiveis += nivel;
			}
		}
		if (atendidas.isEmpty()) {
			return null;
		}
		double score = Math.min(1.0,
				(double) somaNiveis / (NIVEL_MAXIMO * requisitos.size()));
		score = Math.round(score * 100.0) / 100.0;
		List<String> faltantes = requisitos.stream()
				.filter(r -> !atendidas.contains(r))
				.toList();
		return new VagaRecomendadaResponse(vagaService.obter(vaga.getId()), score,
				List.copyOf(atendidas), faltantes, CaminhosLgpd.REVISAO_SOLICITACAO);
	}

	private String normalizar(String nome) {
		return nome.toLowerCase(Locale.ROOT).trim();
	}
}
