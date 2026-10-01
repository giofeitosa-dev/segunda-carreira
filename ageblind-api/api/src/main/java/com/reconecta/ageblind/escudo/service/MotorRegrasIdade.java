package com.reconecta.ageblind.escudo.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reconecta.ageblind.escudo.dominio.RegraIdade;
import com.reconecta.ageblind.escudo.dominio.SinalIdade;
import com.reconecta.ageblind.escudo.dto.RegraAplicada;

import org.springframework.stereotype.Service;

/**
 * Motor das 15 regras age-blind (05-ETARISMO.md §5).
 * Automatiza a remoção/ocultação de proxies de idade; as regras de reescrita
 * ficam como ação manual (revisão humana — nunca mentir, ocultar ≠ falsificar).
 */
@Service
public class MotorRegrasIdade {

	private final List<RegraIdade> regras;

	public MotorRegrasIdade() {
		try (InputStream in = getClass().getResourceAsStream("/escudo/regras-idade.json")) {
			RegraIdade[] carregadas = new ObjectMapper().readValue(in, RegraIdade[].class);
			this.regras = List.of(carregadas);
		} catch (Exception e) {
			throw new IllegalStateException("Falha ao carregar escudo/regras-idade.json", e);
		}
	}

	public List<RegraIdade> regras() {
		return regras;
	}

	/** Detecta proxies de idade sem alterar o texto (base da auditoria). */
	public List<SinalIdade> detectar(String texto) {
		List<SinalIdade> sinais = new ArrayList<>();
		for (RegraIdade r : regras) {
			if (!r.automatica() || r.regex() == null) {
				continue;
			}
			Matcher m = Pattern.compile(r.regex()).matcher(texto);
			while (m.find()) {
				String trecho = m.group().trim();
				if (!trecho.isEmpty()) {
					sinais.add(new SinalIdade(r.codigo(), r.descricao(), truncar(trecho),
							r.severidade(), r.acao()));
				}
			}
		}
		return sinais;
	}

	/** Aplica as regras automáticas e lista o status de todas as 15. */
	public ResultadoAgeBlind aplicar(String curriculo) {
		String conteudo = curriculo;
		List<RegraAplicada> aplicadas = new ArrayList<>();
		for (RegraIdade r : regras) {
			String rotulo = r.codigo() + " — " + r.descricao();
			if (!r.automatica() || r.regex() == null) {
				aplicadas.add(new RegraAplicada(rotulo, "ação manual (revisão humana)"));
				continue;
			}
			Pattern p = Pattern.compile(r.regex());
			Matcher m = p.matcher(conteudo);
			if (m.find()) {
				conteudo = m.replaceAll(r.substituicao());
				aplicadas.add(new RegraAplicada(rotulo, "aplicado automaticamente"));
			} else {
				aplicadas.add(new RegraAplicada(rotulo, "não identificado neste currículo"));
			}
		}
		return new ResultadoAgeBlind(normalizar(conteudo), aplicadas);
	}

	private String normalizar(String texto) {
		return texto
				.replaceAll("[ \\t]{2,}", " ")
				.replaceAll("\\(\\s*\\)", "")
				.replaceAll("(?m)\\s*[-–—]\\s*$", "")
				.replaceAll("(?m)^[ \\t]{1,}$", "")
				.replaceAll("\\n{3,}", "\n\n")
				.trim();
	}

	private String truncar(String s) {
		return s.length() > 80 ? s.substring(0, 77) + "..." : s;
	}

	public record ResultadoAgeBlind(String conteudo, List<RegraAplicada> regrasAplicadas) {
	}
}
