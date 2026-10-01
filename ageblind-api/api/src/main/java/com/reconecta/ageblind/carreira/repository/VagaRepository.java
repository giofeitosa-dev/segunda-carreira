package com.reconecta.ageblind.carreira.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.reconecta.ageblind.carreira.domain.Vaga;
import com.reconecta.ageblind.carreira.enums.ModeloVaga;

public interface VagaRepository extends JpaRepository<Vaga, Long> {

	Page<Vaga> findByAreaIgnoreCase(String area, Pageable pageable);

	Page<Vaga> findByModelo(ModeloVaga modelo, Pageable pageable);

	Page<Vaga> findByAreaIgnoreCaseAndModelo(String area, ModeloVaga modelo, Pageable pageable);
}
