package com.reconecta.ageblind.shared.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.reconecta.ageblind.carreira.repository.PerfilRepository;
import com.reconecta.ageblind.auth.repository.AuditoriaSegurancaRepository;
import com.reconecta.ageblind.shared.security.JwtTestSupport;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Auditoria de segurança (spec US1): todo acesso a dado pessoal gera registro
 * em auditoria_seguranca — criação/atualização de perfil, acesso negado (403)
 * e endpoint público (sem registro ou usuario_id null).
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuditoriaAspectTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtTestSupport jwt;

	@Autowired
	private PerfilRepository perfilRepository;

	@Autowired
	private AuditoriaSegurancaRepository auditoriaRepository;

	private long criarPerfil() {
		return perfilRepository.save(perfil("Matriz Teste", "matriz@recode.demo")).getId();
	}

	private String corpoPerfil() {
		return """
				{"nome": "Auditoria Teste", "email": "auditoria@recode.demo",
				 "areaAtual": "Producao", "areaAlvo": "Logistica"}
				""";
	}

	private com.reconecta.ageblind.carreira.domain.Perfil perfil(String nome, String email) {
		com.reconecta.ageblind.carreira.domain.Perfil p = new com.reconecta.ageblind.carreira.domain.Perfil();
		p.setNome(nome);
		p.setEmail(email);
		p.setAreaAtual("Producao");
		p.setAreaAlvo("Logistica");
		return p;
	}

	@Test
	void criarPerfilGeraRegistroDeAuditoria() throws Exception {
		long countAntes = auditoriaRepository.count();

		mockMvc.perform(post("/api/v1/perfis")
				.with(jwt.jwtCandidato(criarPerfil()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(corpoPerfil()))
				.andExpect(status().isCreated());

		List<?> registros = auditoriaRepository.findAll();
		assertRegistrosAumentaram(countAntes, registros);
		assertAcaoPresente(registros, "PERFIL_CRIADO");
		assertUsuarioIdPresente(registros);
	}

	@Test
	void acessoNegadoGeraRegistroDeAuditoria() throws Exception {
		long countAntes = auditoriaRepository.count();
		long perfilDono = criarPerfil();
		long perfilAlheio = criarPerfil();

		// Token vinculado ao perfilDono, acessando perfilAlheio (não é dono)
		mockMvc.perform(get("/api/v1/perfis/" + perfilAlheio)
				.with(jwt.jwtCandidato(perfilDono)))
				.andExpect(status().isForbidden());

		List<?> registros = auditoriaRepository.findAll();
		assertRegistrosAumentaram(countAntes, registros);
		assertAcaoPresente(registros, "ACESSO_NEGADO");
	}

	@Test
	void endpointPublicoNaoGeraRegistroDeAuditoria() throws Exception {
		long countAntes = auditoriaRepository.count();

		mockMvc.perform(get("/api/v1/vagas"))
				.andExpect(status().isOk());

		List<?> registros = auditoriaRepository.findAll();
		assertRegistrosNaoAumentaram(countAntes, registros);
	}

	private void assertRegistrosAumentaram(long countAntes, List<?> registros) {
		assert registros.size() > (int) countAntes :
				"esperava novo registro de auditoria, antes=" + countAntes + " depois=" + registros.size();
	}

	private void assertRegistrosNaoAumentaram(long countAntes, List<?> registros) {
		assert registros.size() == (int) countAntes :
				"esperava nenhum novo registro de auditoria, antes=" + countAntes + " depois=" + registros.size();
	}

	private void assertAcaoPresente(List<?> registros, String acao) {
		boolean encontrado = registros.stream()
				.anyMatch(r -> r.toString().contains(acao));
		assert encontrado : "esperava registro com acao=" + acao + " em " + registros;
	}

	private void assertUsuarioIdPresente(List<?> registros) {
		boolean encontrado = registros.stream()
				.anyMatch(r -> r.toString().contains("usuarioId"));
		assert encontrado : "esperava registro com usuarioId preenchido em " + registros;
	}
}
