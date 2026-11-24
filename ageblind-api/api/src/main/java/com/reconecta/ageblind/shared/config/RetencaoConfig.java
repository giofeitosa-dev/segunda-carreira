package com.reconecta.ageblind.shared.config;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.reconecta.ageblind.carreira.domain.Perfil;

/**
 * Classe de retenção de dados pessoais (LGPD art. 16 — minimização).
 * Identifica registros inativos elegíveis para purga/anonymização baseado
 * em {@code app.lgpd.retention.dias-inatividade}.
 * <p>
 * Tabelas de auditoria ({@code auditoria_seguranca}) são excluídas da purga
 * (imutáveis para accountability).
 */
@Component
public class RetencaoConfig {

	@Value("${app.lgpd.retention.dias-inatividade:365}")
	private int diasInatividade;

	/**
	 * Verifica se um perfil está elegível para purga/anonymização com base
	 * na inatividade (dias desde a criação ou última atualização).
	 * <p>
	 * Perfil nulo nunca é elegível. Perfil criado há menos de
	 * {@code diasInatividade} dias não é elegível.
	 *
	 * @param perfil   perfil a verificar (pode ser null)
	 * @param agora    data/hora atual para cálculo
	 * @return true se o perfil é elegível para purga
	 */
	public boolean elegivelParaPurga(Perfil perfil, LocalDateTime agora) {
		if (perfil == null) {
			return false;
		}
		LocalDateTime referencia = perfil.getCriadoEm() != null ? perfil.getCriadoEm() : agora;
		return referencia.plusDays(diasInatividade).isBefore(agora);
	}

	/**
	 * Indica se um tipo de dado está excluído da purga (imutável).
	 * Auditorias nunca entram na purga.
	 *
	 * @param tipoDado tipo de dado (ex.: "auditoria_seguranca")
	 * @return true se o tipo está excluído da purga
	 */
	public boolean excluidoDaPurga(String tipoDado) {
		return "auditoria_seguranca".equalsIgnoreCase(tipoDado);
	}

	public int getDiasInatividade() {
		return diasInatividade;
	}
}
