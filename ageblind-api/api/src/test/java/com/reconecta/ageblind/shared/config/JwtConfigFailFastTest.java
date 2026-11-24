package com.reconecta.ageblind.shared.config;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import org.springframework.mock.env.MockEnvironment;

/**
 * Fail-fast do JWT_SECRET em prod (R7 / FR-004): sem secret ou com default
 * conhecido, a aplicação não sobe no profile prod.
 */
class JwtConfigFailFastTest {

	@Test
	void prodSemSecretFalhaNoBoot() {
		MockEnvironment env = new MockEnvironment();
		env.addActiveProfile("prod");
		// secret não definido → default proibido entra em vigor → rejeitado em prod
		assertThatThrownBy(() -> new JwtConfig(env))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("default conhecido");
	}

	@Test
	void prodComSecretCurtoFalhaPeloTamanhoMinimo() {
		MockEnvironment env = new MockEnvironment();
		env.addActiveProfile("prod");
		env.setProperty("app.jwt.secret", "curto");
		assertThatThrownBy(() -> new JwtConfig(env))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("no mínimo 32 caracteres");
	}

	@Test
	void devComSecretCurtoFalhaPeloTamanhoMinimo() {
		MockEnvironment env = new MockEnvironment();
		// nenhum profile ativo -> não é prod
		env.setProperty("app.jwt.secret", "curto");
		assertThatThrownBy(() -> new JwtConfig(env))
				.isInstanceOf(IllegalStateException.class);
	}

	@Test
	void devComSecretValidoNaoFalha() {
		MockEnvironment env = new MockEnvironment();
		env.setProperty("app.jwt.secret", "um-secreto-com-mais-de-trinta-caracteres");
		new JwtConfig(env);
	}
}
