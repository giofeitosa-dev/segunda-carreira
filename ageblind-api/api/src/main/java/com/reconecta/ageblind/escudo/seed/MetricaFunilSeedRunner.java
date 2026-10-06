package com.reconecta.ageblind.escudo.seed;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import com.reconecta.ageblind.escudo.dominio.MetricaFunil;
import com.reconecta.ageblind.escudo.repository.MetricaFunilRepository;
import com.reconecta.ageblind.shared.enums.FaixaEtaria;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * Seed de métricas de funil a partir de data/metricas-seed.csv
 * (vaga 1, 2 etapas enviesadas → adverse impact disparado no demo).
 * Ativo apenas com o profile "seed".
 */
@Component
@Profile("seed")
public class MetricaFunilSeedRunner implements ApplicationRunner {

	private final MetricaFunilRepository metricaFunilRepository;

	public MetricaFunilSeedRunner(MetricaFunilRepository metricaFunilRepository) {
		this.metricaFunilRepository = metricaFunilRepository;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		if (metricaFunilRepository.count() > 0) {
			return;
		}
		try (BufferedReader leitor = new BufferedReader(new InputStreamReader(
				new ClassPathResource("data/metricas-seed.csv").getInputStream(), StandardCharsets.UTF_8))) {
			leitor.readLine();
			String linha;
			while ((linha = leitor.readLine()) != null && !linha.isBlank()) {
				String[] c = linha.split("\\|", -1);
				MetricaFunil m = new MetricaFunil();
				m.setVagaId(Long.parseLong(c[0]));
				m.setEtapa(c[1]);
				m.setFaixa(FaixaEtaria.valueOf(c[2]));
				m.setCandidatos(Integer.parseInt(c[3]));
				m.setAprovados(Integer.parseInt(c[4]));
				m.setPeriodo(c[5]);
				metricaFunilRepository.save(m);
			}
		}
	}
}
