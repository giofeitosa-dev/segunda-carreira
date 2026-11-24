package com.reconecta.ageblind.carreira.api;

import com.reconecta.ageblind.carreira.service.PerfilService;
import com.reconecta.ageblind.shared.api.DadosTitularResponse;
import com.reconecta.ageblind.shared.security.UsuarioAtual;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.NO_CONTENT;

/**
 * Endpoints de governança LGPD (ciclo 003).
 * Exclusão cascata e portabilidade de dados pessoais do titular autenticado.
 * Só o próprio titular pode acessar seus dados (posse via usuario.id do JWT).
 */
@RestController
@RequestMapping("/api/v1/meus-dados")
@Tag(name = "LGPD", description = "Direitos do titular: exclusao e portabilidade (LGPD art. 18 e 20)")
public class TitularController {

	private final PerfilService perfilService;

	public TitularController(PerfilService perfilService) {
		this.perfilService = perfilService;
	}

	/**
	 * Exclusão cascata de dados pessoais (LGPD art. 18).
	 * Exclui perfil e todas as tabelas filhas, desvincula o usuario,
	 * mantendo o audit log imutável.
	 */
	@DeleteMapping
	@Operation(summary = "Exclui meus dados pessoais (exclusão cascata, LGPD art. 18)")
	public ResponseEntity<Void> excluir() {
		Long usuarioId = UsuarioAtual.usuarioId();
		perfilService.excluirTitular(usuarioId);
		return ResponseEntity.noContent().build();
	}

	/**
	 * Portabilidade de dados pessoais (LGPD art. 20).
	 * Exporta todos os dados do titular em JSON estruturado, sem senha.
	 */
	@GetMapping("/exportacao")
	@Operation(summary = "Exporta meus dados pessoais (portabilidade, LGPD art. 20)")
	public ResponseEntity<DadosTitularResponse> exportar() {
		Long usuarioId = UsuarioAtual.usuarioId();
		DadosTitularResponse dados = perfilService.exportarTitular(usuarioId);
		return ResponseEntity.ok(dados);
	}
}
