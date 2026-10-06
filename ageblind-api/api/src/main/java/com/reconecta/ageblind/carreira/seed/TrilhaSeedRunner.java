package com.reconecta.ageblind.carreira.seed;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import com.reconecta.ageblind.carreira.domain.ModuloTrilha;
import com.reconecta.ageblind.carreira.domain.Trilha;
import com.reconecta.ageblind.carreira.repository.TrilhaRepository;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * Seed de trilhas a partir de data/trilhas-seed.csv (3 trilhas + módulos).
 * Ativo apenas com o profile "seed", junto com o VagaSeedRunner.
 */
@Component
@Profile("seed")
public class TrilhaSeedRunner implements ApplicationRunner {

	private final TrilhaRepository trilhaRepository;

	public TrilhaSeedRunner(TrilhaRepository trilhaRepository) {
		this.trilhaRepository = trilhaRepository;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		if (trilhaRepository.count() > 0) {
			return;
		}
		try (BufferedReader leitor = new BufferedReader(new InputStreamReader(
				new ClassPathResource("data/trilhas-seed.csv").getInputStream(), StandardCharsets.UTF_8))) {
			leitor.readLine();
			String linha;
			while ((linha = leitor.readLine()) != null && !linha.isBlank()) {
				String[] c = linha.split("\\|", -1);
				Trilha trilha = new Trilha();
				trilha.setArea(c[0]);
				trilha.setNome(c[1]);
				trilha.setDuracaoSemanas(Integer.parseInt(c[2]));
				trilha.setCusto(new BigDecimal(c[3]));
				trilha.setInstituicao(c[4]);
				trilha.setCertificado(Boolean.parseBoolean(c[5]));
				int ordem = 1;
				for (String modulo : Arrays.asList(c[6].split(";"))) {
					String[] partes = modulo.trim().split(":");
					ModuloTrilha m = new ModuloTrilha();
					m.setNome(partes[0].trim());
					m.setCargaHoraria(Integer.parseInt(partes[1].trim()));
					m.setOrdem(ordem++);
					m.setTrilha(trilha);
					trilha.getModulos().add(m);
				}
				trilhaRepository.save(trilha);
			}
		}
	}
}
