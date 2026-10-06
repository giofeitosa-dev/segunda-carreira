package com.reconecta.ageblind.carreira.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reconecta.ageblind.carreira.domain.Competencia;

public interface CompetenciaRepository extends JpaRepository<Competencia, Long> {

	Optional<Competencia> findByNomeAndArea(String nome, String area);

	List<Competencia> findByAreaIgnoreCase(String area);
}
