package com.example.batch.hello.job2;

import org.springframework.batch.item.ResourceAware;
import org.springframework.core.io.Resource;

/**
 * Created by Alain on 07/05/2017.
 */
public class CsvItem implements ResourceAware {

	private String date;
	private String libelle;
	private String montant;
	private String montant2;
	private Resource resource;

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

	public String getMontant() {
		return montant;
	}

	public void setMontant(String montant) {
		this.montant = montant;
	}

	public String getMontant2() {
		return montant2;
	}

	public void setMontant2(String montant2) {
		this.montant2 = montant2;
	}

	@Override
	public void setResource(Resource resource) {
		this.resource=resource;
	}

	public String nomFichier(){
		return resource.getFilename();
	}


	@Override
	public String toString() {
		return "CsvItem{" +
				"date='" + date + '\'' +
				", libelle='" + libelle + '\'' +
				", montant='" + montant + '\'' +
				", montant2='" + montant2 + '\'' +
				", resource=" + resource +
				'}';
	}

}
