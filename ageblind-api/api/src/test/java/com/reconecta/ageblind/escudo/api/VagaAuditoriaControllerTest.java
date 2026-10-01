package com.reconecta.ageblind.escudo.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class VagaAuditoriaControllerTest {

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void auditarVagaComLimiteEtarioDevolveRiscoAlto() throws Exception {
		String body = objectMapper.writeValueAsString(java.util.Map.of(
				"titulo", "Analista Senior",
				"texto", "Busca-se perfil jovem, nativo digital, com no maximo 30 anos de experiencia"));

		mockMvc.perform(post("/api/v1/vagas/auditoria-linguagem")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.riscoLegal").value("ALTO"))
				.andExpect(jsonPath("$.termosEncontrados").isArray())
				.andExpect(jsonPath("$.termosEncontrados[0]").isString())
				.andExpect(jsonPath("$.baseLegal").value(org.hamcrest.Matchers.containsString("9.029")))
				.andExpect(jsonPath("$.sugestaoRedacao").value(org.hamcrest.Matchers.containsString("compet")));
	}

	@Test
	void auditarVagaNeutraDevolveRiscoBaixo() throws Exception {
		String body = objectMapper.writeValueAsString(java.util.Map.of(
				"titulo", "Analista de Logistica",
				"texto", "Experiencia comprovada em gestao de equipes e processos logisticos. Desejavel dominio de ERP e Power BI."));

		mockMvc.perform(post("/api/v1/vagas/auditoria-linguagem")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.riscoLegal").value("BAIXO"))
				.andExpect(jsonPath("$.termosEncontrados.length()").value(0));
	}

	@Test
	void auditarSemTextoDevolve400() throws Exception {
		mockMvc.perform(post("/api/v1/vagas/auditoria-linguagem")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isBadRequest());
	}
}
