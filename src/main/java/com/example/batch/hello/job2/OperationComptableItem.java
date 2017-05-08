package com.example.batch.hello.job2;

import java.time.LocalDate;

/**
 * Created by Alain on 07/05/2017.
 */
public class OperationComptableItem {

	private LocalDate date;
	private String libelle;
	private double montant;
	private double montantFrancs;
	private String nomFichier;

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

	public String getNomFichier() {
		return nomFichier;
	}

	public void setNomFichier(String nomFichier) {
		this.nomFichier = nomFichier;
	}

	@Override
	public String toString() {
		return "OperationComptableItem{" +
				"date=" + date +
				", libelle='" + libelle + '\'' +
				", montant=" + montant +
				", montantFrancs=" + montantFrancs +
				", nomFichier='" + nomFichier + '\'' +
				'}';
	}
}
