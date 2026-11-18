package com.reconecta.ageblind.shared.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/**
 * CORS allowlist (R9 / US4.4): origem na lista passa no preflight; origem fora
 * da lista é bloqueada.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CorsConfigTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void origemNaAllowlistPassaNoPreflight() throws Exception {
		mockMvc.perform(options("/api/v1/auth/login")
				.header("Origin", "http://localhost:3000")
				.header("Access-Control-Request-Method", "POST")
				.header("Access-Control-Request-Headers", "Authorization,Content-Type"))
				.andExpect(status().isOk())
				.andExpect(header().exists("Access-Control-Allow-Origin"));
	}

	@Test
	void origemForaDaAllowlistBloqueada() throws Exception {
		mockMvc.perform(options("/api/v1/auth/login")
				.header("Origin", "http://site-malicioso.com")
				.header("Access-Control-Request-Method", "POST")
				.header("Access-Control-Request-Headers", "Authorization,Content-Type"))
				.andExpect(status().isForbidden());
	}
}
