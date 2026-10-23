package com.reconecta.ageblind.carreira.gateway;

import tools.jackson.databind.JsonNode;
import com.reconecta.ageblind.carreira.dto.VagaExternaResponse;
import com.reconecta.ageblind.shared.api.BuscaExternaException;
import com.reconecta.ageblind.shared.config.AdzunaProperties;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class AdzunaGateway {

	private final RestClient restClient;
	private final AdzunaProperties properties;

	public AdzunaGateway(AdzunaProperties properties) {
		this.properties = properties;
		this.restClient = RestClient.builder().baseUrl("https://api.adzuna.com").build();
	}

	public RespostaAdzuna buscar(String termo, String local, int pagina) {
		try {
			JsonNode corpo = restClient.get()
					.uri(uriBuilder -> uriBuilder
							.path("/v1/api/jobs/{pais}/search/{pagina}")
							.queryParam("app_id", properties.getAppId())
							.queryParam("app_key", properties.getAppKey())
							.queryParam("what", termo)
							.queryParam("content-type", "application/json")
							.queryParam("results_per_page", 20)
							.queryParamIfPresent("where",
									Optional.ofNullable(local == null || local.isBlank() ? null : local))
							.build(properties.getPais(), pagina))
					.retrieve()
					.body(JsonNode.class);
			return mapear(corpo);
		} catch (RestClientException e) {
			throw new BuscaExternaException(HttpStatus.BAD_GATEWAY,
					"Falha ao consultar o provedor de vagas externas");
		}
	}

	public record RespostaAdzuna(long totalElementos, List<VagaExternaResponse> vagas) {
	}

	static RespostaAdzuna mapear(JsonNode corpo) {
		List<VagaExternaResponse> vagas = new ArrayList<>();
		JsonNode resultados = corpo == null ? null : corpo.path("results");
		if (resultados != null && resultados.isArray()) {
			for (JsonNode item : resultados) {
				vagas.add(new VagaExternaResponse(
						item.path("id").asString(null),
						item.path("title").asString(null),
						item.path("company").path("display_name").asString(null),
						item.path("location").path("display_name").asString(null),
						item.path("redirect_url").asString(null),
						item.path("created").asString(null),
						salario(item.path("salary_min")),
						salario(item.path("salary_max")),
						item.path("description").asString(null),
						"Adzuna"));
			}
		}
		long total = corpo == null ? 0 : corpo.path("count").asLong(vagas.size());
		return new RespostaAdzuna(total, vagas);
	}

	private static BigDecimal salario(JsonNode campo) {
		if (!campo.isNumber()) {
			return null;
		}
		BigDecimal valor = campo.decimalValue();
		return valor.signum() <= 0 ? null : valor;
	}
}
