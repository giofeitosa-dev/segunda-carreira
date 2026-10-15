package com.reconecta.ageblind.carreira.seed;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import com.reconecta.ageblind.carreira.domain.Mentoria;
import com.reconecta.ageblind.carreira.repository.MentoriaRepository;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * Seed de mentorias a partir de data/mentorias-seed.csv (rede de apoio por área).
 * Ativo apenas com o profile "seed", junto com os demais seed runners.
 */
@Component
@Profile("seed")
public class MentoriaSeedRunner implements ApplicationRunner {

	private final MentoriaRepository mentoriaRepository;

	public MentoriaSeedRunner(MentoriaRepository mentoriaRepository) {
		this.mentoriaRepository = mentoriaRepository;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		if (mentoriaRepository.count() > 0) {
			return;
		}
		try (BufferedReader leitor = new BufferedReader(new InputStreamReader(
				new ClassPathResource("data/mentorias-seed.csv").getInputStream(), StandardCharsets.UTF_8))) {
			leitor.readLine();
			String linha;
			while ((linha = leitor.readLine()) != null && !linha.isBlank()) {
				String[] c = linha.split("\\|", -1);
				Mentoria mentoria = new Mentoria();
				mentoria.setMentor(c[0]);
				mentoria.setArea(c[1]);
				mentoria.setDisponibilidade(c[2]);
				mentoria.setAgendamento(c[3]);
				mentoriaRepository.save(mentoria);
			}
		}
	}
}
