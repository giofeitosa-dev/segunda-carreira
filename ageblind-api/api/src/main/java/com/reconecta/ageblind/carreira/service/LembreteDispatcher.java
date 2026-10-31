package com.reconecta.ageblind.carreira.service;

import com.reconecta.ageblind.carreira.domain.Inscricao;
import com.reconecta.ageblind.carreira.domain.Vaga;

public interface LembreteDispatcher {

	void despachar(Inscricao inscricao, Vaga vaga);
}