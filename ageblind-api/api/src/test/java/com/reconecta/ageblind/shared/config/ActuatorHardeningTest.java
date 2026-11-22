package com.reconecta.ageblind.shared.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Actuator hardening (spec US3): /actuator/health público em prod; outros
 * endpoints não expostos sem auth.
 */
@SpringBootTest(properties = {
		"spring.profiles.active=prod",
		"spring.datasource.url=jdbc:h2:mem:actuator-test",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
		"app.jwt.secret=meu-secreto-de-teste-com-mais-de-trinta-caracteres"
})
@AutoConfigureMockMvc
class ActuatorHardeningTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void healthPublicoEmProd() throws Exception {
		mockMvc.perform(get("/actuator/health"))
				.andExpect(status().isOk());
	}

	@Test
	void metricsNaoExpostoSemAuthEmProd() throws Exception {
		mockMvc.perform(get("/actuator/metrics"))
				.andExpect(status().is4xxClientError());
	}
}
