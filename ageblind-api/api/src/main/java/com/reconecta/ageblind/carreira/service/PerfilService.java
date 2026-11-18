package com.reconecta.ageblind.carreira.service;

import java.util.List;

import com.reconecta.ageblind.auth.domain.Papel;
import com.reconecta.ageblind.auth.domain.Usuario;
import com.reconecta.ageblind.auth.repository.UsuarioRepository;
import com.reconecta.ageblind.carreira.domain.Perfil;
import com.reconecta.ageblind.carreira.dto.PerfilRequest;
import com.reconecta.ageblind.carreira.dto.PerfilResponse;
import com.reconecta.ageblind.carreira.repository.PerfilRepository;
import com.reconecta.ageblind.shared.api.RecursoNaoEncontradoException;
import com.reconecta.ageblind.shared.security.PosseChecker;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PerfilService {

	private final PerfilRepository perfilRepository;
	private final UsuarioRepository usuarioRepository;
	private final PosseChecker posseChecker;

	public PerfilService(
			PerfilRepository perfilRepository,
			UsuarioRepository usuarioRepository,
			PosseChecker posseChecker) {
		this.perfilRepository = perfilRepository;
		this.usuarioRepository = usuarioRepository;
		this.posseChecker = posseChecker;
	}

	@Transactional
	public PerfilResponse criar(PerfilRequest req) {
		Perfil p = new Perfil();
		p.setNome(req.nome());
		p.setEmail(req.email());
		p.setAreaAtual(req.areaAtual());
		p.setAreaAlvo(req.areaAlvo());
		p.setPretensaoSalarial(req.pretensaoSalarial());
		p.setTelefone(req.telefone());
		p.setCidade(req.cidade());
		p.setResumo(req.resumo());
		p.setLinkedin(req.linkedin());
		p.setGithub(req.github());
		if (req.disponibilidade() != null) {
			p.getDisponibilidade().addAll(req.disponibilidade());
		}
		Perfil salvo = perfilRepository.save(p);
		vincularSeCandidatoSemPerfil(salvo);
		return toResponse(salvo);
	}

	/** Auto-vínculo US2.6/R12: CANDIDATO sem perfil vincula o recém-criado. */
	private void vincularSeCandidatoSemPerfil(Perfil perfil) {
		Usuario usuario = posseChecker.usuarioAtual();
		if (usuario.getPapel() != Papel.CANDIDATO || usuario.getPerfil() != null) {
			return;
		}
		usuario.setPerfil(perfil);
		usuarioRepository.save(usuario);
	}

	@Transactional(readOnly = true)
	public PerfilResponse obter(Long id) {
		posseChecker.exigirDonoOuRh(id);
		return toResponse(perfilRepository.findById(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Perfil", id)));
	}

	@Transactional
	public PerfilResponse atualizar(Long id, PerfilRequest req) {
		posseChecker.exigirDonoOuRh(id);
		Perfil p = perfilRepository.findById(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Perfil", id));
		p.setNome(req.nome());
		p.setEmail(req.email());
		p.setAreaAtual(req.areaAtual());
		p.setAreaAlvo(req.areaAlvo());
		p.setPretensaoSalarial(req.pretensaoSalarial());
		p.setTelefone(req.telefone());
		p.setCidade(req.cidade());
		p.setResumo(req.resumo());
		p.setLinkedin(req.linkedin());
		p.setGithub(req.github());
		if (req.disponibilidade() != null) {
			p.getDisponibilidade().clear();
			p.getDisponibilidade().addAll(req.disponibilidade());
		}
		return toResponse(perfilRepository.save(p));
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
				p.getCriadoEm(),
				p.getTelefone(),
				p.getCidade(),
				p.getResumo(),
				p.getLinkedin(),
				p.getGithub());
	}
}
