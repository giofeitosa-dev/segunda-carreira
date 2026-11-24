package com.reconecta.ageblind.carreira.api;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayOutputStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reconecta.ageblind.shared.security.JwtTestSupport;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@SpringBootTest
@AutoConfigureMockMvc
class CurriculoUploadTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtTestSupport jwt;

	private final ObjectMapper objectMapper = new ObjectMapper();

	private static final String CURRICULO_TXT = """
			ANA SOUZA
			ana.souza@recode.demo

			RESUMO
			Profissional de producao. 52 anos, nascida em 12/03/1974, idade 52.

			EXPERIENCIA PROFISSIONAL
			Supervisora de Producao - Industria Alfa - 2016 - 2023
			Lideranca de equipe de 30 operadores e metas de OEE
			- Analista de PCP | Metalurgica Sul | 2011 | 2016
			Programacao mestra e estoque

			FORMACAO ACADEMICA
			Tecnico em Logistica - Etec Municipal - 2019

			PROJETOS
			Reducao de perdas na linha
			Checklist visual - https://exemplo.demo/perdas
			""";

	private Long criarPerfil(RequestPostProcessor candidato) throws Exception {
		MvcResult criado = mockMvc.perform(post("/api/v1/perfis")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"nome": "Upload Test", "email": "upload@recode.demo",
								 "areaAtual": "Producao", "areaAlvo": "Logistica"}
								""")
						.with(candidato))
				.andExpect(status().isCreated())
				.andReturn();
		return objectMapper.readTree(criado.getResponse().getContentAsString()).get("id").asLong();
	}

	@Test
	void uploadTxtParseiaSecoesEDescartaLinhasDeIdade() throws Exception {
		RequestPostProcessor candidato = jwt.jwtCandidato(null);
		Long id = criarPerfil(candidato);
		mockMvc.perform(multipart("/api/v1/perfis/" + id + "/curriculo/arquivo")
						.file(new MockMultipartFile("arquivo", "cv-ana.txt", "text/plain", CURRICULO_TXT.getBytes()))
						.with(candidato))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.experiencias", hasSize(2)))
				.andExpect(jsonPath("$.experiencias[0].cargo").value("Supervisora de Producao"))
				.andExpect(jsonPath("$.experiencias[0].empresa").value("Industria Alfa"))
				.andExpect(jsonPath("$.experiencias[0].anoInicio").value(2016))
				.andExpect(jsonPath("$.experiencias[0].anoFim").value(2023))
				.andExpect(jsonPath("$.experiencias[1].cargo").value("Analista de PCP"))
				.andExpect(jsonPath("$.experiencias[1].anoFim").value(2016))
				.andExpect(jsonPath("$.formacoes", hasSize(1)))
				.andExpect(jsonPath("$.formacoes[0].curso").value("Tecnico em Logistica"))
				.andExpect(jsonPath("$.formacoes[0].anoConclusao").value(2019))
				.andExpect(jsonPath("$.projetos", hasSize(1)))
				.andExpect(jsonPath("$.projetos[0].link").value("https://exemplo.demo/perdas"))
				.andExpect(jsonPath("$.dataNascimento").doesNotExist())
				.andExpect(jsonPath("$.idade").doesNotExist());

		mockMvc.perform(multipart("/api/v1/perfis/" + id + "/curriculo")
						.file(new MockMultipartFile("arquivo", "cv-ana.txt", "text/plain", CURRICULO_TXT.getBytes()))
						.with(candidato))
				.andExpect(status().isMethodNotAllowed());
	}

	@Test
	void uploadPdfGeradoParseiaExperienciaEFormacao() throws Exception {
		RequestPostProcessor candidato = jwt.jwtCandidato(null);
		Long id = criarPerfil(candidato);
		byte[] pdf = pdfComLinhas(
				"EXPERIENCIA PROFISSIONAL",
				"Coordenadora de Logistica - Recode - 2020 - 2024",
				"Gestao de frota e roteirizacao",
				"FORMACAO",
				"Logistica - FACENS - 2015");
		mockMvc.perform(multipart("/api/v1/perfis/" + id + "/curriculo/arquivo")
						.file(new MockMultipartFile("arquivo", "cv.pdf", "application/pdf", pdf))
						.with(candidato))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.experiencias", hasSize(1)))
				.andExpect(jsonPath("$.experiencias[0].cargo").value("Coordenadora de Logistica"))
				.andExpect(jsonPath("$.experiencias[0].anoInicio").value(2020))
				.andExpect(jsonPath("$.experiencias[0].anoFim").value(2024))
				.andExpect(jsonPath("$.formacoes", hasSize(1)))
				.andExpect(jsonPath("$.formacoes[0].instituicao").value("FACENS"));
	}

	@Test
	void uploadDocxGeradoParseiaExperienciaEProjeto() throws Exception {
		RequestPostProcessor candidato = jwt.jwtCandidato(null);
		Long id = criarPerfil(candidato);
		byte[] docx = docxComParagrafos(
				"EXPERIENCIA PROFISSIONAL",
				"Vendedor Interno - Comercial Delta - 2010 - 2015",
				"Atendimento B2B",
				"PROJETOS",
				"Campanha de recorrencia - https://exemplo.demo/campanha");
		mockMvc.perform(multipart("/api/v1/perfis/" + id + "/curriculo/arquivo")
						.file(new MockMultipartFile("arquivo", "cv.docx",
								"application/vnd.openxmlformats-officedocument.wordprocessingml.document", docx))
						.with(candidato))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.experiencias", hasSize(1)))
				.andExpect(jsonPath("$.experiencias[0].cargo").value("Vendedor Interno"))
				.andExpect(jsonPath("$.projetos", hasSize(1)))
				.andExpect(jsonPath("$.projetos[0].link").value("https://exemplo.demo/campanha"));
	}

	@Test
	void textoSemSecoesReconheciveisDevolve400() throws Exception {
		RequestPostProcessor candidato = jwt.jwtCandidato(null);
		Long id = criarPerfil(candidato);
		mockMvc.perform(multipart("/api/v1/perfis/" + id + "/curriculo/arquivo")
						.file(new MockMultipartFile("arquivo", "cv.txt", "text/plain",
								"Lorem ipsum dolor sit amet, consectetur adipiscing elit.".getBytes()))
						.with(candidato))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.exception", containsString("identificar")));
	}

	@Test
	void arquivoVazioDevolve400() throws Exception {
		RequestPostProcessor candidato = jwt.jwtCandidato(null);
		Long id = criarPerfil(candidato);
		mockMvc.perform(multipart("/api/v1/perfis/" + id + "/curriculo/arquivo")
						.file(new MockMultipartFile("arquivo", "cv.txt", "text/plain", new byte[0]))
						.with(candidato))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.exception", containsString("vazio")));
	}

	@Test
	void semParteArquivoDevolve400() throws Exception {
		RequestPostProcessor candidato = jwt.jwtCandidato(null);
		mockMvc.perform(multipart("/api/v1/perfis/1/curriculo/arquivo")
						.with(candidato))
				.andExpect(status().isBadRequest());
	}

	@Test
	void perfilInexistenteDevolve404() throws Exception {
		mockMvc.perform(multipart("/api/v1/perfis/99999/curriculo/arquivo")
						.file(new MockMultipartFile("arquivo", "cv.txt", "text/plain", CURRICULO_TXT.getBytes()))
						.with(jwt.jwtRh()))
				.andExpect(status().isNotFound());
	}

	private byte[] pdfComLinhas(String... linhas) throws Exception {
		try (PDDocument doc = new PDDocument(); ByteArrayOutputStream saida = new ByteArrayOutputStream()) {
			PDPage pagina = new PDPage();
			doc.addPage(pagina);
			try (PDPageContentStream cs = new PDPageContentStream(doc, pagina)) {
				cs.beginText();
				cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
				cs.newLineAtOffset(50, 750);
				for (String linha : linhas) {
					cs.showText(linha);
					cs.newLineAtOffset(0, -16);
				}
				cs.endText();
			}
			doc.save(saida);
			return saida.toByteArray();
		}
	}

	private byte[] docxComParagrafos(String... paragrafos) throws Exception {
		try (XWPFDocument doc = new XWPFDocument(); ByteArrayOutputStream saida = new ByteArrayOutputStream()) {
			for (String p : paragrafos) {
				doc.createParagraph().createRun().setText(p);
			}
			doc.write(saida);
			return saida.toByteArray();
		}
	}
}
