package com.reconecta.ageblind.shared.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Sprint 3, T-04 (contrato): os paths do openapi.yaml da raiz do repo
 * escopados às tags Carreira + Escudo Anti-Etarismo devem bater exatamente
 * com a spec em runtime (/v3/api-docs) — sem rota órfã nem rota ausente.
 * (O openapi.yaml traz as 29 rotas do Reconecta pai; a AgeBlind implementa 15.)
 */
@SpringBootTest
@AutoConfigureMockMvc
class ContratoOpenApiTest {

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	private static final Pattern CAMINHO_SPEC = Pattern.compile("(?m)^  (/api/v1[^\\s:]*):");
	private static final Pattern TAGS_SPEC = Pattern.compile("^      tags: \\[([^\\]]+)\\]");

	@Test
	void pathsDoOpenApiBatemComASpecEmRuntime() throws Exception {
		String yaml = Files.readString(localizarSpec(), StandardCharsets.UTF_8);

		Set<String> specPaths = new TreeSet<>();
		Matcher matcher = CAMINHO_SPEC.matcher(yaml);
		while (matcher.find()) {
			specPaths.add(matcher.group(1));
		}

		Set<String> escopoAgeBlind = new TreeSet<>();
		String caminhoAtual = null;
		for (String linha : yaml.split("\\R")) {
			Matcher caminho = CAMINHO_SPEC.matcher(linha);
			if (caminho.find()) {
				caminhoAtual = caminho.group(1);
				continue;
			}
			Matcher tags = TAGS_SPEC.matcher(linha);
			if (caminhoAtual != null && tags.find()) {
				String lista = tags.group(1);
				if (lista.contains("Carreira") || lista.contains("Escudo")) {
					escopoAgeBlind.add(caminhoAtual);
				}
			}
		}

		MvcResult result = mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andReturn();
		JsonNode paths = objectMapper
				.readTree(result.getResponse().getContentAsString())
				.path("paths");
		Set<String> runtimePaths = new TreeSet<>();
		paths.fieldNames().forEachRemaining(nome -> {
			if (nome.startsWith("/api/v1")) {
				runtimePaths.add(nome);
			}
		});

		Assertions.assertFalse(specPaths.isEmpty(), "nenhum path lido do openapi.yaml");
		Assertions.assertEquals(15, escopoAgeBlind.size(),
				"o escopo AgeBlind deve ter exatamente 15 paths no openapi.yaml");
		Assertions.assertTrue(specPaths.containsAll(runtimePaths),
				"toda rota em runtime precisa existir no openapi.yaml (sem rota órfã): "
						+ runtimePaths.stream().filter(r -> !specPaths.contains(r)).toList());
		Assertions.assertEquals(escopoAgeBlind, runtimePaths,
				"paths das tags Carreira/Escudo devem bater com a spec em runtime");
	}

	private Path localizarSpec() throws IOException {
		Path dir = Path.of("").toAbsolutePath();
		for (int i = 0; i < 5 && dir != null; i++) {
			Path candidato = dir.resolve("openapi.yaml");
			if (Files.exists(candidato)) {
				return candidato;
			}
			dir = dir.getParent();
		}
		throw new IOException("openapi.yaml não encontrado a partir de "
				+ Path.of("").toAbsolutePath());
	}
}
