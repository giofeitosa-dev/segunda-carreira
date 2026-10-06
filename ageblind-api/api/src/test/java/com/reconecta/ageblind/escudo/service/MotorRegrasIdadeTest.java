package com.reconecta.ageblind.escudo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import com.reconecta.ageblind.escudo.dominio.SinalIdade;
import com.reconecta.ageblind.escudo.dto.RegraAplicada;

import org.junit.jupiter.api.Test;

class MotorRegrasIdadeTest {

	private final MotorRegrasIdade motor = new MotorRegrasIdade();

	private static final String CURRICULO_ANTES = """
			João Silva — Profissional veterano e maduro
			Data de nascimento: 12/03/1968 (57 anos)
			![Foto](foto.png)
			Graduação em Administração — 1998
			15+ anos de experiência em logística
			Analista de Operações (1998–2014)
			Planejamento (2014 – Presente)
			Certificado PMP 2012
			Inglês: intermediário 2010
			Previdência social: contribuinte ativo
			""";

	@Test
	void carregaAs15Regras() {
		assertEquals(15, motor.regras().size());
	}

	@Test
	void aplicarRemoveOsPrincipaisProxies() {
		MotorRegrasIdade.ResultadoAgeBlind r = motor.aplicar(CURRICULO_ANTES);
		String saida = r.conteudo();

		assertFalse(saida.contains("1968"), "deve remover data de nascimento");
		assertFalse(saida.contains("57 anos"), "deve remover idade");
		assertFalse(saida.contains("1998"), "deve remover ano de graduação e datas antigas");
		assertFalse(saida.contains("15+"), "deve remover total de anos de experiência");
		assertFalse(saida.contains("2012"), "deve remover ano de certificação");
		assertFalse(saida.contains("2010"), "deve remover ano de idioma");
		assertFalse(saida.contains("veterano"), "deve remover adjetivo geracional");
		assertFalse(saida.contains("foto.png"), "deve remover foto");
		assertFalse(saida.contains("Previdência"), "deve remover menção previdenciária");

		assertTrue(saida.contains("Graduação em Administração"), "mantém o curso");
		assertTrue(saida.contains("Inglês: intermediário"), "mantém o nível do idioma");
		assertTrue(saida.contains("2014 – Presente"), "mantém datas relativas abertas (regra 8)");
	}

	@Test
	void listarTodasAs15RegrasComStatus() {
		MotorRegrasIdade.ResultadoAgeBlind r = motor.aplicar(CURRICULO_ANTES);
		List<RegraAplicada> aplicadas = r.regrasAplicadas();

		assertEquals(15, aplicadas.size());
		assertTrue(aplicadas.stream().anyMatch(a -> a.acao().contains("aplicado automaticamente")));
		assertTrue(aplicadas.stream().anyMatch(a -> a.acao().contains("ação manual")));
		assertTrue(aplicadas.stream().anyMatch(a -> a.regra().startsWith("R01")));
	}

	@Test
	void detectarEncontraSinaisSemAlterarTexto() {
		List<SinalIdade> sinais = motor.detectar(CURRICULO_ANTES);
		assertTrue(sinais.stream().anyMatch(s -> s.codigo().equals("R01")), "sinal de nascimento/idade");
		assertTrue(sinais.stream().anyMatch(s -> s.codigo().equals("R03")), "sinal de ano de graduação");
		assertTrue(sinais.size() >= 6, "deve achar múltiplos proxies");
	}

	@Test
	void aplicarEmCurriculoJaLimpoNaoQuebra() {
		MotorRegrasIdade.ResultadoAgeBlind r =
				motor.aplicar("Maria Souza\nEspecialista em Logística\nCertificado PMP\nInglês: avançado");
		assertFalse(r.conteudo().contains("19"));
		assertEquals(15, r.regrasAplicadas().size());
	}

	@Test
	void aplicarToleraTextoSemAcentos() {
		MotorRegrasIdade.ResultadoAgeBlind r = motor.aplicar("""
				Graduacao em Administracao - 1998
				15+ anos de experiencia em logistica
				Ingles: intermediario 2010
				""");
		assertFalse(r.conteudo().contains("1998"), "sem acento deve casar R03");
		assertFalse(r.conteudo().contains("15+"), "sem acento deve casar R04");
		assertFalse(r.conteudo().contains("2010"), "sem acento deve casar R15");
		assertTrue(r.conteudo().contains("Graduacao em Administracao"), "mantém o curso");
	}
}
