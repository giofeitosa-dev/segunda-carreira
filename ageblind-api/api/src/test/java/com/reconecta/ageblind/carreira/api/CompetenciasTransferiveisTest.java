package com.reconecta.ageblind.carreira.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.UUID;

import com.reconecta.ageblind.carreira.domain.Competencia;
import com.reconecta.ageblind.carreira.domain.Perfil;
import com.reconecta.ageblind.carreira.domain.Trilha;
import com.reconecta.ageblind.carreira.repository.CompetenciaRepository;
import com.reconecta.ageblind.carreira.repository.PerfilRepository;
import com.reconecta.ageblind.carreira.repository.TrilhaRepository;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CompetenciasTransferiveisTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private PerfilRepository perfilRepository;

	@Autowired
	private CompetenciaRepository competenciaRepository;

	@Autowired
	private TrilhaRepository trilhaRepository;

	/** Cenário: profissional de Produção quer migrar para Logística. */
	private long criarCenario() {
		Competencia pcp = competenciaRepository.save(new Competencia("PCP", "Producao"));
		Competencia rotas = competenciaRepository.save(new Competencia("Planejamento de Rotas", "Logistica"));
		competenciaRepository.save(new Competencia("Roteirizacao", "Logistica"));
		competenciaRepository.save(new Competencia("Power BI", "Logistica"));

		Trilha trilha = new Trilha();
		trilha.setArea("Logistica");
		trilha.setNome("Logistica Pratica");
		trilha.setDuracaoSemanas(8);
		trilha.setCusto(BigDecimal.ZERO);
		trilha.setInstituicao("Reconecta");
		trilha.setCertificado(true);
		trilhaRepository.save(trilha);

		Perfil perfil = new Perfil();
		perfil.setNome("Migrador");
		perfil.setEmail("migr-" + UUID.randomUUID() + "@recode.demo");
		perfil.setAreaAtual("Producao");
		perfil.setAreaAlvo("Logistica");
		perfil.adicionarCompetencia(pcp, 4);
		return perfilRepository.save(perfil).getId();
	}

	@Test
	void mapeiaExperienciaAntigaParaAreaAlvo() throws Exception {
		long perfilId = criarCenario();

		mockMvc.perform(post("/api/v1/perfis/{id}/competencias-transferiveis", perfilId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.areaAlvo").value("Logistica"))
				.andExpect(jsonPath("$.competenciasTransferiveis[0].de").value("PCP"))
				.andExpect(jsonPath("$.competenciasTransferiveis[0].para").value("Planejamento de Rotas"))
				.andExpect(jsonPath("$.competenciasTransferiveis[0].similaridade").value(0.9))
				.andExpect(jsonPath("$.gaps").isArray())
				.andExpect(jsonPath("$.gaps.length()").value(2))
				.andExpect(jsonPath("$.trilhaSugerida").value("Logistica Pratica"));
	}

	@Test
	void perfilInexistenteDevolve404() throws Exception {
		mockMvc.perform(post("/api/v1/perfis/{id}/competencias-transferiveis", 999999L))
				.andExpect(status().isNotFound());
	}
}
