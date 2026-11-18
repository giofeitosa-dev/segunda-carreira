package com.reconecta.ageblind.shared.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.reconecta.ageblind.auth.domain.Papel;
import com.reconecta.ageblind.auth.domain.Usuario;
import com.reconecta.ageblind.auth.repository.UsuarioRepository;

/**
 * Posse de dados pessoais (R5): o dono é o usuário cujo {@code usuario.perfil_id}
 * aponta para o perfil do recurso — resolvido no banco, nunca por claim.
 * Papel RH é bypass. Violação de papel e de posse devolvem o MESMO body de 403
 * (não revela existência do recurso alheio).
 */
@Component
public class PosseChecker {

	private final UsuarioRepository usuarioRepository;

	public PosseChecker(UsuarioRepository usuarioRepository) {
		this.usuarioRepository = usuarioRepository;
	}

	/** Usuário dono do token JWT (banco é a fonte da verdade do vínculo). */
	public Usuario usuarioAtual() {
		return usuarioRepository.findById(UsuarioAtual.usuarioId())
				.orElseThrow(() -> new AccessDeniedException("Sem permissão para este recurso"));
	}

	/** Lança 403 (problem+json) se o usuário atual não for dono nem RH. */
	public void exigirDonoOuRh(Long perfilIdRecurso) {
		if (!donoOuRh(perfilIdRecurso)) {
			throw new AccessDeniedException("Sem permissão para este recurso");
		}
	}

	public boolean donoOuRh(Long perfilIdRecurso) {
		Usuario usuario = usuarioAtual();
		if (usuario.getPapel() == Papel.RH) {
			return true;
		}
		return perfilIdRecurso != null
				&& usuario.getPerfil() != null
				&& perfilIdRecurso.equals(usuario.getPerfil().getId());
	}
}
