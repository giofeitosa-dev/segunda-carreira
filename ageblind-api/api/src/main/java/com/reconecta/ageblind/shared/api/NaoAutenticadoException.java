package com.reconecta.ageblind.shared.api;

/**
 * Falha de autenticação (credenciais inválidas) → 401 problem+json.
 * Mensagem única para usuário inexistente e senha errada (anti-enumeração, US1.4).
 */
public class NaoAutenticadoException extends RuntimeException {

	public NaoAutenticadoException(String message) {
		super(message);
	}
}
