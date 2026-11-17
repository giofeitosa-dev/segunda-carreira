package com.reconecta.ageblind.shared.api;

/** Conflito de negócio (ex.: email já cadastrado) → 409 problem+json. */
public class ConflitoException extends RuntimeException {

	public ConflitoException(String message) {
		super(message);
	}
}
