package com.reconecta.ageblind.carreira.service;

import java.util.Comparator;
import java.util.List;

import com.reconecta.ageblind.carreira.domain.Trilha;
import com.reconecta.ageblind.carreira.dto.ModuloTrilhaResponse;
import com.reconecta.ageblind.carreira.dto.TrilhaResponse;
import com.reconecta.ageblind.carreira.repository.TrilhaRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrilhaService {

	private final TrilhaRepository trilhaRepository;

	public TrilhaService(TrilhaRepository trilhaRepository) {
		this.trilhaRepository = trilhaRepository;
	}

	@Transactional
	public List<TrilhaResponse> listarPorArea(String area) {
		return trilhaRepository.findByAreaIgnoreCase(area).stream()
				.sorted(Comparator.comparing(Trilha::getId))
				.map(this::paraResponse)
				.toList();
	}

	private TrilhaResponse paraResponse(Trilha trilha) {
		List<ModuloTrilhaResponse> modulos = trilha.getModulos().stream()
				.sorted(Comparator.comparingInt(m -> m.getOrdem()))
				.map(m -> new ModuloTrilhaResponse(m.getNome(), m.getCargaHoraria()))
				.toList();
		return new TrilhaResponse(
				String.valueOf(trilha.getId()),
				trilha.getArea(),
				trilha.getNome(),
				trilha.getDuracaoSemanas(),
				trilha.getCusto(),
				trilha.getInstituicao(),
				trilha.isCertificado(),
				modulos);
	}
}
