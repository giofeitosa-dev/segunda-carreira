package com.reconecta.ageblind.auth.api;

import java.net.URI;

import com.reconecta.ageblind.auth.dto.CadastroRequest;
import com.reconecta.ageblind.auth.dto.LoginRequest;
import com.reconecta.ageblind.auth.dto.LoginResponse;
import com.reconecta.ageblind.auth.dto.UsuarioResponse;
import com.reconecta.ageblind.auth.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "Cadastro e login JWT (papéis CANDIDATO/RH)")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/cadastro")
	@ResponseStatus(CREATED)
	@Operation(summary = "Cadastra usuário candidato (papel fixo CANDIDATO, sem auto-cadastro RH)")
	public ResponseEntity<UsuarioResponse> cadastro(@Valid @RequestBody CadastroRequest req) {
		UsuarioResponse criado = authService.cadastrar(req);
		return ResponseEntity.created(URI.create("/api/v1/auth/cadastro")).body(criado);
	}

	@PostMapping("/login")
	@ResponseStatus(OK)
	@Operation(summary = "Autentica e devolve access token JWT HS256")
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
		return ResponseEntity.ok(authService.login(req));
	}
}
