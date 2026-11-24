package com.reconecta.ageblind.shared.api;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reconecta.ageblind.carreira.domain.Perfil;
import com.reconecta.ageblind.carreira.domain.Vaga;
import com.reconecta.ageblind.carreira.enums.ModeloVaga;
import com.reconecta.ageblind.carreira.repository.PerfilRepository;
import com.reconecta.ageblind.carreira.repository.VagaRepository;
import com.reconecta.ageblind.shared.security.JwtTestSupport;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * Matriz de acesso da spec US2 (SC-001/SC-002): 401 anônimo em dado pessoal,
 * leitura pública aberta, 403 de papel, 403 de posse A→B, RH completo,
 * webhook só dono/RH e CANDIDATO sem perfil → 403 documentado.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AutorizacaoMatrizTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtTestSupport jwt;

	@Autowired
	private PerfilRepository perfilRepository;

	@Autowired
	private VagaRepository vagaRepository;

	private final ObjectMapper objectMapper = new ObjectMapper();

	private long criarPerfil(String nome) {
		Perfil perfil = new Perfil();
		perfil.setNome(nome);
		perfil.setEmail("matriz-" + UUID.randomUUID() + "@recode.demo");
		return perfilRepository.save(perfil).getId();
	}

	private long criarVaga() {
		Vaga vaga = new Vaga();
		vaga.setTitulo("Vaga da Matriz");
		vaga.setEmpresa("Segunda Carreira Demo");
		vaga.setArea("TI");
		vaga.setDescricao("Vaga para validar a matriz de autorizacao");
		vaga.setSalario(new BigDecimal("5000"));
		vaga.setModelo(ModeloVaga.REMOTO);
		return vagaRepository.save(vaga).getId();
	}

	private String corpoPerfil() {
		return """
				{"nome": "Matriz Teste", "email": "matriz@recode.demo",
				 "areaAtual": "Producao", "areaAlvo": "Logistica"}
				""";
	}

	private String corpoInscricao(long perfilId, long vagaId) throws Exception {
		return objectMapper.writeValueAsString(java.util.Map.of(
				"perfilId", perfilId, "tipo", "VAGA", "referenciaId", String.valueOf(vagaId)));
	}

	private String corpoRevisao(long perfilId) throws Exception {
		return objectMapper.writeValueAsString(java.util.Map.of(
				"perfilId", perfilId, "motivo", "Pedido da matriz de autorizacao"));
	}

	private String corpoLembrete(long inscricaoId) throws Exception {
		return objectMapper.writeValueAsString(java.util.Map.of(
				"inscricaoId", inscricaoId, "antecedenciaHoras", 24));
	}

	@Test
	void anonimoRecebe401BearerEmTodoDadoPessoal() throws Exception {
		mockMvc.perform(get("/api/v1/perfis/1"))
				.andExpect(status().isUnauthorized())
				.andExpect(header().string("WWW-Authenticate", containsString("Bearer")))
				.andExpect(header().string("WWW-Authenticate", containsString("invalid_token")))
				.andExpect(jsonPath("$.title").value("Não autenticado"))
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.detail").value("Token ausente, expirado ou inválido"));

		mockMvc.perform(post("/api/v1/perfis")
						.contentType(MediaType.APPLICATION_JSON)
						.content(corpoPerfil()))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(post("/api/v1/inscricoes")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(get("/api/v1/inscricoes").param("perfilId", "1"))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(delete("/api/v1/inscricoes/1"))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(post("/api/v1/webhooks/lembretes")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"inscricaoId\": 1}"))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(post("/api/v1/curriculo/age-blind")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"curriculo\": \"texto\"}"))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(post("/api/v1/revisao/solicitacao")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"perfilId\": 1, \"motivo\": \"x\"}"))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(get("/api/v1/perfis/1/curriculo"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void leituraPublicaAbertaSemToken() throws Exception {
		mockMvc.perform(get("/api/v1/vagas").param("page", "0").param("size", "1"))
				.andExpect(status().isOk());
		mockMvc.perform(get("/api/v1/trilhas/aread11"))
				.andExpect(status().isOk());
		mockMvc.perform(get("/api/v1/mentorias"))
				.andExpect(status().isOk());
		mockMvc.perform(get("/actuator/health"))
				.andExpect(status().isOk());
	}

	@Test
	void candidatoRecebe403NosRecursosDeRh() throws Exception {
		RequestPostProcessor candidato = jwt.jwtCandidato(null);

		mockMvc.perform(get("/api/v1/funil/metricas")
						.param("vagaId", "1")
						.with(candidato))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.title").value("Acesso negado"))
				.andExpect(jsonPath("$.status").value(403))
				.andExpect(jsonPath("$.detail").value("Sem permissão para este recurso"));

		mockMvc.perform(post("/api/v1/vagas/auditoria-linguagem")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"texto\": \"analise de dados\"}")
						.with(candidato))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.title").value("Acesso negado"));
	}

	@Test
	void candidatoAcessoAosRecursosDeOutroDonoRecebe403() throws Exception {
		long perfilA = criarPerfil("Dono A");
		long perfilB = criarPerfil("Dono B");
		long vagaId = criarVaga();
		RequestPostProcessor tokenA = jwt.jwtCandidato(perfilA);
		RequestPostProcessor tokenB = jwt.jwtCandidato(perfilB);

		mockMvc.perform(get("/api/v1/perfis/{id}", perfilB).with(tokenA))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.title").value("Acesso negado"));

		mockMvc.perform(put("/api/v1/perfis/{id}", perfilB)
						.contentType(MediaType.APPLICATION_JSON)
						.content(corpoPerfil())
						.with(tokenA))
				.andExpect(status().isForbidden());

		mockMvc.perform(get("/api/v1/perfis/{id}/curriculo", perfilB).with(tokenA))
				.andExpect(status().isForbidden());

		mockMvc.perform(get("/api/v1/inscricoes")
						.param("perfilId", String.valueOf(perfilB))
						.with(tokenA))
				.andExpect(status().isForbidden());

		mockMvc.perform(post("/api/v1/inscricoes")
						.contentType(MediaType.APPLICATION_JSON)
						.content(corpoInscricao(perfilB, vagaId))
						.with(tokenA))
				.andExpect(status().isForbidden());

		mockMvc.perform(post("/api/v1/revisao/solicitacao")
						.contentType(MediaType.APPLICATION_JSON)
						.content(corpoRevisao(perfilB))
						.with(tokenA))
				.andExpect(status().isForbidden());

		String inscricaoJson = mockMvc.perform(post("/api/v1/inscricoes")
						.contentType(MediaType.APPLICATION_JSON)
						.content(corpoInscricao(perfilB, vagaId))
						.with(tokenB))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		long inscricaoId = objectMapper.readTree(inscricaoJson).get("id").asLong();

		mockMvc.perform(post("/api/v1/webhooks/lembretes")
						.contentType(MediaType.APPLICATION_JSON)
						.content(corpoLembrete(inscricaoId))
						.with(tokenA))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.title").value("Acesso negado"));

		mockMvc.perform(post("/api/v1/webhooks/lembretes")
						.contentType(MediaType.APPLICATION_JSON)
						.content(corpoLembrete(inscricaoId))
						.with(tokenB))
				.andExpect(status().isAccepted());

		mockMvc.perform(post("/api/v1/webhooks/lembretes")
						.contentType(MediaType.APPLICATION_JSON)
						.content(corpoLembrete(inscricaoId))
						.with(jwt.jwtRh()))
				.andExpect(status().isAccepted());
	}

	@Test
	void rhAcessaRecursosDeRhEDadosDeQualquerDono() throws Exception {
		long vagaId = criarVaga();
		long perfilB = criarPerfil("Consulta RH");
		RequestPostProcessor rh = jwt.jwtRh();

		mockMvc.perform(get("/api/v1/funil/metricas")
						.param("vagaId", String.valueOf(vagaId))
						.with(rh))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.vagaId").value(vagaId));

		mockMvc.perform(post("/api/v1/vagas/auditoria-linguagem")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"texto\": \"analise de dados\"}")
						.with(rh))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.riscoLegal").isNotEmpty());

		mockMvc.perform(get("/api/v1/perfis/{id}", perfilB).with(rh))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(perfilB));

		mockMvc.perform(get("/api/v1/perfis/{id}", 999_999L).with(rh))
				.andExpect(status().isNotFound());

		mockMvc.perform(post("/api/v1/revisao/solicitacao")
						.contentType(MediaType.APPLICATION_JSON)
						.content(corpoRevisao(perfilB))
						.with(rh))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.perfilId").value(perfilB));

		mockMvc.perform(post("/api/v1/revisao/solicitacao")
						.contentType(MediaType.APPLICATION_JSON)
						.content(corpoRevisao(999_999L))
						.with(rh))
				.andExpect(status().isNotFound());
	}

	@Test
	void candidatoSemPerfilVinculadoRecebe403Documentado() throws Exception {
		long perfilExistente = criarPerfil("Perfil com dono");
		RequestPostProcessor semPerfil = jwt.jwtCandidato(null);

		mockMvc.perform(get("/api/v1/perfis/{id}", perfilExistente).with(semPerfil))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.title").value("Acesso negado"))
				.andExpect(jsonPath("$.detail").value("Sem permissão para este recurso"));

		mockMvc.perform(get("/api/v1/inscricoes")
						.param("perfilId", String.valueOf(perfilExistente))
						.with(semPerfil))
				.andExpect(status().isForbidden());
	}

	@Test
	void candidatoDonoAcessaSeuProprioPerfilComAutoVinculo() throws Exception {
		RequestPostProcessor candidato = jwt.jwtCandidato(null);

		String criado = mockMvc.perform(post("/api/v1/perfis")
						.contentType(MediaType.APPLICATION_JSON)
						.content(corpoPerfil())
						.with(candidato))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		long id = objectMapper.readTree(criado).get("id").asLong();

		mockMvc.perform(get("/api/v1/perfis/{id}", id).with(candidato))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id));

		mockMvc.perform(put("/api/v1/perfis/{id}", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content(corpoPerfil())
						.with(candidato))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/v1/perfis/{id}/curriculo", id).with(candidato))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/v1/revisao/solicitacao")
						.contentType(MediaType.APPLICATION_JSON)
						.content(corpoRevisao(id))
						.with(candidato))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.perfilId").value(id));
	}
}
