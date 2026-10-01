package com.reconecta.ageblind.carreira.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reconecta.ageblind.carreira.domain.Competencia;
import com.reconecta.ageblind.carreira.domain.Perfil;
import com.reconecta.ageblind.carreira.domain.PerfilCompetencia;
import com.reconecta.ageblind.carreira.dto.MapeamentoTransferivel;
import com.reconecta.ageblind.carreira.dto.TransferiveisResponse;
import com.reconecta.ageblind.carreira.repository.CompetenciaRepository;
import com.reconecta.ageblind.carreira.repository.PerfilRepository;
import com.reconecta.ageblind.carreira.repository.TrilhaRepository;
import com.reconecta.ageblind.shared.api.RecursoNaoEncontradoException;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Mapeia experiência antiga para competências da área alvo (R12 do escudo):
 * similaridade por sobreposição de tokens + grupos de sinônimos.
 * Quem tem 40+ não muda de "idade", muda de vocabulário — este serviço
 * traduz um para o outro sem usar nenhum dado pessoal protegido.
 */
@Service
public class CompetenciasTransferiveisService {

	private static final double LIMITE_ACEITACAO = 0.60;
	private static final double TETO_SINONIMO = 0.90;

	private final PerfilRepository perfilRepository;
	private final CompetenciaRepository competenciaRepository;
	private final TrilhaRepository trilhaRepository;
	private final List<List<String>> gruposSinonimos;

	public CompetenciasTransferiveisService(
			PerfilRepository perfilRepository,
			CompetenciaRepository competenciaRepository,
			TrilhaRepository trilhaRepository) {
		this.perfilRepository = perfilRepository;
		this.competenciaRepository = competenciaRepository;
		this.trilhaRepository = trilhaRepository;
		try (InputStream in = new ClassPathResource("carreira/sinonimos.json").getInputStream()) {
			this.gruposSinonimos = new ObjectMapper().readValue(in, new TypeReference<>() { });
		} catch (IOException e) {
			throw new UncheckedIOException("Falha ao carregar sinonimos.json", e);
		}
	}

	@Transactional
	public TransferiveisResponse mapear(Long perfilId) {
		Perfil perfil = perfilRepository.findById(perfilId)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Perfil", perfilId));

		String areaAlvo = perfil.getAreaAlvo() == null ? "" : perfil.getAreaAlvo();
		List<Competencia> catalogoAlvo = areaAlvo.isBlank()
				? List.of()
				: competenciaRepository.findByAreaIgnoreCase(areaAlvo);

		List<MapeamentoTransferivel> mapeamentos = new ArrayList<>();
		Set<String> cobertas = new LinkedHashSet<>();

		for (PerfilCompetencia pc : perfil.getCompetencias()) {
			String de = pc.getCompetencia().getNome();
			Competencia melhor = null;
			double melhorScore = 0.0;
			for (Competencia alvo : catalogoAlvo) {
				double score = similaridade(de, alvo.getNome());
				if (score > melhorScore) {
					melhorScore = score;
					melhor = alvo;
				}
			}
			if (melhor != null && melhorScore >= LIMITE_ACEITACAO) {
				mapeamentos.add(new MapeamentoTransferivel(de, melhor.getNome(), arredondar(melhorScore)));
				cobertas.add(melhor.getNome());
			}
		}

		List<String> gaps = catalogoAlvo.stream()
				.map(Competencia::getNome)
				.filter(nome -> !cobertas.contains(nome))
				.toList();

		String trilhaSugerida = trilhaRepository.findByAreaIgnoreCase(areaAlvo).stream()
				.findFirst()
				.map(t -> t.getNome())
				.orElse("");

		return new TransferiveisResponse(areaAlvo, mapeamentos, gaps, trilhaSugerida);
	}

	/**
	 * Jaccard sobre tokens normalizados; se tokens de grupos de sinônimos
	 * se casam, o piso é 0,90 (vocabulário antigo ≈ vocabulário novo).
	 */
	double similaridade(String a, String b) {
		Set<String> tokensA = tokens(a);
		Set<String> tokensB = tokens(b);
		if (tokensA.isEmpty() || tokensB.isEmpty()) {
			return 0.0;
		}

		Set<String> intersecao = new LinkedHashSet<>(tokensA);
		intersecao.retainAll(tokensB);
		Set<String> uniao = new LinkedHashSet<>(tokensA);
		uniao.addAll(tokensB);
		double jaccard = (double) intersecao.size() / uniao.size();

		if (temSinonimo(tokensA, tokensB)) {
			return Math.max(TETO_SINONIMO, jaccard);
		}
		return jaccard;
	}

	private boolean temSinonimo(Set<String> a, Set<String> b) {
		for (List<String> grupo : gruposSinonimos) {
			boolean emA = a.stream().anyMatch(grupo::contains);
			boolean emB = b.stream().anyMatch(grupo::contains);
			if (emA && emB) {
				return true;
			}
		}
		return false;
	}

	private Set<String> tokens(String texto) {
		String limpo = Normalizer.normalize(texto.toLowerCase(), Normalizer.Form.NFD)
				.replaceAll("\\p{M}", "")
				.replaceAll("[^a-z0-9]+", " ")
				.trim();
		Set<String> tokens = new LinkedHashSet<>();
		if (!limpo.isEmpty()) {
			List.of(limpo.split(" ")).stream().filter(t -> !t.isBlank()).forEach(tokens::add);
		}
		return tokens;
	}

	private double arredondar(double valor) {
		return Math.round(valor * 100.0) / 100.0;
	}
}
