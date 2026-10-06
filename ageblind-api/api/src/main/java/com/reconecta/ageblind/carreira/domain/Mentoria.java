package com.reconecta.ageblind.carreira.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Encaixe de mentoria entre pares por área (rede de apoio). */
@Entity
@Table(name = "mentoria")
public class Mentoria {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String mentor;
	private String area;
	private String disponibilidade;
	private String agendamento;

	public Long getId() { return id; }
	public String getMentor() { return mentor; }
	public void setMentor(String mentor) { this.mentor = mentor; }
	public String getArea() { return area; }
	public void setArea(String area) { this.area = area; }
	public String getDisponibilidade() { return disponibilidade; }
	public void setDisponibilidade(String d) { this.disponibilidade = d; }
	public String getAgendamento() { return agendamento; }
	public void setAgendamento(String a) { this.agendamento = a; }
}
