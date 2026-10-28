package com.reconecta.ageblind.shared.api;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.reconecta.ageblind.carreira.domain.Vaga;
import com.reconecta.ageblind.carreira.repository.VagaRepository;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class RhProtegidoTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private VagaRepository vagaRepository;

	@Test
	void funilSemCredencialDevolve401ProblemJson() throws Exception {
		mockMvc.perform(get("/api/v1/funil/metricas").param("vagaId", "1"))
				.andExpect(status().isUnauthorized())
				.andExpect(header().string("WWW-Authenticate", containsString("Basic")))
				.andExpect(jsonPath("$.title").value("Não autenticado"))
				.andExpect(jsonPath("$.status").value(401));
	}

	@Test
	void auditoriaDeLinguagemSemCredencialDevolve401ProblemJson() throws Exception {
		mockMvc.perform(post("/api/v1/vagas/auditoria-linguagem")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"texto\":\"analise de dados\"}"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.title").value("Não autenticado"));
	}

	@Test
	void senhaErradaDevolve401() throws Exception {
		mockMvc.perform(get("/api/v1/funil/metricas")
						.param("vagaId", "1")
						.with(httpBasic("rh", "errada")))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void funilComCredencialRhDevolve200() throws Exception {
		Vaga vaga = new Vaga();
		vaga.setTitulo("Seguranca Test");
		vaga.setEmpresa("Segunda Carreira Demo");
		vaga.setArea("TI");
		vaga.setDescricao("Vaga para validar acesso RH");
		long vagaId = vagaRepository.save(vaga).getId();

		mockMvc.perform(get("/api/v1/funil/metricas")
						.param("vagaId", String.valueOf(vagaId))
						.with(httpBasic("rh", "rh-demo")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.vagaId").value(vagaId));
	}

	@Test
	void endpointsPublicosContinuamAbertosSemCredencial() throws Exception {
		mockMvc.perform(get("/api/v1/vagas").param("page", "0").param("size", "1"))
				.andExpect(status().isOk());
	}
}
