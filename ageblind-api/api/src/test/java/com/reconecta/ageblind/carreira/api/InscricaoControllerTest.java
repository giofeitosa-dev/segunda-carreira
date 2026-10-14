package com.reconecta.ageblind.carreira.api;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reconecta.ageblind.carreira.domain.Perfil;
import com.reconecta.ageblind.carreira.domain.Trilha;
import com.reconecta.ageblind.carreira.domain.Vaga;
import com.reconecta.ageblind.carreira.enums.ModeloVaga;
import com.reconecta.ageblind.carreira.repository.PerfilRepository;
import com.reconecta.ageblind.carreira.repository.TrilhaRepository;
import com.reconecta.ageblind.carreira.repository.VagaRepository;
import com.reconecta.ageblind.carreira.service.LembreteJob;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class InscricaoControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private PerfilRepository perfilRepository;

	@Autowired
	private VagaRepository vagaRepository;

	@Autowired
	private TrilhaRepository trilhaRepository;

	@Autowired
	private LembreteJob lembreteJob;

	private final ObjectMapper objectMapper = new ObjectMapper();

	private long criarPerfil() {
		Perfil perfil = new Perfil();
		perfil.setNome("Inscrito");
		perfil.setEmail("ins-" + UUID.randomUUID() + "@recode.demo");
		return perfilRepository.save(perfil).getId();
	}

	private Vaga criarVagaComPrazo(LocalDateTime prazo) {
		Vaga vaga = new Vaga();
		vaga.setTitulo("Vaga com prazo");
		vaga.setEmpresa("Empresa Demo");
		vaga.setArea("AreaD12");
		vaga.setDescricao("Demo");
		vaga.setSalario(new BigDecimal("4000"));
		vaga.setModelo(ModeloVaga.REMOTO);
		vaga.setPrazoInscricao(prazo);
		return vagaRepository.save(vaga);
	}

	@Test
	void criarInscricaoEmVagaDevolve201ComStatusEnviada() throws Exception {
		long perfilId = criarPerfil();
		Vaga vaga = criarVagaComPrazo(LocalDateTime.now().plusDays(30));
		String body = objectMapper.writeValueAsString(java.util.Map.of(
				"perfilId", perfilId, "tipo", "VAGA", "referenciaId", String.valueOf(vaga.getId())));

		mockMvc.perform(post("/api/v1/inscricoes")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.perfilId").value(perfilId))
				.andExpect(jsonPath("$.tipo").value("VAGA"))
				.andExpect(jsonPath("$.status").value("ENVIADA"))
				.andExpect(jsonPath("$.criadoEm").isNotEmpty());
	}

	@Test
	void criarInscricaoValidacoes400E404() throws Exception {
		mockMvc.perform(post("/api/v1/inscricoes")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isBadRequest());

		String perfilInexistente = objectMapper.writeValueAsString(java.util.Map.of(
				"perfilId", 999999L, "tipo", "VAGA", "referenciaId", "1"));
		mockMvc.perform(post("/api/v1/inscricoes")
						.contentType(MediaType.APPLICATION_JSON)
						.content(perfilInexistente))
				.andExpect(status().isNotFound());

		long perfilId = criarPerfil();
		String vagaInexistente = objectMapper.writeValueAsString(java.util.Map.of(
				"perfilId", perfilId, "tipo", "VAGA", "referenciaId", "999999"));
		mockMvc.perform(post("/api/v1/inscricoes")
						.contentType(MediaType.APPLICATION_JSON)
						.content(vagaInexistente))
				.andExpect(status().isNotFound());

		String referenciaNaoNumerica = objectMapper.writeValueAsString(java.util.Map.of(
				"perfilId", perfilId, "tipo", "VAGA", "referenciaId", "abc"));
		mockMvc.perform(post("/api/v1/inscricoes")
						.contentType(MediaType.APPLICATION_JSON)
						.content(referenciaNaoNumerica))
				.andExpect(status().isBadRequest());
	}

	@Test
	void agendarLembreteDevolve202EJobIdentificaNoPrazoSemRecontar() throws Exception {
		long perfilId = criarPerfil();
		Vaga vaga = criarVagaComPrazo(LocalDateTime.now().plusHours(12));
		String body = objectMapper.writeValueAsString(java.util.Map.of(
				"perfilId", perfilId, "tipo", "VAGA", "referenciaId", String.valueOf(vaga.getId())));
		String resposta = mockMvc.perform(post("/api/v1/inscricoes")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		long inscricaoId = objectMapper.readTree(resposta).get("id").asLong();

		mockMvc.perform(post("/api/v1/webhooks/lembretes")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(java.util.Map.of(
								"inscricaoId", inscricaoId, "antecedenciaHoras", 24))))
				.andExpect(status().isAccepted());

		mockMvc.perform(post("/api/v1/webhooks/lembretes")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"inscricaoId\": 999999}"))
				.andExpect(status().isNotFound());

		mockMvc.perform(post("/api/v1/webhooks/lembretes")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isBadRequest());

		// 405 para método não suportado (antes caía no handler genérico como 500)
		mockMvc.perform(get("/api/v1/perfis"))
				.andExpect(status().isMethodNotAllowed());

		// 400 para corpo JSON malformado
		mockMvc.perform(post("/api/v1/inscricoes")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{invalido"))
				.andExpect(status().isBadRequest());

		// prazo em 12h < antecedência 24h → identificado na janela (uma única vez)
		org.junit.jupiter.api.Assertions.assertEquals(1, lembreteJob.disparar());
		org.junit.jupiter.api.Assertions.assertEquals(0, lembreteJob.disparar(),
				"lembrete já encerrado não deve ser recontado");
	}

	@Test
	void listaInscricoesDoPerfilCom404E400() throws Exception {
		long perfilId = criarPerfil();
		Vaga vaga1 = criarVagaComPrazo(LocalDateTime.now().plusDays(30));
		Vaga vaga2 = criarVagaComPrazo(LocalDateTime.now().plusDays(45));

		for (Vaga vaga : new Vaga[] { vaga1, vaga2 }) {
			String body = objectMapper.writeValueAsString(java.util.Map.of(
					"perfilId", perfilId, "tipo", "VAGA", "referenciaId", String.valueOf(vaga.getId())));
			mockMvc.perform(post("/api/v1/inscricoes")
							.contentType(MediaType.APPLICATION_JSON)
							.content(body))
					.andExpect(status().isCreated());
		}

		mockMvc.perform(get("/api/v1/inscricoes").param("perfilId", String.valueOf(perfilId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].perfilId").value(perfilId))
				.andExpect(jsonPath("$[0].id").isNumber());

		mockMvc.perform(get("/api/v1/inscricoes").param("perfilId", "999999"))
				.andExpect(status().isNotFound());

		mockMvc.perform(get("/api/v1/inscricoes"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void cancelarInscricaoDevolve204ESomeDaListaDepoisDe404() throws Exception {
		long perfilId = criarPerfil();
		Vaga vaga = criarVagaComPrazo(LocalDateTime.now().plusDays(30));
		String body = objectMapper.writeValueAsString(java.util.Map.of(
				"perfilId", perfilId, "tipo", "VAGA", "referenciaId", String.valueOf(vaga.getId())));
		String resposta = mockMvc.perform(post("/api/v1/inscricoes")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		long inscricaoId = objectMapper.readTree(resposta).get("id").asLong();

		mockMvc.perform(delete("/api/v1/inscricoes/{id}", inscricaoId))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/v1/inscricoes").param("perfilId", String.valueOf(perfilId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(0)));

		mockMvc.perform(delete("/api/v1/inscricoes/{id}", inscricaoId))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.title").value("Não encontrado"));
	}
}
