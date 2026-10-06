package com.reconecta.ageblind.carreira.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reconecta.ageblind.carreira.domain.Inscricao;

public interface InscricaoRepository extends JpaRepository<Inscricao, Long> {

	List<Inscricao> findByPerfilId(Long perfilId);

	List<Inscricao> findByLembreteAgendadoFalse();

	List<Inscricao> findByLembreteAgendadoTrue();
}
