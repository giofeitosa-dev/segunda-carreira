package com.reconecta.ageblind.carreira.service;

import java.util.Comparator;
import java.util.List;

import com.reconecta.ageblind.carreira.domain.Mentoria;
import com.reconecta.ageblind.carreira.dto.MentoriaResponse;
import com.reconecta.ageblind.carreira.repository.MentoriaRepository;
import com.reconecta.ageblind.carreira.repository.PerfilRepository;
import com.reconecta.ageblind.shared.api.RecursoNaoEncontradoException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MentoriaService {

	private final MentoriaRepository mentoriaRepository;
	private final PerfilRepository perfilRepository;

	public MentoriaService(MentoriaRepository mentoriaRepository, PerfilRepository perfilRepository) {
		this.mentoriaRepository = mentoriaRepository;
		this.perfilRepository = perfilRepository;
	}

	/**
	 * Encaixe de mentoria: filtra por área direta; se vier perfilId,
	 * usa a área alvo do perfil (rede de apoio para migração de carreira).
	 */
	@Transactional
	public List<MentoriaResponse> listar(Long perfilId, String area) {
		String areaEfetiva = area;
		if (areaEfetiva == null || areaEfetiva.isBlank()) {
			if (perfilId == null) {
				return todas();
			}
			var perfil = perfilRepository.findById(perfilId)
					.orElseThrow(() -> new RecursoNaoEncontradoException("Perfil", perfilId));
			areaEfetiva = perfil.getAreaAlvo();
			if (areaEfetiva == null || areaEfetiva.isBlank()) {
				return todas();
			}
		}
		return mentoriaRepository.findByAreaIgnoreCase(areaEfetiva.trim()).stream()
				.sorted(Comparator.comparing(Mentoria::getId))
				.map(this::paraResponse)
				.toList();
	}

	private List<MentoriaResponse> todas() {
		return mentoriaRepository.findAll().stream()
				.sorted(Comparator.comparing(Mentoria::getId))
				.map(this::paraResponse)
				.toList();
	}

	private MentoriaResponse paraResponse(Mentoria m) {
		return new MentoriaResponse(m.getId(), m.getMentor(), m.getArea(),
				m.getDisponibilidade(), m.getAgendamento());
	}
}
