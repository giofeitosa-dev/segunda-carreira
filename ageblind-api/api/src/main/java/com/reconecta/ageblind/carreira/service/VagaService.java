package com.reconecta.ageblind.carreira.service;

import java.util.List;

import com.reconecta.ageblind.carreira.domain.Vaga;
import com.reconecta.ageblind.carreira.dto.VagaResponse;
import com.reconecta.ageblind.carreira.enums.ModeloVaga;
import com.reconecta.ageblind.carreira.repository.VagaRepository;
import com.reconecta.ageblind.shared.api.RecursoNaoEncontradoException;
import com.reconecta.ageblind.shared.dto.PaginaResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class VagaService {

	private static final int TAMANHO_MAXIMO = 100;

	private final VagaRepository vagaRepository;

	public VagaService(VagaRepository vagaRepository) {
		this.vagaRepository = vagaRepository;
	}

	public PaginaResponse<VagaResponse> listar(String area, ModeloVaga modelo, int page, int size) {
		if (page < 0) {
			throw new IllegalArgumentException("page não pode ser negativo");
		}
		Pageable pageable = PageRequest.of(page, Math.min(Math.max(size, 1), TAMANHO_MAXIMO),
				Sort.by("id"));
		Page<Vaga> resultado;
		if (area != null && !area.isBlank() && modelo != null) {
			resultado = vagaRepository.findByAreaIgnoreCaseAndModelo(area.trim(), modelo, pageable);
		} else if (area != null && !area.isBlank()) {
			resultado = vagaRepository.findByAreaIgnoreCase(area.trim(), pageable);
		} else if (modelo != null) {
			resultado = vagaRepository.findByModelo(modelo, pageable);
		} else {
			resultado = vagaRepository.findAll(pageable);
		}
		return pagina(resultado);
	}

	public VagaResponse obter(Long id) {
		Vaga vaga = vagaRepository.findById(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Vaga", id));
		return paraResponse(vaga);
	}

	private PaginaResponse<VagaResponse> pagina(Page<Vaga> page) {
		List<VagaResponse> conteudo = page.getContent().stream().map(this::paraResponse).toList();
		return new PaginaResponse<>(conteudo, page.getNumber(), page.getSize(),
				page.getTotalElements(), page.getTotalPages());
	}

	private VagaResponse paraResponse(Vaga vaga) {
		List<String> requisitos = vaga.getRequisitos().stream()
				.map(r -> r.getNome())
				.sorted()
				.toList();
		return new VagaResponse(vaga.getId(), vaga.getTitulo(), vaga.getEmpresa(), vaga.getArea(),
				vaga.getDescricao(), vaga.getSalario(), vaga.getModelo(), vaga.getPrazoInscricao(), requisitos);
	}
}
