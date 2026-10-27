package com.reconecta.ageblind.carreira.service;

import java.io.ByteArrayInputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.reconecta.ageblind.carreira.dto.CurriculoRequest;
import com.reconecta.ageblind.carreira.dto.CurriculoRequest.ExperienciaRequest;
import com.reconecta.ageblind.carreira.dto.CurriculoRequest.FormacaoRequest;
import com.reconecta.ageblind.carreira.dto.CurriculoRequest.ProjetoRequest;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.WriteOutContentHandler;
import org.springframework.stereotype.Service;

/**
 * Converte o texto de um currículo (PDF, DOCX ou TXT) no modelo estruturado do perfil.
 * Heurística por seções (PT/EN): EXPERIÊNCIA, FORMAÇÃO/EDUCAÇÃO, PROJETOS.
 * REGRA DE OURO: qualquer linha com nascimento ou idade é descartada no parse.
 */
@Service
public class CurriculoParserService {

	private static final int LIMITE_TEXTO = 1_000_000;

	private static final Pattern ANO = Pattern.compile("\\b(19|20)\\d{2}\\b");

	private static final Pattern LINK = Pattern.compile("https?://\\S+");

	private static final Pattern IDADE_OU_NASCIMENTO = Pattern.compile(
			"(?i)(data\\s*de\\s*nasc\\w*|\\bnascid\\w*|\\bnascimento\\b|\\bnasc\\b|\\bidade\\b|"
					+ "\\b\\d{1,2}\\s*anos?\\b|\\bage\\b|\\bborn\\b|\\bbirth\\b)");

	private static final Pattern CABECALHO_SECAO = Pattern.compile(
			"(?i)^(experiencia(s)?|experiencias?\\s+profissionais?|professional\\s+experience|historico\\s+profissional|"
					+ "formacao(es)?|formacao\\s+academica|educacao|education|academic\\s+(background|experience)|"
					+ "projetos?|projects?|portfolio)\\b[:.\\-\\s]*");

	private static final Pattern SEPARADOR_PARTES = Pattern.compile(
			"\\s*[|·•]\\s*|\\s+[—–]\\s+|\\s+-\\s+|\\s{2,}");

	private enum Secao {
		NENHUMA, EXPERIENCIA, FORMACAO, PROJETO
	}

	public CurriculoRequest parse(byte[] conteudo, String nomeArquivo) {
		return estruturar(extrairTexto(conteudo, nomeArquivo));
	}

