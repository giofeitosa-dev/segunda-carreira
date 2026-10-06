package com.reconecta.ageblind.shared.dto;

import java.util.List;

/**
 * Página no formato do openapi.yaml (PaginaVaga/Pagina...):
 * campos explícitos em vez da serialização padrão do Spring Data.
 */
public record PaginaResponse<T>(
		List<T> content,
		int page,
		int size,
		long totalElements,
		int totalPages) {
}
