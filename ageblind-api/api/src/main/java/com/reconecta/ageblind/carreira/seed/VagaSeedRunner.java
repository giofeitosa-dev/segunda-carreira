package com.reconecta.ageblind.carreira.seed;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;

import com.reconecta.ageblind.carreira.domain.Competencia;
import com.reconecta.ageblind.carreira.domain.Vaga;
import com.reconecta.ageblind.carreira.enums.ModeloVaga;
import com.reconecta.ageblind.carreira.repository.CompetenciaRepository;
import com.reconecta.ageblind.carreira.repository.VagaRepository;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * Seed de vagas a partir de data/vagas-seed.csv (20 vagas, 6 áreas).
 * Ativo apenas com o profile "seed": ./mvnw spring-boot:run -Dspring-boot.run.profiles=seed
 */
@Component
@Profile("seed")
public class VagaSeedRunner implements ApplicationRunner {

	private final VagaRepository vagaRepository;
	private final CompetenciaRepository competenciaRepository;

	public VagaSeedRunner(VagaRepository vagaRepository, CompetenciaRepository competenciaRepository) {
		this.vagaRepository = vagaRepository;
		this.competenciaRepository = competenciaRepository;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		if (vagaRepository.count() > 0) {
			return;
		}
		try (BufferedReader leitor = new BufferedReader(new InputStreamReader(
				new ClassPathResource("data/vagas-seed.csv").getInputStream(), StandardCharsets.UTF_8))) {
			leitor.readLine(); // cabeçalho
			String linha;
			while ((linha = leitor.readLine()) != null && !linha.isBlank()) {
				String[] c = linha.split("\\|", -1);
				Vaga vaga = new Vaga();
				vaga.setTitulo(c[0]);
				vaga.setEmpresa(c[1]);
				vaga.setArea(c[2]);
				vaga.setDescricao(c[3]);
				vaga.setSalario(new BigDecimal(c[4]));
				vaga.setModelo(ModeloVaga.valueOf(c[5]));
				vaga.setPrazoInscricao(LocalDateTime.parse(c[6]));
				Arrays.stream(c[7].split(";"))
						.map(String::trim)
						.filter(nome -> !nome.isEmpty())
						.forEach(nome -> vaga.getRequisitos().add(buscarOuCriar(nome, c[2])));
				vagaRepository.save(vaga);
			}
		}
	}

	private Competencia buscarOuCriar(String nome, String area) {
		return competenciaRepository.findByNomeAndArea(nome, area)
				.orElseGet(() -> competenciaRepository.save(new Competencia(nome, area)));
	}
}