	private String extrairTexto(byte[] conteudo, String nomeArquivo) {
		try {
			Metadata metadata = new Metadata();
			if (nomeArquivo != null && !nomeArquivo.isBlank()) {
				metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, nomeArquivo);
			}
			WriteOutContentHandler handler = new WriteOutContentHandler(LIMITE_TEXTO);
			new AutoDetectParser().parse(new ByteArrayInputStream(conteudo), handler, metadata, new ParseContext());
			return handler.toString();
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"N\u00e3o foi poss\u00edvel ler o arquivo \u2014 envie um PDF, DOCX ou TXT v\u00e1lido");
		}
	}

	private CurriculoRequest estruturar(String texto) {
		List<ExperienciaRequest> experiencias = new ArrayList<>();
		List<FormacaoRequest> formacoes = new ArrayList<>();
		List<ProjetoRequest> projetos = new ArrayList<>();
		Secao secao = Secao.NENHUMA;
		List<String> bloco = new ArrayList<>();

		for (String bruta : texto.split("\\R")) {
			String linha = bruta.trim();
			if (linha.isEmpty()) {
				fechar(secao, bloco, experiencias, formacoes, projetos);
				continue;
			}
			if (IDADE_OU_NASCIMENTO.matcher(linha).find()) {
				continue;
			}
			if (linha.length() <= 40 && CABECALHO_SECAO.matcher(linha).lookingAt()) {
				fechar(secao, bloco, experiencias, formacoes, projetos);
				secao = secaoDe(linha);
				continue;
			}
			if (ehInicioDeItem(linha)) {
				fechar(secao, bloco, experiencias, formacoes, projetos);
				linha = linha.replaceFirst("^[-\\u2022*\\u25AA\\u27A1\\u2013\\u2014]\\s*", "")
						.replaceFirst("^\\d+[.)]\\s+", "")
						.trim();
				if (linha.isEmpty()) {
					continue;
				}
			}
			bloco.add(linha);
		}
		fechar(secao, bloco, experiencias, formacoes, projetos);

		if (experiencias.isEmpty() && formacoes.isEmpty() && projetos.isEmpty()) {
			throw new IllegalArgumentException(
					"N\u00e3o foi poss\u00edvel identificar experi\u00eancias, forma\u00e7\u00f5es ou projetos no arquivo");
		}
		return new CurriculoRequest(experiencias, formacoes, projetos);
	}

	private void fechar(Secao secao, List<String> bloco,
			List<ExperienciaRequest> experiencias, List<FormacaoRequest> formacoes, List<ProjetoRequest> projetos) {
		if (bloco.isEmpty() || secao == Secao.NENHUMA) {
			bloco.clear();
			return;
		}
		String cabecalho = bloco.get(0);
		String descricao = String.join(" ", bloco.subList(1, bloco.size()));
		switch (secao) {
			case EXPERIENCIA -> experiencia(cabecalho, descricao).ifPresent(experiencias::add);
			case FORMACAO -> formacao(cabecalho, descricao).ifPresent(formacoes::add);
			case PROJETO -> projeto(cabecalho, descricao).ifPresent(projetos::add);
			case NENHUMA -> {
			}
		}
		bloco.clear();
	}

	private Optional<ExperienciaRequest> experiencia(String cabecalho, String descricao) {
		List<Integer> anos = anosDe(cabecalho);
		if (anos.isEmpty()) {
			return Optional.empty();
		}
		List<String> partes = partesDe(semAnos(cabecalho));
		if (partes.size() < 2) {
			return Optional.empty();
		}
		String cargo = partes.get(0);
		String empresa = partes.get(1);
		if (cargo.isBlank() || empresa.isBlank()) {
			return Optional.empty();
		}
		Integer anoFim = anos.size() > 1 ? anos.get(1) : null;
		return Optional.of(new ExperienciaRequest(cargo, empresa, anos.get(0), anoFim, descricao));
	}

	private Optional<FormacaoRequest> formacao(String cabecalho, String descricao) {
		List<String> partes = partesDe(semAnos(cabecalho));
		if (partes.size() < 2) {
			return Optional.empty();
		}
		String curso = partes.get(0);
		String instituicao = partes.get(1);
		if (curso.isBlank() || instituicao.isBlank()) {
			return Optional.empty();
		}
		List<Integer> anos = anosDe(cabecalho);
		Integer anoConclusao = anos.isEmpty() ? null : anos.get(0);
		return Optional.of(new FormacaoRequest(curso, instituicao, anoConclusao, descricao));
	}

	private Optional<ProjetoRequest> projeto(String cabecalho, String descricao) {
		String completo = cabecalho + " " + descricao;
		Matcher link = LINK.matcher(completo);
		String url = link.find() ? link.group() : null;
		String textoSemLink = completo.replaceAll("https?://\\S+", " ")
				.replaceAll("\\s{2,}", " ")
				.replaceAll("^[\\s\\-\\u2013\\u2014]+|[\\s\\-\\u2013\\u2014]+$", "")
				.trim();
		if (textoSemLink.isBlank() && url == null) {
			return Optional.empty();
		}
		String nome = textoSemLink.isBlank() ? url : textoSemLink;
		return Optional.of(new ProjetoRequest(nome, null, url));
	}

	private Secao secaoDe(String linha) {
		String norm = normalizar(linha);
		if (norm.startsWith("exper") || norm.startsWith("professional experience")
				|| norm.startsWith("historico profissional")) {
			return Secao.EXPERIENCIA;
		}
		if (norm.startsWith("form") || norm.startsWith("educ") || norm.startsWith("academic")) {
			return Secao.FORMACAO;
		}
		if (norm.startsWith("projet") || norm.startsWith("project") || norm.startsWith("portfolio")) {
			return Secao.PROJETO;
		}
		return Secao.NENHUMA;
	}

	private boolean ehInicioDeItem(String linha) {
		return linha.matches("^[\\-•*▪➢>–—].*") || linha.matches("^\\d+[.)]\\s+.*");
	}

	private List<Integer> anosDe(String texto) {
		List<Integer> anos = new ArrayList<>();
		Matcher m = ANO.matcher(texto);
		while (m.find()) {
			anos.add(Integer.parseInt(m.group()));
		}
		return anos;
	}

	private String semAnos(String texto) {
		return ANO.matcher(texto).replaceAll(" ")
				.replaceAll("(?i)\\b(atual|presente|hoje|current)\\b", " ")
				.replaceAll("\\s{2,}", " ")
				.trim();
	}

	private List<String> partesDe(String texto) {
		List<String> partes = new ArrayList<>();
		for (String parte : SEPARADOR_PARTES.split(texto)) {
			adicionar(partes, parte);
		}
		if (partes.size() < 2) {
			partes.clear();
			for (String parte : texto.split("\\s*,\\s*")) {
				adicionar(partes, parte);
			}
		}
		return partes;
	}

	private void adicionar(List<String> partes, String parte) {
		String limpa = parte.replaceAll("^[\\s\\-\\u2013\\u2014]+|[\\s\\-\\u2013\\u2014]+$", "").trim();
		if (!limpa.isEmpty()) {
			partes.add(limpa);
		}
	}

	private String normalizar(String texto) {
		return Normalizer.normalize(texto, Normalizer.Form.NFD)
				.replaceAll("\\p{M}", "")
				.toLowerCase()
				.trim();
	}
}
