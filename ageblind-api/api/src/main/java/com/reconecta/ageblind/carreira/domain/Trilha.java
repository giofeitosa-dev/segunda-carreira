package com.reconecta.ageblind.carreira.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/** Trilha de capacitação curta para a área alvo. */
@Entity
@Table(name = "trilha")
public class Trilha {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String area;
	private String nome;
	private int duracaoSemanas;
	private BigDecimal custo;
	private String instituicao;
	private boolean certificado;

	@OneToMany(mappedBy = "trilha", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ModuloTrilha> modulos = new ArrayList<>();

	public Long getId() { return id; }
	public String getArea() { return area; }
	public void setArea(String area) { this.area = area; }
	public String getNome() { return nome; }
	public void setNome(String nome) { this.nome = nome; }
	public int getDuracaoSemanas() { return duracaoSemanas; }
	public void setDuracaoSemanas(int d) { this.duracaoSemanas = d; }
	public BigDecimal getCusto() { return custo; }
	public void setCusto(BigDecimal custo) { this.custo = custo; }
	public String getInstituicao() { return instituicao; }
	public void setInstituicao(String instituicao) { this.instituicao = instituicao; }
	public boolean isCertificado() { return certificado; }
	public void setCertificado(boolean certificado) { this.certificado = certificado; }
	public List<ModuloTrilha> getModulos() { return modulos; }
}
