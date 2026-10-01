package com.reconecta.ageblind.escudo.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.reconecta.ageblind.carreira.domain.Vaga;
import com.reconecta.ageblind.carreira.repository.VagaRepository;
import com.reconecta.ageblind.escudo.dominio.MetricaFunil;
import com.reconecta.ageblind.escudo.repository.MetricaFunilRepository;
import com.reconecta.ageblind.shared.enums.FaixaEtaria;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class FunilMetricasControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private VagaRepository vagaRepository;

	@Autowired
	private MetricaFunilRepository metricaRepository;

	private long criarVaga() {
		Vaga vaga = new Vaga();
		vaga.setTitulo("Analista de Logistica");
		vaga.setEmpresa("Reconecta Demo");
		vaga.setArea("Logistica");
		vaga.setDescricao("Vaga publica sem requisito de idade");
		return vagaRepository.save(vaga).getId();
	}

	private void linha(long vagaId, String etapa, FaixaEtaria faixa, int candidatos, int aprovados) {
		MetricaFunil m = new MetricaFunil();
		m.setVagaId(vagaId);
		m.setEtapa(etapa);
		m.setFaixa(faixa);
		m.setCandidatos(candidatos);
		m.setAprovados(aprovados);
		m.setPeriodo("2026-09");
		metricaRepository.save(m);
	}

	@Test
	void metricasComViésDevolveAlertaDeAdverseImpact() throws Exception {
		long vagaId = criarVaga();
		linha(vagaId, "Triagem", FaixaEtaria.ATE_39, 100, 42);
		linha(vagaId, "Triagem", FaixaEtaria.DE_40_A_49, 100, 31);
		linha(vagaId, "Triagem", FaixaEtaria.MAIS_50, 100, 18);

		mockMvc.perform(get("/api/v1/funil/metricas")
						.param("vagaId", String.valueOf(vagaId))
						.param("periodo", "2026-09"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.vagaId").value(vagaId))
				.andExpect(jsonPath("$.etapas[0].nome").value("Triagem"))
				.andExpect(jsonPath("$.etapas[0].aprovacao.ATE_39").value(0.42))
				.andExpect(jsonPath("$.etapas[0].aprovacao['40_49']").value(0.31))
				.andExpect(jsonPath("$.etapas[0].aprovacao['50_MAIS']").value(0.18))
				.andExpect(jsonPath("$.etapas[0].razaoAdverseImpact").value(0.43))
				.andExpect(jsonPath("$.etapas[0].alerta").value(true))
				.andExpect(jsonPath("$.interpretacao").value(
						org.hamcrest.Matchers.containsString("0,43")));
	}

	@Test
	void metricasEquilibradasNaoAlertam() throws Exception {
		long vagaId = criarVaga();
		linha(vagaId, "Entrevista", FaixaEtaria.ATE_39, 50, 10);
		linha(vagaId, "Entrevista", FaixaEtaria.DE_40_A_49, 50, 9);
		linha(vagaId, "Entrevista", FaixaEtaria.MAIS_50, 50, 8);

		mockMvc.perform(get("/api/v1/funil/metricas")
						.param("vagaId", String.valueOf(vagaId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.etapas[0].razaoAdverseImpact").value(0.8))
				.andExpect(jsonPath("$.etapas[0].alerta").value(false))
				.andExpect(jsonPath("$.interpretacao").value(
						org.hamcrest.Matchers.containsString("sem indícios")));
	}

	@Test
	void metricasDeVagaInexistenteDevolve404() throws Exception {
		mockMvc.perform(get("/api/v1/funil/metricas").param("vagaId", "999999"))
				.andExpect(status().isNotFound());
	}

	/**
	 * Regressão anti-viés (Sprint 3, T-03): 40% vs 15% de aprovação → razão
	 * 0,375 (o serviço arredonda para 0,37 pelo limite binário) → alerta true.
	 */
	@Test
	void adverseImpactoExato40vs15PorCentoDisparaAlerta() throws Exception {
		long vagaId = criarVaga();
		linha(vagaId, "Contratacao", FaixaEtaria.ATE_39, 100, 40);
		linha(vagaId, "Contratacao", FaixaEtaria.DE_40_A_49, 100, 30);
		linha(vagaId, "Contratacao", FaixaEtaria.MAIS_50, 100, 15);

		mockMvc.perform(get("/api/v1/funil/metricas")
						.param("vagaId", String.valueOf(vagaId))
						.param("periodo", "2026-09"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.etapas[0].aprovacao.ATE_39").value(0.4))
				.andExpect(jsonPath("$.etapas[0].aprovacao['50_MAIS']").value(0.15))
				.andExpect(jsonPath("$.etapas[0].razaoAdverseImpact").value(0.37))
				.andExpect(jsonPath("$.etapas[0].alerta").value(true))
				.andExpect(jsonPath("$.interpretacao").value(
						org.hamcrest.Matchers.containsString("0,37")));
	}

	@Test
	void metricasSemVagaIdDevolve400() throws Exception {
		mockMvc.perform(get("/api/v1/funil/metricas"))
				.andExpect(status().isBadRequest());
	}
}
