package com.reconecta.ageblind.carreira.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import com.reconecta.ageblind.carreira.domain.Perfil;
import com.reconecta.ageblind.carreira.enums.Disponibilidade;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class PerfilRepositoryTest {

	@Autowired
	private PerfilRepository perfilRepository;

	@Test
	void salvaERecuperaPerfil() {
		String emailUnico = "joao-" + java.util.UUID.randomUUID() + "@example.com";
		Perfil p = new Perfil();
		p.setNome("João Silva");
		p.setEmail(emailUnico);
		p.setAreaAtual("PCP");
		p.setAreaAlvo("LOGISTICA");
		p.setPretensaoSalarial(new BigDecimal("4500"));
		p.getDisponibilidade().add(Disponibilidade.REMOTO);

		Perfil salvo = perfilRepository.save(p);
		perfilRepository.flush();

		assertTrue(salvo.getId() != null);
		assertTrue(perfilRepository.findByEmail(emailUnico).isPresent());
		assertEquals("LOGISTICA", perfilRepository.findById(salvo.getId()).get().getAreaAlvo());
	}

	@Test
	void entidadeNaoTemCamposDeIdade() {
		java.util.List<String> proibidos = java.util.List.of("idade", "age", "nascimento", "birthdate", "birth");
		boolean temIdade = java.util.Arrays.stream(Perfil.class.getDeclaredFields())
				.flatMap(f -> java.util.Arrays.stream(f.getName().split("(?<=[a-z])(?=[A-Z])|_")))
				.map(t -> t.toLowerCase())
				.anyMatch(proibidos::contains);
		assertFalse(temIdade, "Perfil não pode ter campos de idade/data de nascimento");
	}
}
