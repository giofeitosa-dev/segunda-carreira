package com.reconecta.ageblind.carreira.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reconecta.ageblind.carreira.domain.Mentoria;

public interface MentoriaRepository extends JpaRepository<Mentoria, Long> {

	List<Mentoria> findByAreaIgnoreCase(String area);
}
