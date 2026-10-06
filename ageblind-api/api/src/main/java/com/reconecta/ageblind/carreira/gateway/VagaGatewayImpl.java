package com.reconecta.ageblind.carreira.gateway;

import com.reconecta.ageblind.carreira.repository.VagaRepository;

import org.springframework.stereotype.Service;

@Service
public class VagaGatewayImpl implements VagaGateway {

	private final VagaRepository vagaRepository;

	public VagaGatewayImpl(VagaRepository vagaRepository) {
		this.vagaRepository = vagaRepository;
	}

	@Override
	public boolean existe(Long vagaId) {
		return vagaRepository.existsById(vagaId);
	}
}
