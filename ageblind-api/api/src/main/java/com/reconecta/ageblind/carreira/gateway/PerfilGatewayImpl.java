package com.reconecta.ageblind.carreira.gateway;

import com.reconecta.ageblind.carreira.repository.PerfilRepository;

import org.springframework.stereotype.Service;

@Service
public class PerfilGatewayImpl implements PerfilGateway {

	private final PerfilRepository perfilRepository;

	public PerfilGatewayImpl(PerfilRepository perfilRepository) {
		this.perfilRepository = perfilRepository;
	}

	@Override
	public boolean existe(Long perfilId) {
		return perfilRepository.existsById(perfilId);
	}
}
