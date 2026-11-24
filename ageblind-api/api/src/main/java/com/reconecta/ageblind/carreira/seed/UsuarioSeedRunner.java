package com.reconecta.ageblind.carreira.seed;

import com.reconecta.ageblind.auth.domain.Papel;
import com.reconecta.ageblind.auth.domain.Usuario;
import com.reconecta.ageblind.auth.repository.UsuarioRepository;
import com.reconecta.ageblind.carreira.domain.Perfil;
import com.reconecta.ageblind.carreira.repository.PerfilRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Seed de usuários demo (FR-012): candidato@demo.com (vinculado ao primeiro
 * perfil do seed) e rh@demo.com. Senhas conhecidas apenas de demo — em prod o
 * JwtConfig fail-fast já rejeita secrets default, e o profile "seed" não roda lá.
 *
 * Roda DEPOIS dos demais runners (@Order) para o Perfil já existir.
 */
@Component
@Profile("seed")
@Order(Ordered.LOWEST_PRECEDENCE)
public class UsuarioSeedRunner implements ApplicationRunner {

	private final UsuarioRepository usuarioRepository;
	private final PerfilRepository perfilRepository;
	private final String senhaCandidato;
	private final String senhaRh;

	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	public UsuarioSeedRunner(UsuarioRepository usuarioRepository, PerfilRepository perfilRepository,
			@Value("${app.seed.candidato.senha:demo123}") String senhaCandidato,
			@Value("${RH_PASSWORD:rh-demo}") String senhaRh) {
		this.usuarioRepository = usuarioRepository;
		this.perfilRepository = perfilRepository;
		this.senhaCandidato = senhaCandidato;
		this.senhaRh = senhaRh;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (usuarioRepository.existsByEmail("candidato@demo.com")) {
			return;
		}
		criar("candidato@demo.com", senhaCandidato, Papel.CANDIDATO, primeiroPerfil());
		criar("rh@demo.com", senhaRh, Papel.RH, null);
	}

	private void criar(String email, String senha, Papel papel, Perfil perfil) {
		Usuario usuario = new Usuario();
		usuario.setEmail(email);
		usuario.setSenha(passwordEncoder.encode(senha));
		usuario.setPapel(papel);
		usuario.setPerfil(perfil);
		usuarioRepository.save(usuario);
	}

	private Perfil primeiroPerfil() {
		return perfilRepository.findAll().stream()
				.sorted(java.util.Comparator.comparing(Perfil::getId))
				.findFirst()
				.orElse(null);
	}
}
