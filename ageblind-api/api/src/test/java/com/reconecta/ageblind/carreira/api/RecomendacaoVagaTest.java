package com.reconecta.ageblind.carreira.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reconecta.ageblind.carreira.domain.Competencia;
import com.reconecta.ageblind.carreira.domain.Perfil;
import com.reconecta.ageblind.carreira.domain.Vaga;
import com.reconecta.ageblind.carreira.enums.ModeloVaga;
import com.reconecta.ageblind.carreira.repository.CompetenciaRepository;
import com.reconecta.ageblind.carreira.repository.PerfilRepository;
import com.reconecta.ageblind.carreira.repository.VagaRepository;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class RecomendacaoVagaTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private PerfilRepository perfilRepository;

	@Autowired
	private CompetenciaRepository competenciaRepository;

	@Autowired
	private VagaRepository vagaRepository;

	private final ObjectMapper objectMapper = new ObjectMapper();

	private Vaga criarVaga(String titulo, Competencia... requisitos) {
		Vaga vaga = new Vaga();
		vaga.setTitulo(titulo);
		vaga.setEmpresa("Empresa Demo");
		vaga.setArea("AreaD10");
		vaga.setDescricao("Vaga demo");
		vaga.setSalario(new BigDecimal("5000"));
		vaga.setModelo(ModeloVaga.REMOTO);
		java.util.List.of(requisitos).forEach(r -> vaga.getRequisitos().add(r));
		return vagaRepository.save(vaga);
	}

	private Competencia ouExiste(String nome, String area) {
		return competenciaRepository.findByNomeAndArea(nome, area)
				.orElseGet(() -> competenciaRepository.save(new Competencia(nome, area)));
	}

	/** Perfil com PCP nível 5 e Logística nível 3 (sem qualquer dado de idade). */
	private long criarPerfil() {
		Competencia pcp = ouExiste("PCP", "AreaD10");
		Competencia log = ouExiste("Logistica", "AreaD10");
		ouExiste("Power BI", "AreaD10");

		Perfil perfil = new Perfil();
		perfil.setNome("Candidato 40+");
		perfil.setEmail("rec-" + UUID.randomUUID() + "@recode.demo");
		perfil.setAreaAlvo("AreaD10");
		perfil.adicionarCompetencia(pcp, 5);
		perfil.adicionarCompetencia(log, 3);
		return perfilRepository.save(perfil).getId();
	}

	@Test
	void ranqueiaSomentePorCompetenciasComPesoDeNivel() throws Exception {
		Competencia pcp = ouExiste("PCP", "AreaD10");
		Competencia log = ouExiste("Logistica", "AreaD10");
		Competencia bi = ouExiste("Power BI", "AreaD10");
		long perfilId = criarPerfil();

		criarVaga("Vaga Top", pcp, log);   // score = (5+3)/(5*2) = 0.80
		criarVaga("Vaga Boa", pcp);        // score = 5/5 = 1.00
		criarVaga("Vaga Zero", bi);        // sem casamento → fora

		mockMvc.perform(get("/api/v1/vagas/recomendadas").param("perfilId", String.valueOf(perfilId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].vaga.titulo").value("Vaga Boa"))
				.andExpect(jsonPath("$[0].score").value(1.0))
				.andExpect(jsonPath("$[1].vaga.titulo").value("Vaga Top"))
				.andExpect(jsonPath("$[1].score").value(0.8))
				.andExpect(jsonPath("$[1].competenciasAtendidas[0]").value("Logistica"))
				.andExpect(jsonPath("$[1].competenciasAtendidas[1]").value("PCP"));
	}

	@Test
	void perfilInexistenteDevolve404ESemVagaIdDevolve400() throws Exception {
		mockMvc.perform(get("/api/v1/vagas/recomendadas").param("perfilId", "999999"))
				.andExpect(status().isNotFound());

		mockMvc.perform(get("/api/v1/vagas/recomendadas"))
				.andExpect(status().isBadRequest());
	}

	/**
	 * Regressão anti-viés (Sprint 3, T-03): o ranking não pode depender de
	 * idade/faixa — o modelo nem possui esses campos; aqui garantimos que o
	 * JSON não vaza termos etários e que a ordenação é determinística.
	 */
	@Test
	void rankingSemDadosEtariosEDeterministico() throws Exception {
		long perfilId = criarPerfil();

		MvcResult primeira = mockMvc.perform(get("/api/v1/vagas/recomendadas")
						.param("perfilId", String.valueOf(perfilId))
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		String corpo = primeira.getResponse().getContentAsString();
		String semAcento = corpo.replaceAll("(?i)idade|nascimento|faixa|anos de nascimento", "");
		org.junit.jupiter.api.Assertions.assertEquals(corpo, semAcento,
				"JSON de recomendação não pode conter termos etários");

		MvcResult segunda = mockMvc.perform(get("/api/v1/vagas/recomendadas")
						.param("perfilId", String.valueOf(perfilId)))
				.andReturn();
		org.junit.jupiter.api.Assertions.assertEquals(corpo,
				segunda.getResponse().getContentAsString(),
				"Ranking deve ser determinístico para o mesmo perfil");
	}
}
