package com.reconecta.ageblind.carreira.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reconecta.ageblind.carreira.domain.Trilha;

public interface TrilhaRepository extends JpaRepository<Trilha, Long> {

	List<Trilha> findByAreaIgnoreCase(String area);
}
