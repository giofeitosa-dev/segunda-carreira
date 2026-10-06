package com.reconecta.ageblind.carreira.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.reconecta.ageblind.carreira.domain.ModuloTrilha;
import com.reconecta.ageblind.carreira.domain.Trilha;
import com.reconecta.ageblind.carreira.repository.TrilhaRepository;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class TrilhaControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TrilhaRepository trilhaRepository;

	private long criarTrilha(String area, String nome) {
		Trilha trilha = new Trilha();
		trilha.setArea(area);
		trilha.setNome(nome);
		trilha.setDuracaoSemanas(8);
		trilha.setCusto(java.math.BigDecimal.ZERO);
		trilha.setInstituicao("SENAI");
		trilha.setCertificado(true);
		ModuloTrilha modulo = new ModuloTrilha();
		modulo.setNome("Fundamentos");
		modulo.setCargaHoraria(12);
		modulo.setOrdem(1);
		modulo.setTrilha(trilha);
		trilha.getModulos().add(modulo);
		return trilhaRepository.save(trilha).getId();
	}

	@Test
	void listaTrilhasDaAreaComModulosEIdString() throws Exception {
		criarTrilha("AreaD11", "Trilha Teste");

		mockMvc.perform(get("/api/v1/trilhas/{area}", "aread11"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").isString())
				.andExpect(jsonPath("$[0].nome").value("Trilha Teste"))
				.andExpect(jsonPath("$[0].certificado").value(true))
				.andExpect(jsonPath("$[0].modulos[0].nome").value("Fundamentos"))
				.andExpect(jsonPath("$[0].modulos[0].cargaHoraria").value(12));
	}

	@Test
	void areaSemTrilhasDevolveListaVazia() throws Exception {
		mockMvc.perform(get("/api/v1/trilhas/{area}", "AreaQueNaoExiste"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(0));
	}
}
