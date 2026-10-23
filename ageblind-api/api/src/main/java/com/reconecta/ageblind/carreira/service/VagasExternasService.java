package com.reconecta.ageblind.carreira.service;

import com.reconecta.ageblind.carreira.dto.RespostaBuscaExternaResponse;
import com.reconecta.ageblind.carreira.gateway.AdzunaGateway;
import com.reconecta.ageblind.shared.api.BuscaExternaException;
import com.reconecta.ageblind.shared.config.AdzunaProperties;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class VagasExternasService {

	private static final long TTL_CACHE_MS = 10 * 60 * 1000L;
	private static final int LIMITE_CACHE = 200;

	private final AdzunaGateway adzuna;
	private final AdzunaProperties properties;
	private final Map<String, EntradaCache> cache = new ConcurrentHashMap<>();

	public VagasExternasService(AdzunaGateway adzuna, AdzunaProperties properties) {
		this.adzuna = adzuna;
		this.properties = properties;
	}

	public RespostaBuscaExternaResponse buscar(String termo, String local, int pagina) {
		String termoNormalizado = termo == null ? "" : termo.trim();
		if (termoNormalizado.length() < 2) {
			throw new IllegalArgumentException("Informe um termo de busca com pelo menos 2 caracteres");
		}
		if (pagina < 1) {
			throw new IllegalArgumentException("Página deve ser maior ou igual a 1");
		}
		if (!properties.configurado()) {
			throw new BuscaExternaException(HttpStatus.SERVICE_UNAVAILABLE,
					"Provedor de vagas externas não configurado (defina ADZUNA_APP_ID e ADZUNA_APP_KEY)");
		}
		String localNormalizado = local == null ? "" : local.trim();
		String chave = termoNormalizado.toLowerCase() + "|" + localNormalizado.toLowerCase() + "|" + pagina;
		long agora = System.currentTimeMillis();
		EntradaCache entrada = cache.get(chave);
		if (entrada != null && agora - entrada.momento() < TTL_CACHE_MS) {
			return entrada.resposta();
		}
		AdzunaGateway.RespostaAdzuna resultado = adzuna.buscar(termoNormalizado, localNormalizado, pagina);
		RespostaBuscaExternaResponse resposta = new RespostaBuscaExternaResponse(termoNormalizado,
				localNormalizado, pagina, resultado.totalElementos(), resultado.vagas());
		if (cache.size() >= LIMITE_CACHE) {
			cache.clear();
		}
		cache.put(chave, new EntradaCache(resposta, agora));
		return resposta;
	}

	private record EntradaCache(RespostaBuscaExternaResponse resposta, long momento) {
	}
}
