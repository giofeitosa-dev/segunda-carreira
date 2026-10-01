package com.reconecta.ageblind.carreira.api;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class PerfilControllerTest {

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	private String jsonNovoPerfil() throws Exception {
		return """
				{
				  "nome": "João Silva",
				  "email": "joao@example.com",
				  "areaAtual": "PCP",
				  "areaAlvo": "LOGISTICA",
				  "pretensaoSalarial": 4500,
				  "disponibilidade": ["REMOTO"]
				}
				""";
	}

	@Test
	void criaPerfilCom201EHeaderLocation() throws Exception {
		mockMvc.perform(post("/api/v1/perfis")
						.contentType(MediaType.APPLICATION_JSON)
						.content(jsonNovoPerfil()))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", org.hamcrest.Matchers.matchesPattern("/api/v1/perfis/\\d+")))
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.nome").value("João Silva"))
				.andExpect(jsonPath("$.areaAlvo").value("LOGISTICA"))
				.andExpect(jsonPath("$.statusCuradoria").value("EM_CURADORIA"))
				.andExpect(jsonPath("$.dataNascimento").doesNotExist())
				.andExpect(jsonPath("$.idade").doesNotExist());
	}

	@Test
	void rejeitaPerfilInvalidoCom400EFieldErrors() throws Exception {
		mockMvc.perform(post("/api/v1/perfis")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"nome\":\"\", \"email\":\"nao-eh-email\", \"areaAlvo\":\"\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrors").isArray())
				.andExpect(jsonPath("$.fieldErrors", hasSize(org.hamcrest.Matchers.greaterThanOrEqualTo(3))));
	}

	@Test
	void consultaPerfilCriado() throws Exception {
		MvcResult criado = mockMvc.perform(post("/api/v1/perfis")
						.contentType(MediaType.APPLICATION_JSON)
						.content(jsonNovoPerfil()))
				.andExpect(status().isCreated())
				.andReturn();

		String body = criado.getResponse().getContentAsString();
		long id = objectMapper.readTree(body).get("id").asLong();

		mockMvc.perform(get("/api/v1/perfis/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value("joao@example.com"));
	}

	@Test
	void retorna404ProblemDetailParaIdInexistente() throws Exception {
		mockMvc.perform(get("/api/v1/perfis/{id}", 999_999L))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.title").value("Não encontrado"))
				.andExpect(jsonPath("$.detail").isNotEmpty());
	}
}
