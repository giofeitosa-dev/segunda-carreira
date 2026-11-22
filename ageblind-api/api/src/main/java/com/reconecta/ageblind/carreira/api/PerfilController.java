package com.reconecta.ageblind.carreira.api;

import java.net.URI;

import com.reconecta.ageblind.carreira.dto.CurriculoRequest;
import com.reconecta.ageblind.carreira.dto.CurriculoResponse;
import com.reconecta.ageblind.carreira.dto.PerfilRequest;
import com.reconecta.ageblind.carreira.dto.PerfilResponse;
import com.reconecta.ageblind.carreira.dto.TransferiveisResponse;
import com.reconecta.ageblind.carreira.service.CompetenciasTransferiveisService;
import com.reconecta.ageblind.carreira.service.CurriculoService;
import com.reconecta.ageblind.carreira.service.PerfilService;
import com.reconecta.ageblind.shared.api.Auditar;
import com.reconecta.ageblind.shared.security.PosseChecker;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/api/v1/perfis")
@Tag(name = "Carreira", description = "ODS 10+5 — perfis, vagas e trilhas (40+)")
public class PerfilController {

	private final PerfilService perfilService;
	private final CompetenciasTransferiveisService transferiveisService;
	private final CurriculoService curriculoService;
	private final PosseChecker posseChecker;

	public PerfilController(PerfilService perfilService, CompetenciasTransferiveisService transferiveisService,
			CurriculoService curriculoService, PosseChecker posseChecker) {
		this.perfilService = perfilService;
		this.transferiveisService = transferiveisService;
		this.curriculoService = curriculoService;
		this.posseChecker = posseChecker;
	}

	@PostMapping
	@ResponseStatus(CREATED)
	@Operation(summary = "Cadastra perfil profissional (sem data de nascimento)")
	@Auditar(acao = "PERFIL_CRIADO")
	public ResponseEntity<PerfilResponse> criar(@Valid @RequestBody PerfilRequest req) {
		PerfilResponse criado = perfilService.criar(req);
		return ResponseEntity.created(URI.create("/api/v1/perfis/" + criado.id())).body(criado);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Consulta perfil")
	@Auditar(acao = "PERFIL_OBTIDO")
	public PerfilResponse obter(@PathVariable Long id) {
		return perfilService.obter(id);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Edita perfil profissional (sem data de nascimento)")
	@Auditar(acao = "PERFIL_ATUALIZADO")
	public PerfilResponse atualizar(@PathVariable Long id, @Valid @RequestBody PerfilRequest req) {
		return perfilService.atualizar(id, req);
	}

	@PostMapping("/{id}/competencias-transferiveis")
	@Operation(summary = "Mapeia experiência antiga para competências da área alvo (R12)")
	public TransferiveisResponse competenciasTransferiveis(@PathVariable Long id) {
		posseChecker.exigirDonoOuRh(id);
		return transferiveisService.mapear(id);
	}

	@GetMapping("/{id}/curriculo")
	@Operation(summary = "Consulta o currículo estruturado do perfil (experiências, formações, projetos)")
	public CurriculoResponse curriculo(@PathVariable Long id) {
		posseChecker.exigirDonoOuRh(id);
		return curriculoService.obter(id);
	}

	@PutMapping("/{id}/curriculo")
	@Operation(summary = "Substitui o currículo estruturado do perfil (sem data de nascimento)")
	public CurriculoResponse substituirCurriculo(@PathVariable Long id, @Valid @RequestBody CurriculoRequest req) {
		posseChecker.exigirDonoOuRh(id);
		return curriculoService.substituir(id, req);
	}

	@PostMapping(value = "/{id}/curriculo/arquivo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "Substitui o currículo por upload de PDF, DOCX ou TXT (parse automático, descarta idade)")
	public CurriculoResponse substituirCurriculoArquivo(@PathVariable Long id,
			@RequestParam("arquivo") MultipartFile arquivo) {
		posseChecker.exigirDonoOuRh(id);
		return curriculoService.substituirPorArquivo(id, arquivo);
	}
}
