package com.reconecta.ageblind.carreira.service;

import java.util.List;

import com.reconecta.ageblind.carreira.domain.Perfil;
import com.reconecta.ageblind.carreira.dto.PerfilRequest;
import com.reconecta.ageblind.carreira.dto.PerfilResponse;
import com.reconecta.ageblind.carreira.repository.PerfilRepository;
import com.reconecta.ageblind.shared.api.RecursoNaoEncontradoException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PerfilService {

	private final PerfilRepository perfilRepository;

	public PerfilService(PerfilRepository perfilRepository) {
		this.perfilRepository = perfilRepository;
	}

	@Transactional
	public PerfilResponse criar(PerfilRequest req) {
		Perfil p = new Perfil();
		p.setNome(req.nome());
		p.setEmail(req.email());
		p.setAreaAtual(req.areaAtual());
		p.setAreaAlvo(req.areaAlvo());
		p.setPretensaoSalarial(req.pretensaoSalarial());
		if (req.disponibilidade() != null) {
			p.getDisponibilidade().addAll(req.disponibilidade());
		}
		return toResponse(perfilRepository.save(p));
	}

	@Transactional(readOnly = true)
	public PerfilResponse obter(Long id) {
		return toResponse(perfilRepository.findById(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Perfil", id)));
	}

	private PerfilResponse toResponse(Perfil p) {
		return new PerfilResponse(
				p.getId(),
				p.getNome(),
				p.getEmail(),
				p.getAreaAtual(),
				p.getAreaAlvo(),
				p.getPretensaoSalarial(),
				List.copyOf(p.getDisponibilidade()),
				p.getStatusCuradoria(),
				p.getCriadoEm());
	}
}
