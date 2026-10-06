package com.reconecta.ageblind.escudo.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CurriculoAuditoriaTest {

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void auditarCurriculoComProxiesDevolveSinaisEscoreBaixo() throws Exception {
		String body = objectMapper.writeValueAsString(java.util.Map.of(
				"curriculo", """
						Data de nascimento: 12/03/1968
						Profissional veterano com 15+ anos de experiencia
						Graduacao em Administracao - 1998
						""",
				"vagaAlvo", "Analista de Logistica"));

		mockMvc.perform(post("/api/v1/curriculo/auditoria")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.scoreAts").isNumber())
				.andExpect(jsonPath("$.scoreAts").value(org.hamcrest.Matchers.lessThan(100)))
				.andExpect(jsonPath("$.sinaisQueRevelamIdade.length()").value(org.hamcrest.Matchers.greaterThan(2)))
				.andExpect(jsonPath("$.sinaisQueRevelamIdade[0].sinal").isString())
				.andExpect(jsonPath("$.sinaisQueRevelamIdade[0].localizacao").value("linha 1"))
				.andExpect(jsonPath("$.sinaisQueRevelamIdade[0].severidade").value("ALTA"))
				.andExpect(jsonPath("$.sugestoes").isArray());
	}

	@Test
	void auditarCurriculoLimpoDevolveScoreMaximo() throws Exception {
		String body = objectMapper.writeValueAsString(java.util.Map.of(
				"curriculo", """
						Analista de Logistica
						Certificada em PMP e Six Sigma
						Lideranca de equipes multidisciplinares
						""",
				"vagaAlvo", "Coordenadora de Logistica"));

		mockMvc.perform(post("/api/v1/curriculo/auditoria")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.scoreAts").value(100))
				.andExpect(jsonPath("$.sinaisQueRevelamIdade.length()").value(0));
	}

	@Test
	void auditarSemCurriculoDevolve400() throws Exception {
		mockMvc.perform(post("/api/v1/curriculo/auditoria")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isBadRequest());
	}
}
