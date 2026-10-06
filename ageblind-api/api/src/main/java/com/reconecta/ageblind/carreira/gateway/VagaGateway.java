package com.reconecta.ageblind.carreira.gateway;

/**
 * Porta pública do módulo Carreira: consulta mínima de vaga por outros módulos.
 * No monolito é implementada localmente; se virar serviço separado,
 * troca-se a implementação por um RestClient sem tocar nos consumidores.
 */
public interface VagaGateway {

	boolean existe(Long vagaId);
}
