package com.reconecta.ageblind.shared.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.UUID;

import com.reconecta.ageblind.carreira.domain.Competencia;
import com.reconecta.ageblind.carreira.domain.Perfil;
import com.reconecta.ageblind.carreira.domain.Vaga;
import com.reconecta.ageblind.carreira.enums.ModeloVaga;
import com.reconecta.ageblind.carreira.repository.CompetenciaRepository;
import com.reconecta.ageblind.carreira.repository.PerfilRepository;
import com.reconecta.ageblind.carreira.repository.VagaRepository;
import com.reconecta.ageblind.escudo.dominio.MetricaFunil;
import com.reconecta.ageblind.escudo.repository.MetricaFunilRepository;
import com.reconecta.ageblind.shared.enums.FaixaEtaria;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Regressão LGPD (Sprint 3, T-03): toda resposta de decisão automatizada
 * (ranking, auditoria, funil) aponta o caminho para revisão humana.
 */
@SpringBootTest
@AutoConfigureMockMvc
class LgpdRevisaoCaminhoTest {

	private static final String CAMINHO = "/api/v1/revisao/solicitacao";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private PerfilRepository perfilRepository;

	@Autowired
	private CompetenciaRepository competenciaRepository;

	@Autowired
	private VagaRepository vagaRepository;

	@Autowired
	private MetricaFunilRepository metricaRepository;

	@Test
	void recomendacaoApontaParaRevisaoEListaCompetenciasFaltantes() throws Exception {
		Competencia requisito = competenciaRepository
				.findByNomeAndArea("Excel", "AreaLGPD")
				.orElseGet(() -> competenciaRepository.save(new Competencia("Excel", "AreaLGPD")));

		Vaga vaga = new Vaga();
		vaga.setTitulo("Vaga LGPD");
		vaga.setEmpresa("Empresa Demo");
		vaga.setArea("AreaLGPD");
		vaga.setDescricao("Vaga demo");
		vaga.setSalario(new BigDecimal("5000"));
		vaga.setModelo(ModeloVaga.REMOTO);
		vaga.getRequisitos().add(requisito);
		vaga = vagaRepository.save(vaga);

		Perfil perfil = new Perfil();
		perfil.setNome("Candidato LGPD");
		perfil.setEmail("lgpd-" + UUID.randomUUID() + "@recode.demo");
		perfil.setAreaAlvo("AreaLGPD");
		perfil.adicionarCompetencia(requisito, 4);
		long perfilId = perfilRepository.save(perfil).getId();

		mockMvc.perform(get("/api/v1/vagas/recomendadas")
						.param("perfilId", String.valueOf(perfilId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].caminhoParaRevisao").value(CAMINHO))
				.andExpect(jsonPath("$[0].competenciasFaltantes").isArray());
	}

	@Test
	void auditoriaDeCurriculoApontaParaRevisao() throws Exception {
		mockMvc.perform(post("/api/v1/curriculo/auditoria")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"curriculo\": \"Data de nascimento: 12/03/1970\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.caminhoParaRevisao").value(CAMINHO));
	}

	@Test
	void metricasDeFunilApontamParaRevisao() throws Exception {
		Vaga vaga = new Vaga();
		vaga.setTitulo("Funil LGPD");
		vaga.setEmpresa("Empresa Demo");
		vaga.setArea("AreaLGPD");
		vaga.setDescricao("Vaga demo");
		long vagaId = vagaRepository.save(vaga).getId();

		MetricaFunil m = new MetricaFunil();
		m.setVagaId(vagaId);
		m.setEtapa("Triagem");
		m.setFaixa(FaixaEtaria.ATE_39);
		m.setCandidatos(10);
		m.setAprovados(5);
		m.setPeriodo("2026-09");
		metricaRepository.save(m);

		mockMvc.perform(get("/api/v1/funil/metricas")
						.param("vagaId", String.valueOf(vagaId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.caminhoParaRevisao").value(CAMINHO));
	}
}
