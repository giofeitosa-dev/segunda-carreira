package com.reconecta.ageblind.carreira.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import com.reconecta.ageblind.carreira.domain.Mentoria;
import com.reconecta.ageblind.carreira.domain.Perfil;
import com.reconecta.ageblind.carreira.repository.MentoriaRepository;
import com.reconecta.ageblind.carreira.repository.PerfilRepository;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class MentoriaControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private MentoriaRepository mentoriaRepository;

	@Autowired
	private PerfilRepository perfilRepository;

	private long criarMentoria(String mentor, String area, String agendamento) {
		Mentoria mentoria = new Mentoria();
		mentoria.setMentor(mentor);
		mentoria.setArea(area);
		mentoria.setDisponibilidade("Semanal");
		mentoria.setAgendamento(agendamento);
		return mentoriaRepository.save(mentoria).getId();
	}

	@Test
	void listarSemFiltroDevolveTudoOrdenadoPorId() throws Exception {
		long id1 = criarMentoria("Paula", "TI", null);
		criarMentoria("Rubens", "Saude", null);

		mockMvc.perform(get("/api/v1/mentorias"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)))
				.andExpect(jsonPath("$[*].id", org.hamcrest.Matchers.hasItem((int) id1)))
				.andExpect(jsonPath("$[*].area").isArray());
	}

	@Test
	void filtrarPorAreaIgnoraCaixa() throws Exception {
		criarMentoria("Ana", "Logistica", null);
		criarMentoria("Bruno", "TI", null);

		mockMvc.perform(get("/api/v1/mentorias").param("area", "LOGISTICA"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].mentor").value("Ana"));
	}

	@Test
	void filtrarPorPerfilIdUsaAreaAlvoE404SeNaoExiste() throws Exception {
		criarMentoria("Carla", "Educacao", "01/10 às 10h");
		criarMentoria("Diego", "TI", null);

		Perfil perfil = new Perfil();
		perfil.setNome("Mentorado");
		perfil.setEmail("ment-" + UUID.randomUUID() + "@recode.demo");
		perfil.setAreaAlvo("Educacao");
		long perfilId = perfilRepository.save(perfil).getId();

		mockMvc.perform(get("/api/v1/mentorias").param("perfilId", String.valueOf(perfilId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].mentor").value("Carla"))
				.andExpect(jsonPath("$[0].agendamento").value("01/10 às 10h"));

		mockMvc.perform(get("/api/v1/mentorias").param("perfilId", "999999"))
				.andExpect(status().isNotFound());
	}
}
