package com.reconecta.ageblind.carreira.dto;

public record MentoriaResponse(
		Long id,
		String mentor,
		String area,
		String disponibilidade,
		String agendamento) {
}
