package com.reconecta.ageblind.carreira.seed;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.reconecta.ageblind.carreira.repository.MentoriaRepository;
import com.reconecta.ageblind.carreira.repository.PerfilRepository;
import com.reconecta.ageblind.carreira.repository.TrilhaRepository;
import com.reconecta.ageblind.carreira.repository.VagaRepository;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/** D-20 — o seed demo completo carrega com o profile "seed". */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:seed_d20")
@ActiveProfiles("seed")
class SeedDemoTest {

	@Autowired
	private PerfilRepository perfilRepository;

	@Autowired
	private VagaRepository vagaRepository;

	@Autowired
	private TrilhaRepository trilhaRepository;

	@Autowired
	private MentoriaRepository mentoriaRepository;

	@Test
	@Transactional
	void seedDemoCarregaPerfisVagasTrilhasEMentorias() {
		assertTrue(perfilRepository.count() >= 10,
				"esperava >= 10 perfis, veio " + perfilRepository.count());
		assertTrue(vagaRepository.count() >= 20,
				"esperava >= 20 vagas, veio " + vagaRepository.count());
		assertTrue(trilhaRepository.count() >= 5,
				"esperava >= 5 trilhas, veio " + trilhaRepository.count());
		assertTrue(mentoriaRepository.count() >= 5,
				"esperava >= 5 mentorias, veio " + mentoriaRepository.count());

		boolean algumComCompetencias = perfilRepository.findAll().stream()
				.anyMatch(p -> !p.getCompetencias().isEmpty());
		assertTrue(algumComCompetencias, "pelo menos um perfil com competências vinculadas");

		boolean algumComCurriculo = perfilRepository.findAll().stream()
				.anyMatch(p -> !p.getExperiencias().isEmpty()
						&& !p.getFormacoes().isEmpty()
						&& !p.getProjetos().isEmpty());
		assertTrue(algumComCurriculo, "pelo menos um perfil com currículo estruturado (seed)");

		assertFalse(perfilRepository.findAll().stream().anyMatch(p -> p.getNome() == null));
	}
}
