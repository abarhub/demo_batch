package com.example.batch.hello.job2.entity;

import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.LocalDate;

/**
 * Created by Alain on 07/05/2017.
 */
@Entity
public class Operation2 {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name = "date")
	private LocalDate date;

	@Column(name = "libelle")
	private String libelle;

	@Column(name = "montant")
	private double montant;

	@Column(name = "montant2")
	private double montantFrancs;

	@Column(name = "no_compte")
	private String noCompte;

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

	public String getLibelle() {
		return libelle;
	}

	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}

	public double getMontant() {
		return montant;
	}

	public void setMontant(double montant) {
		this.montant = montant;
	}

	public double getMontantFrancs() {
		return montantFrancs;
	}

	public void setMontantFrancs(double montantFrancs) {
		this.montantFrancs = montantFrancs;
	}

	public String getNoCompte() {
		return noCompte;
	}

	public void setNoCompte(String noCompte) {
		this.noCompte = noCompte;
	}

	@Override
	public String toString() {
		return "Operation2{" +
				"id=" + id +
				", date=" + date +
				", libelle='" + libelle + '\'' +
				", montant=" + montant +
				", montantFrancs=" + montantFrancs +
				", noCompte='" + noCompte + '\'' +
				'}';
	}
}
