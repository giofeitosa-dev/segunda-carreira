package com.reconecta.ageblind.shared.api;

/** Erro de aplicação: recurso inexistente (404). */
public class RecursoNaoEncontradoException extends RuntimeException {

	public RecursoNaoEncontradoException(String recurso, Object id) {
		super(recurso + " não encontrado: " + id);
	}
}
