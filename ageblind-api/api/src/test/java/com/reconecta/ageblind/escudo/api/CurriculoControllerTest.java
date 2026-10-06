package com.reconecta.ageblind.escudo.api;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CurriculoControllerTest {

	@Autowired
	private MockMvc mockMvc;

	private static final String CURRICULO = """
			Profissional veterano
			Data de nascimento: 12/03/1968
			Graduação em Administração — 1998
			15+ anos de experiência
			Certificado PMP 2012
			""";

	@Test
	void geraVersaoAgeBlindCom15Regras() throws Exception {
		mockMvc.perform(post("/api/v1/curriculo/age-blind")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"curriculo\": " + escapar(CURRICULO) + "}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tipo").value("AGE_BLIND"))
				.andExpect(jsonPath("$.regrasAplicadas", hasSize(15)))
				.andExpect(jsonPath("$.conteudo").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("1968"))))
				.andExpect(jsonPath("$.conteudo").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("1998"))))
				.andExpect(jsonPath("$.conteudo").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("veterano"))))
				.andExpect(jsonPath("$.conteudo").value(org.hamcrest.Matchers.containsString("Graduação em Administração")));
	}

	@Test
	void rejeitaCurriculoVazio() throws Exception {
		mockMvc.perform(post("/api/v1/curriculo/age-blind")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"curriculo\": \"\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrors[0].field").value("curriculo"));
	}

	private String escapar(String s) {
		return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
	}
}
