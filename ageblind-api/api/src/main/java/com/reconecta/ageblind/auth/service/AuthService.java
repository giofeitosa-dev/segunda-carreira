package com.reconecta.ageblind.auth.service;

import java.time.Instant;
import java.util.Locale;

import com.reconecta.ageblind.auth.domain.Papel;
import com.reconecta.ageblind.auth.domain.Usuario;
import com.reconecta.ageblind.auth.dto.CadastroRequest;
import com.reconecta.ageblind.auth.dto.LoginRequest;
import com.reconecta.ageblind.auth.dto.LoginResponse;
import com.reconecta.ageblind.auth.dto.UsuarioResponse;
import com.reconecta.ageblind.auth.repository.UsuarioRepository;
import com.reconecta.ageblind.shared.api.ConflitoException;
import com.reconecta.ageblind.shared.api.NaoAutenticadoException;
import com.reconecta.ageblind.shared.config.JwtConfig;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cadastro (só CANDIDATO — FR-013) e login com emissão de JWT HS256.
 * Mensagem de 401 única para usuário inexistente e senha errada (US1.4).
 */
@Service
public class AuthService {

	private static final String MENSAGEM_CREDENCIAIS = "Credenciais inválidas";

	private final UsuarioRepository usuarioRepository;
	private final JwtEncoder jwtEncoder;
	private final JwtConfig jwtConfig;

	/**
	 * BCryptPasswordEncoder instanciado diretamente (não é bean): um bean
	 * PasswordEncoder único seria adotado pelo httpBasic legado e quebraria o
	 * `{noop}` do usuário in-memory até o C1.4 remover o Basic.
	 */
	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	public AuthService(UsuarioRepository usuarioRepository,
			JwtEncoder jwtEncoder, JwtConfig jwtConfig) {
		this.usuarioRepository = usuarioRepository;
		this.jwtEncoder = jwtEncoder;
		this.jwtConfig = jwtConfig;
	}

	@Transactional
	public UsuarioResponse cadastrar(CadastroRequest req) {
		String email = normalizar(req.email());
		if (usuarioRepository.existsByEmail(email)) {
			throw new ConflitoException("Email já cadastrado");
		}
		Usuario usuario = new Usuario();
		usuario.setEmail(email);
		usuario.setSenha(passwordEncoder.encode(req.senha()));
		usuario.setPapel(Papel.CANDIDATO);
		Usuario salvo = usuarioRepository.save(usuario);
		return new UsuarioResponse(salvo.getId(), salvo.getEmail());
	}

	@Transactional(readOnly = true)
	public LoginResponse login(LoginRequest req) {
		String email = normalizar(req.email());
		Usuario usuario = usuarioRepository.findByEmail(email)
				.filter(u -> passwordEncoder.matches(req.senha(), u.getSenha()))
				.orElseThrow(() -> new NaoAutenticadoException(MENSAGEM_CREDENCIAIS));
		return new LoginResponse(emitirToken(usuario), "Bearer",
				jwtConfig.getExpirationSegundos(),
				new LoginResponse.UsuarioInfo(usuario.getId(), usuario.getEmail(),
						usuario.getPapel().name()));
	}

	private String emitirToken(Usuario usuario) {
		Instant agora = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuedAt(agora)
				.expiresAt(agora.plusMillis(jwtConfig.getExpirationMs()))
				.subject(usuario.getId().toString())
				.claim("email", usuario.getEmail())
				.claim("papel", usuario.getPapel().name())
				.build();
		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
	}

	private String normalizar(String email) {
		return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
	}
}
