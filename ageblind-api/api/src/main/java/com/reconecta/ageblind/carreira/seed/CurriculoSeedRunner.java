package com.reconecta.ageblind.carreira.seed;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import com.reconecta.ageblind.carreira.domain.Certificacao;
import com.reconecta.ageblind.carreira.domain.Experiencia;
import com.reconecta.ageblind.carreira.domain.Formacao;
import com.reconecta.ageblind.carreira.domain.Idioma;
import com.reconecta.ageblind.carreira.domain.Perfil;
import com.reconecta.ageblind.carreira.domain.Projeto;
import com.reconecta.ageblind.carreira.enums.NivelIdioma;
import com.reconecta.ageblind.carreira.repository.PerfilRepository;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seed do currículo estruturado (experiências, formações, projetos) a partir de
 * data/{experiencias,formacoes,projetos}-seed.csv. Ativo apenas com o profile "seed".
 * Roda em ApplicationReadyEvent — depois de todos os ApplicationRunner (cria os perfis).
 */
@Component
@Profile("seed")
public class CurriculoSeedRunner {

	private final PerfilRepository perfilRepository;

	public CurriculoSeedRunner(PerfilRepository perfilRepository) {
		this.perfilRepository = perfilRepository;
	}

	@Transactional
	@EventListener(ApplicationReadyEvent.class)
	public void carregar() {
		Map<Long, Perfil> perfis = new HashMap<>();
		for (Perfil p : perfilRepository.findAll()) {
			perfis.put(p.getId(), p);
			if (!p.getExperiencias().isEmpty()) {
				return;
			}
		}
		carregarArquivo("data/experiencias-seed.csv", perfis, this::experiencia);
		carregarArquivo("data/formacoes-seed.csv", perfis, this::formacao);
		carregarArquivo("data/projetos-seed.csv", perfis, this::projeto);
		carregarArquivo("data/idiomas-seed.csv", perfis, this::idioma);
		carregarArquivo("data/certificacoes-seed.csv", perfis, this::certificacao);
		perfilRepository.saveAll(perfis.values());
	}

	private void carregarArquivo(String caminho, Map<Long, Perfil> perfis,
			BiConsumer<Perfil, String[]> aoLer) {
		try (BufferedReader leitor = new BufferedReader(new InputStreamReader(
				new ClassPathResource(caminho).getInputStream(), StandardCharsets.UTF_8))) {
			leitor.readLine();
			String linha;
			while ((linha = leitor.readLine()) != null && !linha.isBlank()) {
				Perfil perfil = perfis.get(Long.parseLong(linha.split("\\|", -1)[0]));
				if (perfil != null) {
					aoLer.accept(perfil, linha.split("\\|", -1));
				}
			}
		} catch (Exception e) {
			throw new IllegalStateException("Falha ao carregar seed de curriculo: " + caminho, e);
		}
	}

	private void experiencia(Perfil perfil, String[] c) {
		Experiencia e = new Experiencia();
		e.setPerfil(perfil);
		e.setCargo(c[1]);
		e.setEmpresa(c[2]);
		e.setAnoInicio(Integer.parseInt(c[3]));
		e.setAnoFim(c[4].isBlank() ? null : Integer.valueOf(c[4]));
		e.setDescricao(c[5]);
		perfil.getExperiencias().add(e);
	}

	private void formacao(Perfil perfil, String[] c) {
		Formacao f = new Formacao();
		f.setPerfil(perfil);
		f.setCurso(c[1]);
		f.setInstituicao(c[2]);
		f.setAnoConclusao(c[3].isBlank() ? null : Integer.valueOf(c[3]));
		f.setDescricao(c[4]);
		perfil.getFormacoes().add(f);
	}

	private void projeto(Perfil perfil, String[] c) {
		Projeto p = new Projeto();
		p.setPerfil(perfil);
		p.setNome(c[1]);
		p.setDescricao(c[2]);
		p.setLink(c[3].isBlank() ? null : c[3]);
		perfil.getProjetos().add(p);
	}

	private void idioma(Perfil perfil, String[] c) {
		Idioma i = new Idioma();
		i.setPerfil(perfil);
		i.setIdioma(c[1]);
		i.setNivel(NivelIdioma.valueOf(c[2]));
		perfil.getIdiomas().add(i);
	}

	private void certificacao(Perfil perfil, String[] c) {
		Certificacao cert = new Certificacao();
		cert.setPerfil(perfil);
		cert.setNome(c[1]);
		cert.setInstituicao(c[2]);
		cert.setAnoConclusao(c[3].isBlank() ? null : Integer.valueOf(c[3]));
		cert.setDescricao(c[4]);
		cert.setLink(c[5].isBlank() ? null : c[5]);
		perfil.getCertificacoes().add(cert);
	}
}
