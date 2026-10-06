package com.reconecta.ageblind.shared.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

class FaixaEtariaTest {

	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	void serializaComOsCodigosOficiais() throws Exception {
		assertEquals("\"ATE_39\"", mapper.writeValueAsString(FaixaEtaria.ATE_39));
		assertEquals("\"40_49\"", mapper.writeValueAsString(FaixaEtaria.DE_40_A_49));
		assertEquals("\"50_MAIS\"", mapper.writeValueAsString(FaixaEtaria.MAIS_50));
	}

	@Test
	void desserializaCodigoEEnum() throws Exception {
		assertEquals(FaixaEtaria.DE_40_A_49, mapper.readValue("\"40_49\"", FaixaEtaria.class));
		assertEquals(FaixaEtaria.MAIS_50, mapper.readValue("\"50_MAIS\"", FaixaEtaria.class));
		assertEquals(FaixaEtaria.ATE_39, mapper.readValue("\"ATE_39\"", FaixaEtaria.class));
	}

	@Test
	void rejeitaCodigoDesconhecido() {
		assertThrows(IllegalArgumentException.class, () -> FaixaEtaria.fromCodigo("80_90"));
	}
}
