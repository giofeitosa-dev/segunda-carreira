package com.reconecta.ageblind.escudo.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.reconecta.ageblind.carreira.gateway.VagaGateway;
import com.reconecta.ageblind.escudo.dominio.MetricaFunil;
import com.reconecta.ageblind.escudo.dto.EtapaMetrica;
import com.reconecta.ageblind.escudo.dto.MetricasFunilResponse;
import com.reconecta.ageblind.escudo.repository.MetricaFunilRepository;
import com.reconecta.ageblind.shared.api.RecursoNaoEncontradoException;
import com.reconecta.ageblind.shared.enums.FaixaEtaria;

import org.springframework.stereotype.Service;

/**
 * Métricas do funil com teste de adverse impact (razão < 0,80 alerta viés etário).
 * Referência: 08-ROTAS.md rota 22 e 05-ETARISMO.md (dashboard de recorte).
 */
@Service
public class FunilMetricasService {

	static final double LIMITE_ADVERSE_IMPACT = 0.80;

	private final MetricaFunilRepository repository;
	private final VagaGateway vagaGateway;

	public FunilMetricasService(MetricaFunilRepository repository, VagaGateway vagaGateway) {
		this.repository = repository;
		this.vagaGateway = vagaGateway;
	}

	public MetricasFunilResponse metricas(Long vagaId, String periodo) {
		if (!vagaGateway.existe(vagaId)) {
			throw new RecursoNaoEncontradoException("Vaga", vagaId);
		}

		List<MetricaFunil> linhas = (periodo == null || periodo.isBlank())
				? repository.findByVagaIdOrderById(vagaId)
				: repository.findByVagaIdAndPeriodoOrderById(vagaId, periodo);

		if (linhas.isEmpty()) {
			return new MetricasFunilResponse(vagaId, List.of(),
					"Sem métricas para o período informado.");
		}

		Map<String, List<MetricaFunil>> porEtapa = new LinkedHashMap<>();
		linhas.forEach(l -> porEtapa.computeIfAbsent(l.getEtapa(), k -> new ArrayList<>()).add(l));

		List<EtapaMetrica> etapas = porEtapa.entrySet().stream()
				.map(e -> calcularEtapa(e.getKey(), e.getValue()))
				.toList();

		double menorRazao = etapas.stream().mapToDouble(EtapaMetrica::razaoAdverseImpact).min().orElse(1.0);
		String interpretacao = menorRazao < LIMITE_ADVERSE_IMPACT
				? "Razão " + decimal(menorRazao) + " < 0,80 → possível viés etário; investigar."
				: "Menor razão " + decimal(menorRazao) + " ≥ 0,80 → sem indícios de adverse impact.";

		return new MetricasFunilResponse(vagaId, etapas, interpretacao);
	}

	private EtapaMetrica calcularEtapa(String etapa, List<MetricaFunil> linhas) {
		Map<FaixaEtaria, Integer> candidatos = new LinkedHashMap<>();
		Map<FaixaEtaria, Integer> aprovados = new LinkedHashMap<>();
		for (FaixaEtaria f : FaixaEtaria.values()) {
			candidatos.put(f, 0);
			aprovados.put(f, 0);
		}
		for (MetricaFunil l : linhas) {
			candidatos.merge(l.getFaixa(), l.getCandidatos(), Integer::sum);
			aprovados.merge(l.getFaixa(), l.getAprovados(), Integer::sum);
		}

		Map<FaixaEtaria, Double> taxas = new LinkedHashMap<>();
		List<Double> lista = new ArrayList<>();
		for (FaixaEtaria f : FaixaEtaria.values()) {
			int c = candidatos.get(f);
			double taxa = c == 0 ? 0.0 : arredondar(aprovados.get(f) / (double) c);
			taxas.put(f, taxa);
			lista.add(taxa);
		}

		double menor = lista.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
		double maior = lista.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
		double razao = maior == 0.0 ? 1.0 : arredondar(menor / maior);

		return new EtapaMetrica(etapa, taxas, razao, razao < LIMITE_ADVERSE_IMPACT);
	}

	private double arredondar(double valor) {
		return Math.round(valor * 100.0) / 100.0;
	}

	private String decimal(double valor) {
		return String.valueOf(valor).replace('.', ',');
	}
}
