package com.reconecta.ageblind.carreira.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "modulo_trilha")
public class ModuloTrilha {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "trilha_id")
	private Trilha trilha;

	private String nome;
	private int cargaHoraria;
	private int ordem;

	public Long getId() { return id; }
	public Trilha getTrilha() { return trilha; }
	public void setTrilha(Trilha trilha) { this.trilha = trilha; }
	public String getNome() { return nome; }
	public void setNome(String nome) { this.nome = nome; }
	public int getCargaHoraria() { return cargaHoraria; }
	public void setCargaHoraria(int c) { this.cargaHoraria = c; }
	public int getOrdem() { return ordem; }
	public void setOrdem(int ordem) { this.ordem = ordem; }
}
