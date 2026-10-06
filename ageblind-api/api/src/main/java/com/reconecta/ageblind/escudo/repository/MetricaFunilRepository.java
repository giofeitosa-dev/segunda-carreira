package com.reconecta.ageblind.escudo.repository;

import java.util.List;

import com.reconecta.ageblind.escudo.dominio.MetricaFunil;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MetricaFunilRepository extends JpaRepository<MetricaFunil, Long> {

	List<MetricaFunil> findByVagaIdOrderById(Long vagaId);

	List<MetricaFunil> findByVagaIdAndPeriodoOrderById(Long vagaId, String periodo);
}
