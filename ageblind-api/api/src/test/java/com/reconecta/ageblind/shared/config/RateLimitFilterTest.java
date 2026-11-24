package com.reconecta.ageblind.shared.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Rate limit em /api/v1/auth/** (R2 / US4.5): 10 requisições por janela
 * passam; a 11ª recebe 429 problem+json.
 */
@SpringBootTest(properties = {
		"app.ratelimit.auth.limite=10",
		"app.ratelimit.auth.janela-segundos=60"
})
@AutoConfigureMockMvc
class RateLimitFilterTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void dezRequisicoesPassamEAOnzesimaRecebe429() throws Exception {
		for (int i = 0; i < 10; i++) {
			mockMvc.perform(post("/api/v1/auth/login")
					.contentType("application/json")
					.content("{\"email\":\"x@y.com\",\"senha\":\"errada123\"}"))
					.andExpect(status().is4xxClientError());
		}
		mockMvc.perform(post("/api/v1/auth/login")
				.contentType("application/json")
				.content("{\"email\":\"x@y.com\",\"senha\":\"errada123\"}"))
				.andExpect(status().isTooManyRequests());
	}
}
