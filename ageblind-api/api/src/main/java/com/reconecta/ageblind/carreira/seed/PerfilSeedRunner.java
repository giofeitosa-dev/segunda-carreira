package com.reconecta.ageblind.carreira.seed;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import com.reconecta.ageblind.carreira.domain.Competencia;
import com.reconecta.ageblind.carreira.domain.Perfil;
import com.reconecta.ageblind.carreira.domain.PerfilCompetencia;
import com.reconecta.ageblind.carreira.enums.Disponibilidade;
import com.reconecta.ageblind.carreira.enums.StatusCuradoria;
import com.reconecta.ageblind.carreira.repository.CompetenciaRepository;
import com.reconecta.ageblind.carreira.repository.PerfilRepository;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * Seed demo de 10 perfis com competências (D-20), a partir de
 * data/perfis-seed.csv. Ativo apenas com o profile "seed".
 */
@Component
@Profile("seed")
public class PerfilSeedRunner implements ApplicationRunner {

	private final PerfilRepository perfilRepository;
	private final CompetenciaRepository competenciaRepository;

	public PerfilSeedRunner(PerfilRepository perfilRepository, CompetenciaRepository competenciaRepository) {
		this.perfilRepository = perfilRepository;
		this.competenciaRepository = competenciaRepository;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		if (perfilRepository.count() > 0) {
			return;
		}
		try (BufferedReader leitor = new BufferedReader(new InputStreamReader(
				new ClassPathResource("data/perfis-seed.csv").getInputStream(), StandardCharsets.UTF_8))) {
			leitor.readLine();
			String linha;
			while ((linha = leitor.readLine()) != null && !linha.isBlank()) {
				String[] c = linha.split("\\|", -1);
				Perfil perfil = new Perfil();
				perfil.setNome(c[0]);
				perfil.setEmail(c[1]);
				perfil.setAreaAtual(c[2]);
				perfil.setAreaAlvo(c[3]);
				perfil.setPretensaoSalarial(new BigDecimal(c[4]));
				for (String disp : c[5].split(";")) {
					if (!disp.isBlank()) {
						perfil.getDisponibilidade().add(Disponibilidade.valueOf(disp.trim()));
					}
				}
				perfil.setStatusCuradoria(StatusCuradoria.valueOf(c[6]));
				for (String comp : c[7].split(";")) {
					String[] partes = comp.trim().split(":");
					Competencia competencia = buscarOuCriar(partes[0].trim(), c[3]);
					PerfilCompetencia pc = new PerfilCompetencia();
					pc.setPerfil(perfil);
					pc.setCompetencia(competencia);
					pc.setNivel(Integer.parseInt(partes[1].trim()));
					perfil.getCompetencias().add(pc);
				}
				perfilRepository.save(perfil);
			}
		}
	}

	private Competencia buscarOuCriar(String nome, String area) {
		return competenciaRepository.findByNomeAndArea(nome, area)
				.orElseGet(() -> competenciaRepository.save(new Competencia(nome, area)));
	}
}
