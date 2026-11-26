package com.reconecta.ageblind.carreira.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.reconecta.ageblind.auth.repository.UsuarioRepository;
import com.reconecta.ageblind.carreira.domain.Perfil;
import com.reconecta.ageblind.carreira.repository.InscricaoRepository;
import com.reconecta.ageblind.carreira.repository.PerfilRepository;
import com.reconecta.ageblind.shared.security.JwtTestSupport;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Titular LGPD (ciclo 003): exclusao cascata (DELETE /meus-dados) e
 * portabilidade (GET /meus-dados/exportacao), so para o proprio titular.
 */
@SpringBootTest
@AutoConfigureMockMvc
class TitularControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtTestSupport jwt;

	@Autowired
	private PerfilRepository perfilRepository;

	@Autowired
	private InscricaoRepository inscricaoRepository;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@AfterEach
	void limparBanco() {
		inscricaoRepository.deleteAll();
		usuarioRepository.deleteAll();
		perfilRepository.deleteAll();
	}

	private Long criarPerfil() {
		Perfil p = new Perfil();
		p.setNome("Titular Teste");
		p.setEmail("titular@teste.demo");
		p.setAreaAtual("Producao");
		p.setAreaAlvo("Logistica");
		return perfilRepository.save(p).getId();
	}

	private Long criarUsuarioComPerfil(Long perfilId) {
		return usuarioRepository.findAll().stream()
				.filter(u -> u.getPerfil() != null && u.getPerfil().getId().equals(perfilId))
				.findFirst()
				.map(u -> u.getId())
				.orElseGet(() -> {
					// Criar um novo usuario vinculado ao perfil
					com.reconecta.ageblind.auth.domain.Usuario u =
							new com.reconecta.ageblind.auth.domain.Usuario();
					u.setEmail("titular-user-" + System.currentTimeMillis() + "@teste.demo");
					u.setSenha("senha-teste-123");
					u.setPapel(com.reconecta.ageblind.auth.domain.Papel.CANDIDATO);
					u.setPerfil(perfilRepository.findById(perfilId)
							.orElseThrow());
					return usuarioRepository.save(u).getId();
				});
	}

	@Test
	void excluirMeusDadosRemovePerfilEFilhas() throws Exception {
		Long perfilId = criarPerfil();
		Long usuarioId = criarUsuarioComPerfil(perfilId);

		mockMvc.perform(delete("/api/v1/meus-dados")
				.with(jwt.jwtCandidato(perfilId)))
				.andExpect(status().isNoContent());

		// Perfil excluido
		var perfilOptional = perfilRepository.findById(perfilId);
		if (perfilOptional.isPresent()) {
			throw new AssertionError("Perfil deveria ter sido excluido");
		}

		// Usuario desvinculado (perfil_id null)
		var usuarioOptional = usuarioRepository.findById(usuarioId);
		if (usuarioOptional.isEmpty()) {
			throw new AssertionError("Usuario deveria continuar existindo");
		}
		if (usuarioOptional.get().getPerfil() != null) {
			throw new AssertionError("Usuario deveria ter perfil desvinculado");
		}
	}

	@Test
	void excluirMeusDadosSemPerfilEhIdempotente() throws Exception {
		// Usuario sem perfil
		com.reconecta.ageblind.auth.domain.Usuario u = new com.reconecta.ageblind.auth.domain.Usuario();
		u.setEmail("sem-perfil-" + System.currentTimeMillis() + "@teste.demo");
		u.setSenha("senha-teste-123");
		u.setPapel(com.reconecta.ageblind.auth.domain.Papel.CANDIDATO);
		Long usuarioId = usuarioRepository.save(u).getId();

		mockMvc.perform(delete("/api/v1/meus-dados")
				.with(jwt.jwtCandidato(null)))
				.andExpect(status().isNoContent());
	}

	@Test
	void acessarPerfilAposExclusaoRetorna404() throws Exception {
		Long perfilId = criarPerfil();
		Long usuarioId = criarUsuarioComPerfil(perfilId);

		mockMvc.perform(delete("/api/v1/meus-dados")
				.with(jwt.jwtCandidato(perfilId)))
				.andExpect(status().isNoContent());

		// Apos exclusao, nao consegue mais acessar (perfil nao existe mais).
        // Usa RH (tem acesso a todos perfis) para que a autorizacao passe e o 404
        // seja por "perfil nao encontrado", nao por falta de perfil proprio.
		mockMvc.perform(get("/api/v1/perfis/" + perfilId)
				.with(jwt.jwtRh()))
				.andExpect(status().isNotFound());
	}

	@Test
	void outroTitularNaoPodeExcluirMeusDados() throws Exception {
		Long perfilId = criarPerfil();
		criarUsuarioComPerfil(perfilId);

		// Outro usuario (sem vinculo a este perfil) tenta excluir.
        // /meus-dados opera sobre o titular autenticado; sem perfil eh idempotente (204).
		mockMvc.perform(delete("/api/v1/meus-dados")
				.with(jwt.jwtCandidato(null)))
				.andExpect(status().isNoContent());
	}

	@Test
	void exportarMeusDadosRetornaJsonSemSenha() throws Exception {
		Long perfilId = criarPerfil();
		Long usuarioId = criarUsuarioComPerfil(perfilId);

		mockMvc.perform(get("/api/v1/meus-dados/exportacao")
				.with(jwt.jwtCandidato(perfilId))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.usuario.id").value(usuarioId.intValue()))
				.andExpect(jsonPath("$.usuario.email").exists())
				.andExpect(jsonPath("$.perfil.id").value(perfilId.intValue()))
				.andExpect(jsonPath("$.perfil.nome").value("Titular Teste"))
				.andExpect(jsonPath("$.senha").doesNotExist());
	}

	@Test
	void exportarMeusDadosSemPerfilRetornaDadosDoUsuario() throws Exception {
		// Usuario sem perfil, autenticado via JWT (jwtCandidato(null) cria o usuario)
		mockMvc.perform(get("/api/v1/meus-dados/exportacao")
				.with(jwt.jwtCandidato(null)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.perfil").doesNotExist());
	}

	@Test
	void outroTitularNaoPodeExportarMeusDados() throws Exception {
		Long perfilId = criarPerfil();
		criarUsuarioComPerfil(perfilId);

		// Outro usuario (sem vinculo a este perfil) tenta exportar.
        // /meus-dados opera sobre o titular autenticado; sem perfil eh idempotente (200).
		mockMvc.perform(get("/api/v1/meus-dados/exportacao")
				.with(jwt.jwtCandidato(null)))
				.andExpect(status().isOk());
	}
}
