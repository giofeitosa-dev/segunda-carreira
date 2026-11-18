package com.reconecta.ageblind.shared.api;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import com.reconecta.ageblind.carreira.domain.Vaga;
import com.reconecta.ageblind.carreira.enums.ModeloVaga;
import com.reconecta.ageblind.carreira.repository.VagaRepository;
import com.reconecta.ageblind.shared.security.JwtTestSupport;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * Painel RH protegido por JWT (FR-008): sem token → 401 problem+json com
 * WWW-Authenticate Bearer; token inválido → 401; candidato → 403; RH → 200;
 * leitura pública continua aberta. Substitui o antigo HTTP Basic.
 */
@SpringBootTest
@AutoConfigureMockMvc
class RhProtegidoTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private VagaRepository vagaRepository;

	@Autowired
	private JwtTestSupport jwt;

	private long criarVaga() {
		Vaga vaga = new Vaga();
		vaga.setTitulo("Seguranca Test");
		vaga.setEmpresa("Segunda Carreira Demo");
		vaga.setArea("TI");
		vaga.setDescricao("Vaga para validar acesso RH");
		vaga.setSalario(new BigDecimal("5000"));
		vaga.setModelo(ModeloVaga.REMOTO);
		return vagaRepository.save(vaga).getId();
	}

	@Test
	void funilSemTokenDevolve401BearerProblemJson() throws Exception {
		mockMvc.perform(get("/api/v1/funil/metricas").param("vagaId", "1"))
				.andExpect(status().isUnauthorized())
				.andExpect(header().string("WWW-Authenticate", containsString("Bearer")))
				.andExpect(jsonPath("$.title").value("Não autenticado"))
				.andExpect(jsonPath("$.status").value(401));
	}

	@Test
	void auditoriaDeLinguagemSemTokenDevolve401ProblemJson() throws Exception {
		mockMvc.perform(post("/api/v1/vagas/auditoria-linguagem")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"texto\":\"analise de dados\"}"))
				.andExpect(status().isUnauthorized())
				.andExpect(header().string("WWW-Authenticate", containsString("Bearer")))
				.andExpect(jsonPath("$.title").value("Não autenticado"));
	}

	@Test
	void tokenInvalidoDevolve401InvalidToken() throws Exception {
		mockMvc.perform(get("/api/v1/funil/metricas")
						.param("vagaId", "1")
						.header("Authorization", "Bearer token-invalido.mau.assinatura"))
				.andExpect(status().isUnauthorized())
				.andExpect(header().string("WWW-Authenticate", containsString("invalid_token")))
				.andExpect(jsonPath("$.title").value("Não autenticado"));
	}

	@Test
	void loginComSenhaErradaDevolve401MensagemUnica() throws Exception {
		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\": \"rh@demo.com\", \"senha\": \"senha-errada\"}"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.title").value("Não autenticado"));
	}

	@Test
	void candidatoNoFunilDevolve403() throws Exception {
		mockMvc.perform(get("/api/v1/funil/metricas")
						.param("vagaId", "1")
						.with(jwt.jwtCandidato(null)))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.title").value("Acesso negado"));
	}

	@Test
	void funilComTokenRhDevolve200() throws Exception {
		long vagaId = criarVaga();
		RequestPostProcessor rh = jwt.jwtRh();

		mockMvc.perform(get("/api/v1/funil/metricas")
						.param("vagaId", String.valueOf(vagaId))
						.with(rh))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.vagaId").value(vagaId));

		mockMvc.perform(post("/api/v1/vagas/auditoria-linguagem")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"texto\":\"analise de dados\"}")
						.with(rh))
				.andExpect(status().isOk());
	}

	@Test
	void endpointsPublicosContinuamAbertosSemToken() throws Exception {
		mockMvc.perform(get("/api/v1/vagas").param("page", "0").param("size", "1"))
				.andExpect(status().isOk());
	}
}
