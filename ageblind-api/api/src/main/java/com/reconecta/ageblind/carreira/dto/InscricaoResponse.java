package com.reconecta.ageblind.carreira.dto;

import java.time.LocalDateTime;

import com.reconecta.ageblind.carreira.enums.StatusInscricao;
import com.reconecta.ageblind.carreira.enums.TipoInscricao;

public record InscricaoResponse(
		Long id,
		Long perfilId,
		TipoInscricao tipo,
		StatusInscricao status,
		LocalDateTime criadoEm) {
}
