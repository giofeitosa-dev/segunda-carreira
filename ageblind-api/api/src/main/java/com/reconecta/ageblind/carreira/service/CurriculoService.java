package com.reconecta.ageblind.carreira.service;

import java.util.Comparator;
import java.util.List;

import com.reconecta.ageblind.carreira.domain.Experiencia;
import com.reconecta.ageblind.carreira.domain.Formacao;
import com.reconecta.ageblind.carreira.domain.Perfil;
import com.reconecta.ageblind.carreira.domain.Projeto;
import com.reconecta.ageblind.carreira.dto.CurriculoRequest;
import com.reconecta.ageblind.carreira.dto.CurriculoResponse;
import com.reconecta.ageblind.carreira.repository.PerfilRepository;
import com.reconecta.ageblind.shared.api.RecursoNaoEncontradoException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CurriculoService {

	private final PerfilRepository perfilRepository;

	public CurriculoService(PerfilRepository perfilRepository) {
		this.perfilRepository = perfilRepository;
	}

	@Transactional(readOnly = true)
	public CurriculoResponse obter(Long perfilId) {
		return toResponse(buscarPerfil(perfilId));
	}

	@Transactional
	public CurriculoResponse substituir(Long perfilId, CurriculoRequest req) {
		Perfil perfil = buscarPerfil(perfilId);

		perfil.getExperiencias().clear();
		if (req.experiencias() != null) {
			for (CurriculoRequest.ExperienciaRequest e : req.experiencias()) {
				Experiencia experiencia = new Experiencia();
				experiencia.setPerfil(perfil);
				experiencia.setCargo(e.cargo());
				experiencia.setEmpresa(e.empresa());
				experiencia.setAnoInicio(e.anoInicio());
				experiencia.setAnoFim(e.anoFim());
				experiencia.setDescricao(e.descricao());
				perfil.getExperiencias().add(experiencia);
			}
		}

		perfil.getFormacoes().clear();
		if (req.formacoes() != null) {
			for (CurriculoRequest.FormacaoRequest f : req.formacoes()) {
				Formacao formacao = new Formacao();
				formacao.setPerfil(perfil);
				formacao.setCurso(f.curso());
				formacao.setInstituicao(f.instituicao());
				formacao.setAnoConclusao(f.anoConclusao());
				formacao.setDescricao(f.descricao());
				perfil.getFormacoes().add(formacao);
			}
		}

		perfil.getProjetos().clear();
		if (req.projetos() != null) {
			for (CurriculoRequest.ProjetoRequest p : req.projetos()) {
				Projeto projeto = new Projeto();
				projeto.setPerfil(perfil);
				projeto.setNome(p.nome());
				projeto.setDescricao(p.descricao());
				projeto.setLink(p.link());
				perfil.getProjetos().add(projeto);
			}
		}

		return toResponse(perfilRepository.save(perfil));
	}

	private Perfil buscarPerfil(Long perfilId) {
		return perfilRepository.findById(perfilId)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Perfil", perfilId));
	}

	private CurriculoResponse toResponse(Perfil p) {
		List<CurriculoResponse.ExperienciaResponse> experiencias = p.getExperiencias().stream()
				.sorted(Comparator.comparingInt(Experiencia::getAnoInicio).reversed())
				.map(e -> new CurriculoResponse.ExperienciaResponse(
						e.getId(), e.getCargo(), e.getEmpresa(), e.getAnoInicio(), e.getAnoFim(), e.getDescricao()))
				.toList();
		List<CurriculoResponse.FormacaoResponse> formacoes = p.getFormacoes().stream()
				.sorted(Comparator.comparing(Formacao::getAnoConclusao,
						Comparator.nullsLast(Comparator.reverseOrder())))
				.map(f -> new CurriculoResponse.FormacaoResponse(
						f.getId(), f.getCurso(), f.getInstituicao(), f.getAnoConclusao(), f.getDescricao()))
				.toList();
		List<CurriculoResponse.ProjetoResponse> projetos = p.getProjetos().stream()
				.map(pr -> new CurriculoResponse.ProjetoResponse(
						pr.getId(), pr.getNome(), pr.getDescricao(), pr.getLink()))
				.toList();
		return new CurriculoResponse(experiencias, formacoes, projetos);
	}
}
