package com.reconecta.ageblind.carreira.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import com.reconecta.ageblind.carreira.domain.Competencia;
import com.reconecta.ageblind.carreira.domain.Vaga;
import com.reconecta.ageblind.carreira.enums.ModeloVaga;
import com.reconecta.ageblind.carreira.repository.CompetenciaRepository;
import com.reconecta.ageblind.carreira.repository.VagaRepository;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class VagaControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private VagaRepository vagaRepository;

	@Autowired
	private CompetenciaRepository competenciaRepository;

	private Vaga criarVaga(String titulo, String area, ModeloVaga modelo) {
		Vaga vaga = new Vaga();
		vaga.setTitulo(titulo);
		vaga.setEmpresa("Empresa Demo");
		vaga.setArea(area);
		vaga.setDescricao("Descricao da vaga sem requisito de idade");
		vaga.setSalario(new BigDecimal("5000"));
		vaga.setModelo(modelo);
		return vagaRepository.save(vaga);
	}

	@Test
	void listarSemFiltroDevolvePaginaNoFormatoDoContrato() throws Exception {
		mockMvc.perform(get("/api/v1/vagas"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.page").value(0))
				.andExpect(jsonPath("$.size").value(20))
				.andExpect(jsonPath("$.totalElements").isNumber())
				.andExpect(jsonPath("$.totalPages").isNumber());
	}

	@Test
	void listarFiltraPorAreaEModelo() throws Exception {
		criarVaga("Vaga Log A", "AreaD09-Log", ModeloVaga.REMOTO);
		criarVaga("Vaga Log B", "AreaD09-Log", ModeloVaga.PRESENCIAL);
		criarVaga("Vaga Outra", "AreaD09-Admin", ModeloVaga.REMOTO);

		mockMvc.perform(get("/api/v1/vagas").param("area", "AREAD09-LOG"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalElements").value(2));

		mockMvc.perform(get("/api/v1/vagas").param("area", "AreaD09-Log")
						.param("modelo", "REMOTO"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalElements").value(1))
				.andExpect(jsonPath("$.content[0].titulo").value("Vaga Log A"));
	}

	@Test
	void paginarComSizeMaximoDoContrato() throws Exception {
		criarVaga("Pag 1", "AreaD09-Pag", ModeloVaga.HIBRIDO);
		criarVaga("Pag 2", "AreaD09-Pag", ModeloVaga.HIBRIDO);
		criarVaga("Pag 3", "AreaD09-Pag", ModeloVaga.HIBRIDO);

		mockMvc.perform(get("/api/v1/vagas").param("area", "AreaD09-Pag")
						.param("page", "0").param("size", "2"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()").value(2))
				.andExpect(jsonPath("$.totalElements").value(3))
				.andExpect(jsonPath("$.totalPages").value(2));
	}

	@Test
	void detalheDevolveRequisitosEDepois404() throws Exception {
		Competencia roteiro = competenciaRepository.save(new Competencia("Roteirizacao", "AreaD09-Detalhe"));
		Competencia log = competenciaRepository.save(new Competencia("Logistica", "AreaD09-Detalhe"));
		Vaga vaga = criarVaga("Vaga Detalhe", "AreaD09-Detalhe", ModeloVaga.REMOTO);
		vaga.getRequisitos().add(roteiro);
		vaga.getRequisitos().add(log);
		vagaRepository.save(vaga);

		mockMvc.perform(get("/api/v1/vagas/{id}", vaga.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.titulo").value("Vaga Detalhe"))
				.andExpect(jsonPath("$.requisitos[0]").value("Logistica"))
				.andExpect(jsonPath("$.requisitos[1]").value("Roteirizacao"));

		mockMvc.perform(get("/api/v1/vagas/{id}", 999999L))
				.andExpect(status().isNotFound());
	}

	@Test
	void modeloInvalidoDevolve400() throws Exception {
		mockMvc.perform(get("/api/v1/vagas").param("modelo", "QUALQUER"))
				.andExpect(status().isBadRequest());
	}

	/** Sprint 3, matriz item 4 (erro): página negativa → 400 RFC 7807 (não 500). */
	@Test
	void paginaNegativaDevolve400ProblemDetail() throws Exception {
		mockMvc.perform(get("/api/v1/vagas").param("page", "-1"))
				.andExpect(status().isBadRequest())
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
						.contentType(org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON));
	}
}
