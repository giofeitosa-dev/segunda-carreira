package com.reconecta.ageblind.carreira.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reconecta.ageblind.carreira.domain.Perfil;

public interface PerfilRepository extends JpaRepository<Perfil, Long> {

	Optional<Perfil> findByEmail(String email);
}
