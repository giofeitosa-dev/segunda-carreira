package com.reconecta.ageblind.auth.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.reconecta.ageblind.auth.domain.AuditoriaSeguranca;

/**
 * Repositório de registros de auditoria de segurança (imutável — sem update/delete).
 */
@Repository
public interface AuditoriaSegurancaRepository extends JpaRepository<AuditoriaSeguranca, Long> {

	Page<AuditoriaSeguranca> findAll(Pageable pageable);

	@Query("SELECT a FROM AuditoriaSeguranca a WHERE a.usuarioId = :usuarioId ORDER BY a.dataHora DESC")
	Page<AuditoriaSeguranca> findByUsuarioId(@Param("usuarioId") Long usuarioId, Pageable pageable);

	@Query("SELECT a FROM AuditoriaSeguranca a WHERE a.acao = :acao ORDER BY a.dataHora DESC")
	Page<AuditoriaSeguranca> findByAcao(@Param("acao") String acao, Pageable pageable);

	List<AuditoriaSeguranca> findTop10ByOrderByDataHoraDesc();
}
