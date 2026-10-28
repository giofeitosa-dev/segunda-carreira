package com.reconecta.ageblind.carreira.api;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
				  "disponibilidade": ["REMOTO"],
				  "telefone": "(11) 99999-0000",
				  "cidade": "São Paulo - SP",
				  "resumo": "Profissional de PCP com 10 anos em chão de fábrica.",
				  "linkedin": "https://linkedin.com/in/joao-silva-demo",
				  "github": "https://github.com/joao-silva-demo"
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
				.andExpect(jsonPath("$.telefone").value("(11) 99999-0000"))
				.andExpect(jsonPath("$.cidade").value("São Paulo - SP"))
				.andExpect(jsonPath("$.resumo").value("Profissional de PCP com 10 anos em chão de fábrica."))
				.andExpect(jsonPath("$.linkedin").value("https://linkedin.com/in/joao-silva-demo"))
				.andExpect(jsonPath("$.github").value("https://github.com/joao-silva-demo"))
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

	@Test
	void atualizaPerfilCom200ESemCamposDeIdade() throws Exception {
		MvcResult criado = mockMvc.perform(post("/api/v1/perfis")
						.contentType(MediaType.APPLICATION_JSON)
						.content(jsonNovoPerfil()))
				.andExpect(status().isCreated())
				.andReturn();
		long id = objectMapper.readTree(criado.getResponse().getContentAsString()).get("id").asLong();

		String atualizacao = """
				{
				  "nome": "Jo\u00e3o Editado",
				  "email": "joao.novo@example.com",
				  "areaAtual": "Produ\u00e7\u00e3o",
				  "areaAlvo": "SUPRIMENTOS",
				  "pretensaoSalarial": 6000,
				  "disponibilidade": ["HIBRIDO", "PRESENCIAL"],
				  "telefone": "(21) 98888-1111",
				  "cidade": "Rio de Janeiro - RJ",
				  "resumo": "Editado: coordena\u00e7\u00e3o de PCP e melhoria cont\u00ednua.",
				  "linkedin": "https://linkedin.com/in/joao-editado",
				  "github": "https://github.com/joao-editado"
				}
				""";
		mockMvc.perform(put("/api/v1/perfis/{id}", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content(atualizacao))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id))
				.andExpect(jsonPath("$.nome").value("Jo\u00e3o Editado"))
				.andExpect(jsonPath("$.areaAlvo").value("SUPRIMENTOS"))
				.andExpect(jsonPath("$.pretensaoSalarial").value(6000))
				.andExpect(jsonPath("$.disponibilidade", hasSize(2)))
				.andExpect(jsonPath("$.telefone").value("(21) 98888-1111"))
				.andExpect(jsonPath("$.cidade").value("Rio de Janeiro - RJ"))
				.andExpect(jsonPath("$.resumo").value("Editado: coordena\u00e7\u00e3o de PCP e melhoria cont\u00ednua."))
				.andExpect(jsonPath("$.linkedin").value("https://linkedin.com/in/joao-editado"))
				.andExpect(jsonPath("$.github").value("https://github.com/joao-editado"))
				.andExpect(jsonPath("$.dataNascimento").doesNotExist())
				.andExpect(jsonPath("$.idade").doesNotExist());

		mockMvc.perform(get("/api/v1/perfis/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nome").value("Jo\u00e3o Editado"));
	}

	@Test
	void atualizarRetorna404InexistenteE400Invalido() throws Exception {
		mockMvc.perform(put("/api/v1/perfis/{id}", 999_999L)
						.contentType(MediaType.APPLICATION_JSON)
						.content(jsonNovoPerfil()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.title").value("Não encontrado"));

		mockMvc.perform(put("/api/v1/perfis/{id}", 999_999L)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"nome\":\"\", \"email\":\"invalido\", \"areaAlvo\":\"\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrors").isArray());
	}
}
