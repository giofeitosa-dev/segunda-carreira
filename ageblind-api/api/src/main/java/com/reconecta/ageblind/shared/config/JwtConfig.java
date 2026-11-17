package com.reconecta.ageblind.shared.config;

import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

/**
 * Infraestrutura JWT HS256 (pesquisa R1): decoder/encoder com chave simétrica
 * de env e conversão da claim `papel` para authorities ROLE_*.
 *
 * Fail-fast (R7 / FR-004): no profile `prod` o secret precisa existir,
 * ter ≥32 chars e não ser nenhum default conhecido — a app não sobe fraca.
 */
@Configuration
public class JwtConfig {

	/** Defaults conhecidos que NUNCA podem ir para prod. */
	private static final List<String> SECRETS_PROIBIDOS = List.of(
			"changeme", "secret", "password", "ageblind-demo-secret-key-2026-001");

	private final String secret;
	private final long expirationMs;
	private final Environment environment;

	public JwtConfig(
			@Value("${app.jwt.secret}") String secret,
			@Value("${app.jwt.expiration}") long expirationMs,
			Environment environment) {
		this.secret = secret;
		this.expirationMs = expirationMs;
		this.environment = environment;
		validarSegredo();
	}

	private void validarSegredo() {
		// HS256 exige ≥256 bits em QUALQUER profile (Nimbus valida no encode —
		// falhar no boot é melhor que falhar no primeiro login).
		if (secret == null || secret.length() < 32) {
			throw new IllegalStateException(
					"app.jwt.secret deve ter no mínimo 32 caracteres (HS256 exige 256 bits).");
		}
		boolean emProd = List.of(environment.getActiveProfiles()).contains("prod");
		if (!emProd) {
			return;
		}
		if (SECRETS_PROIBIDOS.contains(secret)) {
			throw new IllegalStateException(
					"JWT_SECRET é um default conhecido e foi rejeitado no profile prod.");
		}
	}

	@Bean
	JwtDecoder jwtDecoder() {
		return NimbusJwtDecoder.withSecretKey(chaveSimetrica())
				.macAlgorithm(MacAlgorithm.HS256)
				.build();
	}

	@Bean
	JwtEncoder jwtEncoder() {
		return new NimbusJwtEncoder(new ImmutableSecret<>(chaveSimetrica()));
	}

	/**
	 * Authorities a partir da claim `papel` (CANDIDATO→ROLE_CANDIDATO etc.).
	 * A claim `papel` é preenchida por AuthService na emissão.
	 */
	@Bean
	JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
		converter.setJwtGrantedAuthoritiesConverter(jwt -> {
			String papel = jwt.getClaim("papel");
			if (papel == null || papel.isBlank()) {
				return List.of();
			}
			return List.of(() -> "ROLE_" + papel);
		});
		converter.setPrincipalClaimName(JwtClaimNames.SUB);
		return converter;
	}

	private SecretKeySpec chaveSimetrica() {
		return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
	}

	/** TTL do access token em segundos (campo expiresIn da resposta de login). */
	public long getExpirationSegundos() {
		return expirationMs / 1000;
	}

	/** Expiração em ms para a claim exp. */
	public long getExpirationMs() {
		return expirationMs;
	}
}
