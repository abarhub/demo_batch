package com.example.batch.hello.job2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Created by Alain on 07/05/2017.
 */
@Component
public class CsvProcessor implements ItemProcessor<CsvItem, OperationComptableItem> {

	private static final Logger log = LoggerFactory.getLogger(CsvProcessor.class);

	private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	@Override
	public OperationComptableItem process(final CsvItem csv) throws Exception {

		log.info("csv=" + csv);
		OperationComptableItem operationComptable = new OperationComptableItem();
		operationComptable.setDate(parseDate(csv.getDate()));
		operationComptable.setLibelle(csv.getLibelle());
		operationComptable.setMontant(parseDouble(csv.getMontant()));
		operationComptable.setMontantFrancs(parseDouble(csv.getMontant2()));
		String nomFichier = csv.nomFichier();
		if (nomFichier != null && nomFichier.length() > 0) {
			operationComptable.setNomFichier(nomFichier);
		}
		return operationComptable;
	}

	private LocalDate parseDate(String s) {
		return LocalDate.parse(s, formatter);
	}

	private double parseDouble(String s) {
		if (s == null || s.trim().length() == 0) {
			return 0.0;
		} else {
			String s2 = s.trim().replace(',', '.');
			s2 = s2.replace(" ", "");
			return Double.parseDouble(s2);
		}
	}

}
