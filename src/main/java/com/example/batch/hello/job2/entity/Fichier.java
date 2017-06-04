package com.example.batch.hello.job2.entity;

import javax.persistence.Id;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Created by Alain on 07/05/2017.
 */
@Entity
public class Fichier {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name = "date")
	private LocalDate date;

	@Column(name = "nom_fichier",unique = true)
	private String nomFichier;

	@Column(name = "no_compte")
	private String noCompte;

	@Column(name = "solde")
	private double solde;

	@Column(name = "solde2")
	private double soldeFrancs;

	@OneToMany(mappedBy="fichier")
	private List<Operation> listeOperations;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public String getNomFichier() {
		return nomFichier;
	}

	public void setNomFichier(String nomFichier) {
		this.nomFichier = nomFichier;
	}

	public double getSolde() {
		return solde;
	}

	public void setSolde(double solde) {
		this.solde = solde;
	}

	public double getSoldeFrancs() {
		return soldeFrancs;
	}

	public void setSoldeFrancs(double soldeFrancs) {
		this.soldeFrancs = soldeFrancs;
	}

	public List<Operation> getListeOperations() {
		return listeOperations;
	}

	public void setListeOperations(List<Operation> listeOperations) {
		this.listeOperations = listeOperations;
	}

	public String getNoCompte() {
		return noCompte;
	}

	public void setNoCompte(String noCompte) {
		this.noCompte = noCompte;
	}

	@Override
	public String toString() {
		return "Fichier{" +
				"id=" + id +
				", date=" + date +
				", nomFichier='" + nomFichier + '\'' +
				", noCompte='" + noCompte + '\'' +
				", solde=" + solde +
				", soldeFrancs=" + soldeFrancs +
				'}';
	}
}
