package com.reconecta.ageblind.shared.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex) {
		ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
				"Violação de validação");
		List<java.util.Map<String, String>> fieldErrors = new ArrayList<>();
		ex.getBindingResult().getFieldErrors().forEach(f -> fieldErrors.add(
				java.util.Map.of("field", f.getField(),
						"message", Objects.toString(f.getDefaultMessage(), ""))));
		pd.setProperty("fieldErrors", fieldErrors);
		return badRequest(pd);
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ProblemDetail> handleParametroAusente(MissingServletRequestParameterException ex) {
		ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
				"Parâmetro obrigatório ausente: " + ex.getParameterName());
		pd.setTitle("Requisição inválida");
		return badRequest(pd);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ProblemDetail> handleTipoInvalido(MethodArgumentTypeMismatchException ex) {
		ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
				"Valor inválido para o parâmetro '" + ex.getName() + "'");
		pd.setTitle("Requisição inválida");
		return badRequest(pd);
	}

	@ExceptionHandler(RecursoNaoEncontradoException.class)
	public ResponseEntity<ProblemDetail> handleNaoEncontrado(RecursoNaoEncontradoException ex) {
		ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
		pd.setTitle("Não encontrado");
		return respond(HttpStatus.NOT_FOUND, pd);
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ProblemDetail> handleNotFound(NoResourceFoundException ex) {
		return problem(HttpStatus.NOT_FOUND, "Recurso não encontrado", ex.getMessage());
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ProblemDetail> handleMetodoNaoSuportado(HttpRequestMethodNotSupportedException ex) {
		return problem(HttpStatus.METHOD_NOT_ALLOWED,
				"M�todo n�o suportado: " + ex.getMethod(),
				ex.getMessage());
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ProblemDetail> handleJsonInvalido(HttpMessageNotReadableException ex) {
		return problem(HttpStatus.BAD_REQUEST, "Corpo JSON inv�lido ou malformado", ex.getMessage());
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ProblemDetail> handleArgumentoInvalido(IllegalArgumentException ex) {
		return problem(HttpStatus.BAD_REQUEST, "Requisição inválida", ex.getMessage());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ProblemDetail> handleGeneric(Exception ex) {
		return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno", ex.getMessage());
	}

	private ResponseEntity<ProblemDetail> badRequest(ProblemDetail pd) {
		return respond(HttpStatus.BAD_REQUEST, pd);
	}

	private ResponseEntity<ProblemDetail> problem(HttpStatus status, String detail, String exMsg) {
		ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
		if (exMsg != null && !exMsg.isBlank()) {
			pd.setProperty("exception", exMsg);
		}
		return respond(status, pd);
	}

	private ResponseEntity<ProblemDetail> respond(HttpStatus status, ProblemDetail pd) {
		return ResponseEntity.status(status)
				.contentType(MediaType.parseMediaType("application/problem+json"))
				.body(pd);
	}
}
