package com.reconecta.ageblind.carreira.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.reconecta.ageblind.carreira.repository.CompetenciaRepository;
import com.reconecta.ageblind.carreira.repository.PerfilRepository;
import com.reconecta.ageblind.carreira.repository.TrilhaRepository;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CompetenciasTransferiveisSimilaridadeTest {

	@Autowired
	private PerfilRepository perfilRepository;
	@Autowired
	private CompetenciaRepository competenciaRepository;
	@Autowired
	private TrilhaRepository trilhaRepository;

	private CompetenciasTransferiveisService servico() {
		return new CompetenciasTransferiveisService(
				perfilRepository, competenciaRepository, trilhaRepository);
	}

	@Test
	void siglaComGrupoDeSinonimosRecebePiso() {
		double s = servico().similaridade("PCP", "Planejamento de Rotas");
		assertTrue(s >= 0.90, "sigla no mesmo grupo deve receber piso, veio " + s);
	}

	@Test
	void palavrasGenericasDoMesmoGrupoNaoRecebemPiso() {
		double s = servico().similaridade("planejamento", "controle");
		assertTrue(s < 0.60,
				"genéricas do mesmo grupo sem sobreposição não podem casar acima do limite, veio " + s);
	}

	@Test
	void termosIguaisContinuamNoTeto() {
		assertEquals(1.0, servico().similaridade("Roteirizacao", "Roteirizacao"), 0.001);
	}
}
