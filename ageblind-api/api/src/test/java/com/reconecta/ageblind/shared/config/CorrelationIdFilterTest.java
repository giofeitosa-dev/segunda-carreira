package com.reconecta.ageblind.shared.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Correlation-id (spec US2): header X-Correlation-Id aceito/gerado e
 * propagado na resposta.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CorrelationIdFilterTest {

	@Autowired
	private MockMvc mockMvc;

	private static final Pattern UUID_PATTERN = Pattern.compile(
			"^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");

	@Test
	void headerPresenteEPropagadoNaResposta() throws Exception {
		mockMvc.perform(get("/api/v1/vagas")
				.header("X-Correlation-Id", "meu-id-123"))
				.andExpect(status().isOk())
				.andExpect(header().string("X-Correlation-Id", "meu-id-123"));
	}

	@Test
	void headerAusenteGeraUuidNaResposta() throws Exception {
		String correlationId = mockMvc.perform(get("/api/v1/vagas"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getHeader("X-Correlation-Id");

		assertNotNull(correlationId, "header X-Correlation-Id deve estar presente");
		assertTrue(UUID_PATTERN.matcher(correlationId).matches(),
				"valor deve ser UUID válido: " + correlationId);
	}
}
