package com.reconecta.ageblind.carreira.gateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reconecta.ageblind.carreira.dto.VagaExternaResponse;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

class AdzunaGatewayTest {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	@Test
	void mapeiaRespostaRealDoAdzunaParaONossoDto() throws Exception {
		JsonNode corpo = MAPPER.readTree("""
				{
				  "count": 1234,
				  "results": [
				    {
				      "id": "491974345",
				      "title": "Desenvolvedor Java Pleno",
				      "description": "Vaga para desenvolvimento backend",
				      "redirect_url": "https://www.adzuna.com/br/job/491974345",
				      "company": {"display_name": "Empresa Exemplo LTDA"},
				      "location": {"display_name": "São Paulo, São Paulo"},
				      "created": "2026-10-20T09:00:00.000Z",
				      "salary_min": 4500.0,
				      "salary_max": 7000.0
				    },
				    {
				      "id": "491974346",
				      "title": "Analista de Dados",
				      "company": {"display_name": "Dados SA"},
				      "location": {"display_name": "Remoto"},
				      "redirect_url": "https://www.adzuna.com/br/job/491974346",
				      "salary_min": 0,
				      "salary_max": null
				    }
				  ]
				}
				""");

		AdzunaGateway.RespostaAdzuna resposta = AdzunaGateway.mapear(corpo);

		assertEquals(1234, resposta.totalElementos());
		List<VagaExternaResponse> vagas = resposta.vagas();
		assertEquals(2, vagas.size());

		VagaExternaResponse primeira = vagas.get(0);
		assertEquals("491974345", primeira.id());
		assertEquals("Desenvolvedor Java Pleno", primeira.titulo());
		assertEquals("Empresa Exemplo LTDA", primeira.empresa());
		assertEquals("São Paulo, São Paulo", primeira.local());
		assertEquals("https://www.adzuna.com/br/job/491974345", primeira.link());
		assertEquals(new BigDecimal("4500.0"), primeira.salarioMinimo());
		assertEquals(new BigDecimal("7000.0"), primeira.salarioMaximo());
		assertEquals("Adzuna", primeira.fonte());

		VagaExternaResponse segunda = vagas.get(1);
		assertEquals("Analista de Dados", segunda.titulo());
		assertNull(segunda.salarioMinimo());
		assertNull(segunda.salarioMaximo());
		assertNull(segunda.descricao());
	}

	@Test
	void corpoInvalidoDevolveListaVazia() {
		AdzunaGateway.RespostaAdzuna resposta = AdzunaGateway.mapear(null);
		assertTrue(resposta.vagas().isEmpty());
		assertEquals(0, resposta.totalElementos());
	}
}
