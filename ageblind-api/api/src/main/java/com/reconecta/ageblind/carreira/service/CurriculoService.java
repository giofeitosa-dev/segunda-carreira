package com.reconecta.ageblind.carreira.service;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import com.reconecta.ageblind.carreira.domain.Certificacao;
import com.reconecta.ageblind.carreira.domain.Experiencia;
import com.reconecta.ageblind.carreira.domain.Formacao;
import com.reconecta.ageblind.carreira.domain.Idioma;
import com.reconecta.ageblind.carreira.domain.Perfil;
import com.reconecta.ageblind.carreira.domain.Projeto;
import com.reconecta.ageblind.carreira.dto.CurriculoRequest;
import com.reconecta.ageblind.carreira.dto.CurriculoResponse;
import com.reconecta.ageblind.carreira.repository.PerfilRepository;
import com.reconecta.ageblind.shared.api.RecursoNaoEncontradoException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CurriculoService {

	private final PerfilRepository perfilRepository;
	private final CurriculoParserService parserService;

	public CurriculoService(PerfilRepository perfilRepository, CurriculoParserService parserService) {
		this.perfilRepository = perfilRepository;
		this.parserService = parserService;
	}

	@Transactional(readOnly = true)
	public CurriculoResponse obter(Long perfilId) {
		return toResponse(buscarPerfil(perfilId));
	}

	/** Substitui o currículo a partir de um arquivo enviado (PDF, DOCX ou TXT). */
	@Transactional
	public CurriculoResponse substituirPorArquivo(Long perfilId, MultipartFile arquivo) {
		if (arquivo == null || arquivo.isEmpty()) {
			throw new IllegalArgumentException("Arquivo vazio ou ausente");
		}
		CurriculoRequest req;
		try {
			req = parserService.parse(arquivo.getBytes(), arquivo.getOriginalFilename());
		} catch (IOException e) {
			throw new IllegalArgumentException("N\u00e3o foi poss\u00edvel ler o arquivo enviado");
		}
		return substituir(perfilId, req);
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

		perfil.getIdiomas().clear();
		if (req.idiomas() != null) {
			for (CurriculoRequest.IdiomaRequest i : req.idiomas()) {
				Idioma idioma = new Idioma();
				idioma.setPerfil(perfil);
				idioma.setIdioma(i.idioma());
				idioma.setNivel(i.nivel());
				perfil.getIdiomas().add(idioma);
			}
		}

		perfil.getCertificacoes().clear();
		if (req.certificacoes() != null) {
			for (CurriculoRequest.CertificacaoRequest c : req.certificacoes()) {
				Certificacao certificacao = new Certificacao();
				certificacao.setPerfil(perfil);
				certificacao.setNome(c.nome());
				certificacao.setInstituicao(c.instituicao());
				certificacao.setAnoConclusao(c.anoConclusao());
				certificacao.setDescricao(c.descricao());
				certificacao.setLink(c.link());
				perfil.getCertificacoes().add(certificacao);
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
		List<CurriculoResponse.IdiomaResponse> idiomas = p.getIdiomas().stream()
				.map(i -> new CurriculoResponse.IdiomaResponse(
						i.getId(), i.getIdioma(), i.getNivel()))
				.toList();
		List<CurriculoResponse.CertificacaoResponse> certificacoes = p.getCertificacoes().stream()
				.sorted(Comparator.comparing(Certificacao::getAnoConclusao,
						Comparator.nullsLast(Comparator.reverseOrder())))
				.map(c -> new CurriculoResponse.CertificacaoResponse(
						c.getId(), c.getNome(), c.getInstituicao(), c.getAnoConclusao(),
						c.getDescricao(), c.getLink()))
				.toList();
		return new CurriculoResponse(experiencias, formacoes, projetos, idiomas, certificacoes);
	}
}
