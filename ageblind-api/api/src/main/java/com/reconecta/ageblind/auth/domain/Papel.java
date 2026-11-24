package com.reconecta.ageblind.auth.domain;

/**
 * Papel do usuário autenticável. EMPRESA existe no enum para evolução
 * futura sem migração (adiado no ROADMAP — ciclo 001 não expõe endpoints).
 */
public enum Papel {
	CANDIDATO, RH, EMPRESA
}
