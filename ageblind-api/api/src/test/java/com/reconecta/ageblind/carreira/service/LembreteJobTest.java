package com.reconecta.ageblind.carreira.service;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.reconecta.ageblind.carreira.domain.Inscricao;
import com.reconecta.ageblind.carreira.domain.Vaga;
import com.reconecta.ageblind.carreira.enums.TipoInscricao;
import com.reconecta.ageblind.carreira.repository.InscricaoRepository;
import com.reconecta.ageblind.carreira.repository.VagaRepository;

class LembreteJobTest {

	private InscricaoRepository inscricaoRepository;
	private VagaRepository vagaRepository;
	private LembreteDispatcher dispatcher;
	private LembreteJob lembreteJob;

	@BeforeEach
	void setUp() {
		inscricaoRepository = mock(InscricaoRepository.class);
		vagaRepository = mock(VagaRepository.class);
		dispatcher = mock(LembreteDispatcher.class);
		lembreteJob = new LembreteJob(inscricaoRepository, vagaRepository, dispatcher);
	}

	@Test
	void deverIdentificarEDispatcharLembreteQuandoVigente() {
		Inscricao inscricao = new Inscricao();
		inscricao.setTipo(TipoInscricao.VAGA);
		inscricao.setLembreteAgendado(true);
		inscricao.setReferenciaId("1");
		inscricao.setLembreteAntecedenciaHoras(48);

		Vaga vaga = new Vaga();
		vaga.setTitulo("Desenvolvedor Java");
		vaga.setPrazoInscricao(LocalDateTime.now().plusDays(2));

		when(inscricaoRepository.findByLembreteAgendadoTrue()).thenReturn(List.of(inscricao));
		when(vagaRepository.findById(1L)).thenReturn(java.util.Optional.of(vaga));

		lembreteJob.disparar();

		verify(dispatcher).despachar(inscricao, vaga);
		verify(inscricaoRepository).save(inscricao);
		assert inscricao.isLembreteAgendado() == false;
	}

	@Test
	void naoDispatcharQuandoForaDaJanela() {
		Inscricao inscricao = new Inscricao();
		inscricao.setTipo(TipoInscricao.VAGA);
		inscricao.setLembreteAgendado(true);
		inscricao.setReferenciaId("2");
		inscricao.setLembreteAntecedenciaHoras(48);

		Vaga vaga = new Vaga();
		vaga.setTitulo("Designer Pleno");
		vaga.setPrazoInscricao(LocalDateTime.now().minusDays(1));

		when(inscricaoRepository.findByLembreteAgendadoTrue()).thenReturn(List.of(inscricao));
		when(vagaRepository.findById(2L)).thenReturn(java.util.Optional.of(vaga));

		lembreteJob.disparar();

		verify(dispatcher, never()).despachar(any(), any());
		verify(inscricaoRepository, never()).save(any());
	}
}