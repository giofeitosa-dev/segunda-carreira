package com.reconecta.ageblind.carreira.service;

import com.reconecta.ageblind.carreira.domain.Inscricao;
import com.reconecta.ageblind.carreira.domain.Vaga;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LogLembreteDispatcher implements LembreteDispatcher {

	private static final Logger log = LoggerFactory.getLogger(LogLembreteDispatcher.class);

	public void despachar(Inscricao inscricao, Vaga vaga) {
		var perfil = inscricao.getPerfil();
		log.info(
			"[LEMBRETE] DISPARADO | perfil={} {} {} | vaga={} | prazo={} | inscricao={}",
			perfil.getId(),
			perfil.getNome(),
			perfil.getEmail(),
			vaga.getTitulo(),
			vaga.getPrazoInscricao(),
			inscricao.getId()
		);
	}
}