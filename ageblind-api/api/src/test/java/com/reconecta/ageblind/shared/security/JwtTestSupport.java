package com.reconecta.ageblind.shared.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import java.util.UUID;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.reconecta.ageblind.auth.domain.Papel;
import com.reconecta.ageblind.auth.domain.Usuario;
import com.reconecta.ageblind.auth.repository.UsuarioRepository;
import com.reconecta.ageblind.carreira.domain.Perfil;
import com.reconecta.ageblind.carreira.repository.PerfilRepository;

/**
 * Tokens JWT de teste (R6): cria o Usuario no banco (a posse é resolvida por
 * usuario.perfil_id, nunca por claim) e devolve o post-processor com as
 * claims sub/email/papel + authority ROLE_*.
 */
@Component
public class JwtTestSupport {

	private final UsuarioRepository usuarioRepository;
	private final PerfilRepository perfilRepository;

	public JwtTestSupport(UsuarioRepository usuarioRepository, PerfilRepository perfilRepository) {
		this.usuarioRepository = usuarioRepository;
		this.perfilRepository = perfilRepository;
	}

	/** CANDIDATO vinculado ao perfil; {@code perfilId null} = sem vínculo. */
	public RequestPostProcessor jwtCandidato(Long perfilId) {
		return tokenDe(novoUsuario(Papel.CANDIDATO, perfilId));
	}

	public RequestPostProcessor jwtRh() {
		return tokenDe(novoUsuario(Papel.RH, null));
	}

	private Usuario novoUsuario(Papel papel, Long perfilId) {
		Usuario usuario = new Usuario();
		usuario.setEmail(papel.name().toLowerCase() + "-" + UUID.randomUUID() + "@teste.demo");
		usuario.setSenha("senha-teste-123");
		usuario.setPapel(papel);
		if (perfilId != null) {
			Perfil perfil = perfilRepository.findById(perfilId)
					.orElseThrow(() -> new IllegalArgumentException(
							"Perfil " + perfilId + " não existe para vincular o token"));
			usuario.setPerfil(perfil);
		}
		return usuarioRepository.save(usuario);
	}

	private RequestPostProcessor tokenDe(Usuario usuario) {
		String papel = usuario.getPapel().name();
		return jwt()
				.jwt(builder -> builder
						.subject(String.valueOf(usuario.getId()))
						.claim("email", usuario.getEmail())
						.claim("papel", papel))
				.authorities(new SimpleGrantedAuthority("ROLE_" + papel));
	}
}
