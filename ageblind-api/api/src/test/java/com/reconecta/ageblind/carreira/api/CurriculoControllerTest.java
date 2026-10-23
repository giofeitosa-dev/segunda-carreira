package com.reconecta.ageblind.carreira.api;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
class CurriculoControllerTest {

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	private String jsonCurriculo() {
		return """
				{
				  "experiencias": [
				    {"cargo": "Supervisora de Producao", "empresa": "Industria Alfa", "anoInicio": 2016,
				     "anoFim": 2023, "descricao": "Lideranca de equipe"},
				    {"cargo": "Analista de PCP", "empresa": "Metalurgica Sul", "anoInicio": 2011, "descricao": null}
				  ],
				  "formacoes": [
				    {"curso": "Tecnico em Logistica", "instituicao": "Etec Municipal", "anoConclusao": 2019}
				  ],
				  "projetos": [
				    {"nome": "Reducao de perdas", "descricao": "Checklist visual", "link": null}
				  ]
				}
				""";
	}

	private Long criarPerfil() throws Exception {
		MvcResult criado = mockMvc.perform(post("/api/v1/perfis")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"nome": "Ana Curriculo", "email": "ana.curriculo@recode.demo",
								 "areaAtual": "Producao", "areaAlvo": "Logistica"}
								"""))
				.andExpect(status().isCreated())
				.andReturn();
		return objectMapper.readTree(criado.getResponse().getContentAsString()).get("id").asLong();
	}

	@Test
	void subistituiEConsultaCurriculoEstruturado() throws Exception {
		Long id = criarPerfil();

		mockMvc.perform(put("/api/v1/perfis/" + id + "/curriculo")
						.contentType(MediaType.APPLICATION_JSON)
						.content(jsonCurriculo()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.experiencias", hasSize(2)))
				.andExpect(jsonPath("$.formacoes", hasSize(1)))
				.andExpect(jsonPath("$.projetos", hasSize(1)))
				.andExpect(jsonPath("$.experiencias[0].cargo").value("Supervisora de Producao"))
				.andExpect(jsonPath("$.experiencias[0].anoInicio").value(2016))
				.andExpect(jsonPath("$.experiencias[0].anoFim").value(2023))
				.andExpect(jsonPath("$.experiencias[1].anoFim").doesNotExist())
				.andExpect(jsonPath("$.formacoes[0].instituicao").value("Etec Municipal"))
				.andExpect(jsonPath("$.projetos[0].nome").value("Reducao de perdas"))
				.andExpect(jsonPath("$.dataNascimento").doesNotExist())
				.andExpect(jsonPath("$.idade").doesNotExist());

		mockMvc.perform(get("/api/v1/perfis/" + id + "/curriculo"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.experiencias", hasSize(2)))
				.andExpect(jsonPath("$.experiencias[0].anoInicio").value(2016));
	}

	@Test
	void segundoPutSubstituiAEstruturaInteira() throws Exception {
		Long id = criarPerfil();
		mockMvc.perform(put("/api/v1/perfis/" + id + "/curriculo")
						.contentType(MediaType.APPLICATION_JSON)
						.content(jsonCurriculo()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.experiencias", hasSize(2)));

		mockMvc.perform(put("/api/v1/perfis/" + id + "/curriculo")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"experiencias": [{"cargo": "Analista", "empresa": "Nova", "anoInicio": 2020}],
								 "formacoes": [], "projetos": []}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.experiencias", hasSize(1)))
				.andExpect(jsonPath("$.formacoes", hasSize(0)))
				.andExpect(jsonPath("$.projetos", hasSize(0)));
	}

	@Test
	void curriculoDePerfilInexistenteDevolve404() throws Exception {
		mockMvc.perform(get("/api/v1/perfis/99999/curriculo"))
				.andExpect(status().isNotFound());
	}

	@Test
	void itemInvalidoDevolve400ComFieldErrors() throws Exception {
		Long id = criarPerfil();
		mockMvc.perform(put("/api/v1/perfis/" + id + "/curriculo")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"experiencias": [{"cargo": "", "empresa": "", "anoInicio": 2020}],
								 "formacoes": [], "projetos": []}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrors").isArray())
				.andExpect(jsonPath("$.fieldErrors", hasSize(org.hamcrest.Matchers.greaterThanOrEqualTo(2))));
	}

	@Test
	void anoInicioAnteriorA1900Devolve400() throws Exception {
		Long id = criarPerfil();
		mockMvc.perform(put("/api/v1/perfis/" + id + "/curriculo")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"experiencias": [{"cargo": "Auxiliar", "empresa": "Antiga", "anoInicio": 1890}],
								 "formacoes": [], "projetos": []}
								"""))
				.andExpect(status().isBadRequest());
	}
}
