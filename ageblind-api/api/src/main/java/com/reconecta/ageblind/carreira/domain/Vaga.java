package com.reconecta.ageblind.carreira.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.reconecta.ageblind.carreira.enums.ModeloVaga;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/** Vaga pública — sem requisito de idade (CLT art. 373-A). */
@Entity
@Table(name = "vaga")
public class Vaga {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String titulo;
	private String empresa;
	private String area;
	private String descricao;
	private BigDecimal salario;

	@Enumerated(EnumType.STRING)
	private ModeloVaga modelo;

	private LocalDateTime prazoInscricao;

	/** CSV aberto, concurso etc. */
	private String fonte;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "vaga_competencia",
			joinColumns = @JoinColumn(name = "vaga_id"),
			inverseJoinColumns = @JoinColumn(name = "competencia_id"))
	private Set<Competencia> requisitos = new HashSet<>();

	private LocalDateTime criadoEm;

	@PrePersist
	void aoPersistir() {
		this.criadoEm = LocalDateTime.now();
	}

	public Long getId() { return id; }
	public String getTitulo() { return titulo; }
	public void setTitulo(String titulo) { this.titulo = titulo; }
	public String getEmpresa() { return empresa; }
	public void setEmpresa(String empresa) { this.empresa = empresa; }
	public String getArea() { return area; }
	public void setArea(String area) { this.area = area; }
	public String getDescricao() { return descricao; }
	public void setDescricao(String descricao) { this.descricao = descricao; }
	public BigDecimal getSalario() { return salario; }
	public void setSalario(BigDecimal salario) { this.salario = salario; }
	public ModeloVaga getModelo() { return modelo; }
	public void setModelo(ModeloVaga modelo) { this.modelo = modelo; }
	public LocalDateTime getPrazoInscricao() { return prazoInscricao; }
	public void setPrazoInscricao(LocalDateTime p) { this.prazoInscricao = p; }
	public String getFonte() { return fonte; }
	public void setFonte(String fonte) { this.fonte = fonte; }
	public Set<Competencia> getRequisitos() { return requisitos; }
	public LocalDateTime getCriadoEm() { return criadoEm; }
}
