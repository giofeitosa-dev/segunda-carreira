package com.reconecta.ageblind.shared.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.reconecta.ageblind.shared.security.JwtTestSupport;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Endpoint de auditoria (spec US1): GET /api/v1/auditoria só RH.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuditoriaControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtTestSupport jwt;

	@Test
	void rhListaAuditoriaRecebe200() throws Exception {
		mockMvc.perform(get("/api/v1/auditoria")
				.with(jwt.jwtRh()))
				.andExpect(status().isOk());
	}

	@Test
	void candidatoListaAuditoriaRecebe403() throws Exception {
		mockMvc.perform(get("/api/v1/auditoria")
				.with(jwt.jwtCandidato(null)))
				.andExpect(status().isForbidden());
	}

	@Test
	void anonimoListaAuditoriaRecebe401() throws Exception {
		mockMvc.perform(get("/api/v1/auditoria"))
				.andExpect(status().isUnauthorized());
	}
}
