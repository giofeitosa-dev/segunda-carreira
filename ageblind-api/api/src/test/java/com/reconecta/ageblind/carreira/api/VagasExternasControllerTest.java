package com.reconecta.ageblind.carreira.api;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.reconecta.ageblind.carreira.dto.VagaExternaResponse;
import com.reconecta.ageblind.carreira.gateway.AdzunaGateway;
import com.reconecta.ageblind.shared.config.AdzunaProperties;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class VagasExternasControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private AdzunaProperties properties;

	@MockitoBean
	private AdzunaGateway adzunaGateway;

	@Test
	void semChaveDoProvedorDevolve503() throws Exception {
		properties.setAppId("");
		properties.setAppKey("");

		mockMvc.perform(get("/api/v1/vagas/busca-externa").param("termo", "desenvolvedor"))
				.andExpect(status().isServiceUnavailable())
				.andExpect(jsonPath("$.title").value("Busca externa indisponível"));
	}

	@Test
	void termoComMenosDeDoisCaracteresDevolve400() throws Exception {
		properties.setAppId("app");
		properties.setAppKey("key");

		mockMvc.perform(get("/api/v1/vagas/busca-externa").param("termo", "x"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void paginaMenorQueUmDevolve400() throws Exception {
		properties.setAppId("app");
		properties.setAppKey("key");

		mockMvc.perform(get("/api/v1/vagas/busca-externa")
						.param("termo", "java")
						.param("pagina", "0"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void comChaveDevolveVagasMapeadasDoProvedor() throws Exception {
		properties.setAppId("app");
		properties.setAppKey("key");
		VagaExternaResponse vaga = new VagaExternaResponse("491974345", "Desenvolvedor Java",
				"Empresa Exemplo", "São Paulo", "https://www.adzuna.com/br/job/491974345",
				"2026-10-20T09:00:00.000Z", new BigDecimal("4500.0"), new BigDecimal("7000.0"),
				"Vaga sem requisito de idade", "Adzuna");
		org.mockito.Mockito.when(adzunaGateway.buscar(eq("desenvolvedor"), eq("São Paulo"), anyInt()))
				.thenReturn(new AdzunaGateway.RespostaAdzuna(1234, List.of(vaga)));

		mockMvc.perform(get("/api/v1/vagas/busca-externa")
						.param("termo", "desenvolvedor")
						.param("local", "São Paulo"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.termo").value("desenvolvedor"))
				.andExpect(jsonPath("$.local").value("São Paulo"))
				.andExpect(jsonPath("$.pagina").value(1))
				.andExpect(jsonPath("$.totalElementos").value(1234))
				.andExpect(jsonPath("$.content[0].titulo").value("Desenvolvedor Java"))
				.andExpect(jsonPath("$.content[0].empresa").value("Empresa Exemplo"))
				.andExpect(jsonPath("$.content[0].fonte").value("Adzuna"))
				.andExpect(jsonPath("$.content[0].idade").doesNotExist());
	}

	@Test
	void falhaDoProvedorDevolve502() throws Exception {
		properties.setAppId("app");
		properties.setAppKey("key");
		org.mockito.Mockito.when(adzunaGateway.buscar(anyString(), anyString(), anyInt()))
				.thenThrow(new com.reconecta.ageblind.shared.api.BuscaExternaException(
						HttpStatus.BAD_GATEWAY, "Falha ao consultar o provedor de vagas externas"));

		mockMvc.perform(get("/api/v1/vagas/busca-externa").param("termo", "analista"))
				.andExpect(status().isBadGateway())
				.andExpect(jsonPath("$.title").value("Busca externa indisponível"));
	}
}
