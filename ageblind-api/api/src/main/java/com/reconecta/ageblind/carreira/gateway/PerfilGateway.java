package com.reconecta.ageblind.carreira.gateway;

/** Porta pública do módulo Carreira para existência de perfil. */
public interface PerfilGateway {

	boolean existe(Long perfilId);
}
