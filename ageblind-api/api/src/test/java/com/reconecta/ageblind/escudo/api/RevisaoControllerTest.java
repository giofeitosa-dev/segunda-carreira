package com.reconecta.ageblind.escudo.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reconecta.ageblind.carreira.domain.Perfil;
import com.reconecta.ageblind.carreira.repository.PerfilRepository;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import com.reconecta.ageblind.shared.security.JwtTestSupport;

@SpringBootTest
@AutoConfigureMockMvc
class RevisaoControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private PerfilRepository perfilRepository;

	@Autowired
	private JwtTestSupport jwt;

	private final ObjectMapper objectMapper = new ObjectMapper();

	private long criarPerfil() {
		Perfil perfil = new Perfil();
		perfil.setNome("Titular LGPD");
		perfil.setEmail("lgpd-" + UUID.randomUUID() + "@recode.demo");
		return perfilRepository.save(perfil).getId();
	}

	@Test
	void solicitarRevisaoDevolve201ComProtocoloEPendente() throws Exception {
		long perfilId = criarPerfil();
		RequestPostProcessor dono = jwt.jwtCandidato(perfilId);
		String body = objectMapper.writeValueAsString(java.util.Map.of(
				"perfilId", perfilId,
				"motivo", "Decisão automatizada não considerou minhas certificações recentes"));

		mockMvc.perform(post("/api/v1/revisao/solicitacao")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body)
						.with(dono))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.perfilId").value(perfilId))
				.andExpect(jsonPath("$.status").value("PENDENTE"))
				.andExpect(jsonPath("$.protocolo").value(
						org.hamcrest.Matchers.startsWith("REV-")))
				.andExpect(jsonPath("$.criadoEm").isNotEmpty());
	}

	@Test
	void solicitarSemMotivoDevolve400ComFieldErrors() throws Exception {
		RequestPostProcessor candidato = jwt.jwtCandidato(null);
		String body = objectMapper.writeValueAsString(java.util.Map.of("perfilId", 1L));

		mockMvc.perform(post("/api/v1/revisao/solicitacao")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body)
						.with(candidato))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrors[0].field").value("motivo"));
	}

	@Test
	void solicitarParaPerfilInexistenteDevolve404() throws Exception {
		RequestPostProcessor rh = jwt.jwtRh();
		String body = objectMapper.writeValueAsString(java.util.Map.of(
				"perfilId", 999999L,
				"motivo", "Revisão solicitada"));

		mockMvc.perform(post("/api/v1/revisao/solicitacao")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body)
						.with(rh))
				.andExpect(status().isNotFound());
	}
}
