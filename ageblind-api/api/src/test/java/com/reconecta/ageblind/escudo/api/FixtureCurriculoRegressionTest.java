package com.reconecta.ageblind.escudo.api;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Regressão anti-viés (Sprint 3, T-03): o currículo real do seed
 * (`data/curriculos/sinalizado.txt`) deve sair age-blind da API e a auditoria
 * deve marcar ≥ 3 sinais de idade.
 */
@SpringBootTest
@AutoConfigureMockMvc
class FixtureCurriculoRegressionTest {

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	private String fixture() throws IOException {
		try (InputStream in = getClass().getClassLoader()
				.getResourceAsStream("data/curriculos/sinalizado.txt")) {
			if (in == null) {
				throw new IOException("fixture data/curriculos/sinalizado.txt não encontrada no classpath");
			}
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	private String corpoJson(String curriculo) throws IOException {
		return objectMapper.writeValueAsString(Map.of("curriculo", curriculo));
	}

	@Test
	void fixtureRealGeraVersaoAgeBlindSemProxiesDeIdade() throws Exception {
		String corpo = corpoJson(fixture());

		mockMvc.perform(post("/api/v1/curriculo/age-blind")
						.contentType(MediaType.APPLICATION_JSON)
						.content(corpo))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.regrasAplicadas",
						hasSize(greaterThanOrEqualTo(10))))
				.andExpect(jsonPath("$.conteudo").value(not(containsString("1974"))))
				.andExpect(jsonPath("$.conteudo").value(not(containsString("1998"))))
				.andExpect(jsonPath("$.conteudo").value(not(containsString("Data de nascimento"))))
				.andExpect(jsonPath("$.conteudo").value(not(containsString("25 anos de experiência"))))
				.andExpect(jsonPath("$.conteudo").value(not(containsString("aposentadoria"))))
				.andExpect(jsonPath("$.conteudo").value(not(containsString("veterano"))))
				.andExpect(jsonPath("$.conteudo").value(not(containsString("foto-maria"))))
				.andExpect(jsonPath("$.conteudo").value(not(containsString("idade: 51"))))
				.andExpect(jsonPath("$.conteudo").value(
						containsString("Graduação em Administração")));
	}

	@Test
	void auditoriaDaFixtureRealMarcaAoMenosTresSinais() throws Exception {
		String corpo = corpoJson(fixture());

		mockMvc.perform(post("/api/v1/curriculo/auditoria")
						.contentType(MediaType.APPLICATION_JSON)
						.content(corpo))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.sinaisQueRevelamIdade",
						hasSize(greaterThanOrEqualTo(3))))
				.andExpect(jsonPath("$.scoreAts").isNumber());
	}
}
