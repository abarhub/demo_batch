package com.example.batch.hello.job2.entity;

import io.searchbox.annotations.JestId;

/**
 * Created by Alain on 04/06/2017.
 */
public class JestOperation {

	@JestId
	private String documentId;

	private Long id;

	private String date;

	private String libelle;

	private double montant;

	public String getDocumentId() {
		return documentId;
	}

	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
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
}
