package com.reconecta.ageblind.carreira.dto;

import java.util.List;

public record TransferiveisResponse(
		String areaAlvo,
		List<MapeamentoTransferivel> competenciasTransferiveis,
		List<String> gaps,
		String trilhaSugerida) {
}
