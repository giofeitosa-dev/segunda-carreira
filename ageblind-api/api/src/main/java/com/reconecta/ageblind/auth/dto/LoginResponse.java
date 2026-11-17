package com.reconecta.ageblind.auth.dto;

public record LoginResponse(String token, String tokenType, long expiresIn, UsuarioInfo usuario) {

	public record UsuarioInfo(Long id, String email, String papel) {
	}
}
