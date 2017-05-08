package com.example.batch.hello.job2;

import com.example.batch.hello.job2.entity.Fichier;
import com.example.batch.hello.job2.entity.Operation;
import com.example.batch.hello.job2.repository.FichierRepository;
import com.example.batch.hello.job2.repository.OperationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by Alain on 07/05/2017.
 */
@Component
@Transactional
public class WriterJpa implements ItemWriter<OperationComptableItem> {

	private static final Logger log = LoggerFactory.getLogger(WriterJpa.class);

	@Autowired
	private OperationRepository operationRepository;

	@Autowired
	private FichierRepository fichierRepository;

	@Override
	public void write(List<? extends OperationComptableItem> list) throws Exception {
		log.info("save ...");
		for (OperationComptableItem o : list) {
			Operation operation = conv(o);
			operationRepository.save(operation);
		}
		log.info("save ok");
	}

	private Operation conv(OperationComptableItem o) {
		Fichier f = getFichier(o);
		Operation o2 = new Operation();
		o2.setDate(o.getDate());
		o2.setLibelle(o.getLibelle());
		o2.setMontant(o.getMontant());
		o2.setMontantFrancs(o.getMontantFrancs());
		o2.setIgnorer(false);
		if (f != null) {
			o2.setFichier(f);
			if (f.getListeOperations() == null) {
				f.setListeOperations(new ArrayList<>());
			}
			f.getListeOperations().add(o2);
		}
		return o2;
	}

	private Fichier getFichier(OperationComptableItem o) {
		String nomFichier = o.getNomFichier();

		if (nomFichier != null && nomFichier.trim().length() > 0) {
			Optional<Fichier> optFichier = fichierRepository.findByNomFichier(nomFichier);
			if (optFichier.isPresent()) {
				return optFichier.get();
			} else {
				Fichier f = new Fichier();
				f.setNomFichier(nomFichier);
				f.setListeOperations(new ArrayList<>());
				f = fichierRepository.save(f);
				return f;
			}
		}

		return null;
	}
}
