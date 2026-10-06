package com.reconecta.ageblind.shared.api;

/**
 * Caminhos exigidos pela LGPD art. 20 nas respostas de decisão automatizada:
 * o titular precisa saber onde pedir revisão humana.
 */
public final class CaminhosLgpd {

	public static final String REVISAO_SOLICITACAO = "/api/v1/revisao/solicitacao";

	private CaminhosLgpd() {
	}
}
