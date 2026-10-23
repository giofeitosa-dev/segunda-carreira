package com.reconecta.ageblind.shared.api;

import org.springframework.http.HttpStatus;

public class BuscaExternaException extends RuntimeException {

	private final HttpStatus status;

	public BuscaExternaException(HttpStatus status, String message) {
		super(message);
		this.status = status;
	}

	public HttpStatus getStatus() {
		return status;
	}
}
