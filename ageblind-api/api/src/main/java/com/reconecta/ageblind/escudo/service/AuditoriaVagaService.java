package com.reconecta.ageblind.escudo.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reconecta.ageblind.escudo.dto.AuditoriaVagaResponse;
import com.reconecta.ageblind.escudo.enums.RiscoLegal;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

/**
 * Auditoria de linguagem de vagas: termos com viés etário, risco legal
 * e sugestão de reescrita (Lei 9.029/1995, CLT 373-A, Estatuto art. 27).
 */
@Service
public class AuditoriaVagaService {

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record RegraTermo(
			String termo,
			String severidade,
			List<String> padroes,
			String risco,
			String baseLegal) {
	}

	private final List<RegraTermo> regras;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public AuditoriaVagaService() {
		try (InputStream in = new ClassPathResource("escudo/termos-discriminatorios.json").getInputStream()) {
			this.regras = List.of(objectMapper.readValue(in, RegraTermo[].class));
		} catch (IOException e) {
			throw new UncheckedIOException("Falha ao carregar termos-discriminatorios.json", e);
		}
	}

	public AuditoriaVagaResponse auditar(String texto, String titulo) {
		String alvo = texto.toLowerCase();
		List<String> encontrados = new ArrayList<>();
		RiscoLegal risco = RiscoLegal.BAIXO;
		String baseLegal = "Nenhum termo com viés etário identificado — anúncio compatível com Lei 9.029/1995";

		for (RegraTermo regra : regras) {
			for (String padrao : regra.padroes()) {
				if (Pattern.compile(padrao, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE).matcher(alvo).find()) {
					encontrados.add(regra.termo());
					RiscoLegal doRegra = RiscoLegal.valueOf(regra.risco());
					if (doRegra.ordinal() > risco.ordinal()) {
						risco = doRegra;
						baseLegal = regra.baseLegal();
					}
					break;
				}
			}
		}

		return new AuditoriaVagaResponse(titulo, encontrados, risco, baseLegal, sugestao(encontrados));
	}

	private String sugestao(List<String> encontrados) {
		if (encontrados.isEmpty()) {
			return "Redação adequada — descreva competências e entregas em vez de perfis";
		}
		StringBuilder sb = new StringBuilder("Reescreva descrevendo competências: ");
		if (encontrados.contains("LIMITE_ETARIO")) {
			sb.append("substitua idade por 'experiência comprovada em X'; ");
		}
		if (encontrados.contains("ANOS_MAX_EXPERIENCIA")) {
			sb.append("remova o teto de anos de experiência; ");
		}
		if (encontrados.contains("PERFIL_JOVEM") || encontrados.contains("NATIVO_DIGITAL")) {
			sb.append("remova adjetivos geracionais; cite as tecnologias usadas; ");
		}
		if (encontrados.contains("RECEM_FORMADO")) {
			sb.append("aceite qualquer formação com competência equivalente; ");
		}
		if (encontrados.contains("FIT_CULTURAL")) {
			sb.append("substitua 'fit cultural' por critérios objetivos de avaliação; ");
		}
		return sb.toString();
	}
}
