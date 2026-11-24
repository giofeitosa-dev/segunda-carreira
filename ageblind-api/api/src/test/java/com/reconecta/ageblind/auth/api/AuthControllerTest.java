package com.reconecta.ageblind.auth.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reconecta.ageblind.auth.domain.Papel;
import com.reconecta.ageblind.auth.domain.Usuario;
import com.reconecta.ageblind.auth.repository.UsuarioRepository;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * US1 — cadastro e login com JWT (cenários 1–5 do spec; cenários 6–7 de
 * Bearer em endpoint protegido são cobertos por AutorizacaoMatrizTest no C1.4).
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private JwtDecoder jwtDecoder;

	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	private final ObjectMapper mapper = new ObjectMapper();

	private String cadastrar(String email, String senha) throws Exception {
		return mockMvc.perform(post("/api/v1/auth/cadastro")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}"))
				.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
	}

	// --- US1.1: cadastro cria conta com senha BCrypt ---
	@Test
	void cadastroValidoCriaUsuarioComSenhaBcrypt() throws Exception {
		mockMvc.perform(post("/api/v1/auth/cadastro")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"cadastro-ok@teste.com\",\"senha\":\"senha-forte-123\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.email").value("cadastro-ok@teste.com"))
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.senha").doesNotExist());

		Usuario salvo = usuarioRepository.findByEmail("cadastro-ok@teste.com").orElseThrow();
		// armazenado como hash BCrypt, nunca texto puro
		org.junit.jupiter.api.Assertions.assertTrue(
				passwordEncoder.matches("senha-forte-123", salvo.getSenha()),
				"senha deve ser BCrypt e casar com a digitada");
		org.junit.jupiter.api.Assertions.assertFalse(salvo.getSenha().contains("senha-forte-123"));
		org.junit.jupiter.api.Assertions.assertEquals(Papel.CANDIDATO, salvo.getPapel(),
				"cadastro público só cria CANDIDATO (FR-013)");
	}

	// --- US1.2: email duplicado → 409 problem+json ---
	@Test
	void cadastroEmailDuplicadoDevolve409() throws Exception {
		cadastrar("duplicado@teste.com", "senha-forte-123");

		mockMvc.perform(post("/api/v1/auth/cadastro")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"duplicado@teste.com\",\"senha\":\"outra-senha-999\"}"))
				.andExpect(status().isConflict())
				.andExpect(header().string("Content-Type", "application/problem+json"))
				.andExpect(jsonPath("$.title").value("Conflito"));
	}

	// --- US1.5: campos inválidos → 400 problem+json com violações ---
	@Test
	void cadastroComCamposInvalidosDevolve400() throws Exception {
		mockMvc.perform(post("/api/v1/auth/cadastro")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"nao-e-email\",\"senha\":\"123\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(header().string("Content-Type", "application/problem+json"))
				.andExpect(jsonPath("$.fieldErrors").isArray());
	}

	// --- US1.3: login devolve token HS256 com sub/email/papel/exp ---
	@Test
	void loginValidoDevolveTokenComClaims() throws Exception {
		cadastrar("login-ok@teste.com", "senha-forte-123");

		MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"login-ok@teste.com\",\"senha\":\"senha-forte-123\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.expiresIn").value(1800))
				.andExpect(jsonPath("$.usuario.email").value("login-ok@teste.com"))
				.andExpect(jsonPath("$.usuario.papel").value("CANDIDATO"))
				.andExpect(jsonPath("$.senha").doesNotExist())
				.andReturn();

		JsonNode body = mapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
		String token = body.get("token").asText();

		Jwt jwt = jwtDecoder.decode(token);
		org.junit.jupiter.api.Assertions.assertNotNull(jwt.getSubject(), "sub = usuarioId");
		org.junit.jupiter.api.Assertions.assertEquals("login-ok@teste.com", jwt.getClaimAsString("email"));
		org.junit.jupiter.api.Assertions.assertEquals("CANDIDATO", jwt.getClaimAsString("papel"));
		org.junit.jupiter.api.Assertions.assertNotNull(jwt.getExpiresAt(), "claim exp presente");
	}

	// --- US1.4: senha errada → 401 mensagem única (anti-enumeração) ---
	@Test
	void loginSenhaErradaDevolve401() throws Exception {
		cadastrar("login-errada@teste.com", "senha-forte-123");

		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"login-errada@teste.com\",\"senha\":\"senha-errada-1\"}"))
				.andExpect(status().isUnauthorized())
				.andExpect(header().string("Content-Type", "application/problem+json"))
				.andExpect(jsonPath("$.title").value("Não autenticado"));
	}

	// --- US1.4: usuário inexistente → 401 com MESMA mensagem da senha errada ---
	@Test
	void loginUsuarioInexistenteDevolve401ComMesmaMensagem() throws Exception {
		String corpoInexistente = mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"fantasma@teste.com\",\"senha\":\"senha-errada-1\"}"))
				.andExpect(status().isUnauthorized())
				.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

		cadastrar("login-mesma-msg@teste.com", "senha-forte-123");
		String corpoSenhaErrada = mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"login-mesma-msg@teste.com\",\"senha\":\"senha-errada-1\"}"))
				.andExpect(status().isUnauthorized())
				.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

		org.junit.jupiter.api.Assertions.assertEquals(corpoInexistente, corpoSenhaErrada,
				"resposta 401 idêntica para usuário inexistente e senha errada (US1.4)");
	}

	// --- US1.5: login com campos inválidos → 400 ---
	@Test
	void loginComCamposInvalidosDevolve400() throws Exception {
		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"\",\"senha\":\"\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrors").isArray());
	}
}
