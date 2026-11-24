package com.reconecta.ageblind.carreira.service;

import java.util.List;

import com.reconecta.ageblind.auth.domain.Papel;
import com.reconecta.ageblind.auth.domain.Usuario;
import com.reconecta.ageblind.auth.repository.UsuarioRepository;
import com.reconecta.ageblind.carreira.domain.Perfil;
import com.reconecta.ageblind.carreira.domain.Certificacao;
import com.reconecta.ageblind.carreira.domain.Competencia;
import com.reconecta.ageblind.carreira.domain.Experiencia;
import com.reconecta.ageblind.carreira.domain.Formacao;
import com.reconecta.ageblind.carreira.domain.Idioma;
import com.reconecta.ageblind.carreira.domain.Projeto;
import com.reconecta.ageblind.carreira.dto.PerfilRequest;
import com.reconecta.ageblind.carreira.dto.PerfilResponse;
import com.reconecta.ageblind.carreira.repository.PerfilRepository;
import com.reconecta.ageblind.shared.api.DadosTitularResponse;
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

	/**
	 * Exclusão cascata de dados pessoais (LGPD art. 18).
	 * Exclui perfil e todas as tabelas filhas (cascade), desvincula o usuario
	 * (perfil_id=null). Audit log é mantido (usuario_id via SET NULL na V3).
	 * Idempotente: sem perfil, retorna sem erro.
	 */
	@Transactional
	public void excluirTitular(Long usuarioId) {
		Usuario usuario = usuarioRepository.findById(usuarioId)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Usuario", usuarioId));
		Perfil perfil = usuario.getPerfil();
		if (perfil == null) {
			return; // idempotente: sem perfil, nada a excluir
		}
		perfilRepository.delete(perfil); // cascade remove todas as filhas
		usuario.setPerfil(null);
		usuarioRepository.save(usuario);
	}

	/**
	 * Portabilidade de dados pessoais (LGPD art. 20).
	 * Exporta todos os dados do titular em JSON estruturado, sem senha.
	 */
	@Transactional(readOnly = true)
	public DadosTitularResponse exportarTitular(Long usuarioId) {
		Usuario usuario = usuarioRepository.findById(usuarioId)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Usuario", usuarioId));
		DadosTitularResponse.UsuarioDto usuarioDto = new DadosTitularResponse.UsuarioDto(
				usuario.getId(), usuario.getEmail(), usuario.getPapel().name(),
				usuario.getCriadoEm());

		Perfil perfil = usuario.getPerfil();
		if (perfil == null) {
			return new DadosTitularResponse(usuarioDto, null, List.of(), List.of(),
					List.of(), List.of(), List.of(), List.of(), List.of());
		}

		// Inicializar coleções lazy dentro da transação
		List<Experiencia> experiencias = perfil.getExperiencias();
		List<Formacao> formacoes = perfil.getFormacoes();
		List<Projeto> projetos = perfil.getProjetos();
		List<Idioma> idiomas = perfil.getIdiomas();
		List<Certificacao> certificacoes = perfil.getCertificacoes();

		DadosTitularResponse.PerfilDto perfilDto = new DadosTitularResponse.PerfilDto(
				perfil.getId(), perfil.getNome(), perfil.getEmail(), perfil.getTelefone(),
				perfil.getCidade(), perfil.getResumo(), perfil.getLinkedin(),
				perfil.getGithub(), perfil.getCriadoEm());

		DadosTitularResponse response = new DadosTitularResponse();
		response.setUsuario(usuarioDto);
		response.setPerfil(perfilDto);
		response.setExperiencias(mapearExperiencias(experiencias));
		response.setFormacoes(mapearFormacoes(formacoes));
		response.setProjetos(mapearProjetos(projetos));
		response.setIdiomas(mapearIdiomas(idiomas));
		response.setCertificacoes(mapearCertificacoes(certificacoes));
		response.setCompetencias(mapearCompetencias(perfil.getCompetencias()));
		response.setInscricoes(List.of()); // inscricoes não têm acesso direto pelo perfil neste escopo
		return response;
	}

	private List<DadosTitularResponse.ExperienciaDto> mapearExperiencias(
			List<Experiencia> lista) {
		return lista.stream()
				.map(e -> new DadosTitularResponse.ExperienciaDto(
						e.getId(), e.getCargo(), e.getEmpresa(), e.getAnoInicio(),
						e.getAnoFim(), e.getDescricao()))
				.toList();
	}

	private List<DadosTitularResponse.FormacaoDto> mapearFormacoes(
			List<Formacao> lista) {
		return lista.stream()
				.map(f -> new DadosTitularResponse.FormacaoDto(
						f.getId(), f.getCurso(), f.getInstituicao(), f.getAnoConclusao()))
				.toList();
	}

	private List<DadosTitularResponse.ProjetoDto> mapearProjetos(List<Projeto> lista) {
		return lista.stream()
				.map(p -> new DadosTitularResponse.ProjetoDto(
						p.getId(), p.getNome(), p.getDescricao(), p.getAnoInicio(),
						p.getAnoFim()))
				.toList();
	}

	private List<DadosTitularResponse.IdiomaDto> mapearIdiomas(List<Idioma> lista) {
		return lista.stream()
				.map(i -> new DadosTitularResponse.IdiomaDto(
						i.getId(), i.getIdioma(), i.getNivel()))
				.toList();
	}

	private List<DadosTitularResponse.CertificacaoDto> mapearCertificacoes(
			List<Certificacao> lista) {
		return lista.stream()
				.map(c -> new DadosTitularResponse.CertificacaoDto(
						c.getId(), c.getNome(), c.getInstituicao(), c.getAnoObtencao()))
				.toList();
	}

	private List<DadosTitularResponse.CompetenciaDto> mapearCompetencias(
			List<com.reconecta.ageblind.carreira.domain.PerfilCompetencia> lista) {
		return lista.stream()
				.map(pc -> new DadosTitularResponse.CompetenciaDto(
						pc.getCompetencia().getId(), pc.getCompetencia().getNome(),
						String.valueOf(pc.getNivel()), null))
				.toList();
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

