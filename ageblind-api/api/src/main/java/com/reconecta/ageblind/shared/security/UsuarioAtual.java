package com.reconecta.ageblind.shared.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Lê o JWT autenticado do SecurityContext (R6): expõe o usuarioId
 * (claim {@code sub}) e o papel (authorities {@code ROLE_*}).
 */
public final class UsuarioAtual {

	private UsuarioAtual() {
	}

	/** Claim {@code sub} do token = usuarioId. */
	public static Long usuarioId() {
		Jwt jwt = jwt();
		try {
			return Long.parseLong(jwt.getSubject());
		} catch (NumberFormatException | NullPointerException e) {
			throw new AccessDeniedException("Token sem sub válido");
		}
	}

	/** Papel via authorities (ROLE_RH) — funciona com token real e de teste. */
	public static boolean ehRh() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null) {
			return false;
		}
		return auth.getAuthorities().stream()
				.anyMatch(a -> "ROLE_RH".equals(a.getAuthority()));
	}

	private static Jwt jwt() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth instanceof JwtAuthenticationToken jwtAuth && jwtAuth.getToken() != null) {
			return jwtAuth.getToken();
		}
		throw new AccessDeniedException("Requisição sem JWT autenticado");
	}
}
