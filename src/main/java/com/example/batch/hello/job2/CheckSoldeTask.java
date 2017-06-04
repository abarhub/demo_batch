package com.example.batch.hello.job2;

import com.example.batch.hello.job2.entity.Fichier;
import com.example.batch.hello.job2.entity.Operation;
import com.example.batch.hello.job2.repository.FichierRepository;
import com.example.batch.hello.job2.repository.OperationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;

/**
 * Created by Alain on 07/05/2017.
 */
public class CheckSoldeTask implements Tasklet {

	private static final Logger LOG = LoggerFactory.getLogger(CheckSoldeTask.class);

	@Value("${app.rep_comptes}")
	private String repertoireComptes;

	@Autowired
	private OperationRepository operationRepository;

	@Autowired
	private FichierRepository fichierRepository;

	@Override
	public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
		LOG.info("checkSolde ...");

		Iterable<Fichier> iter = fichierRepository.findAll();

		iter.forEach(f -> verifieFichier(f));

		LOG.info("checkSolde ok");
		return RepeatStatus.FINISHED;
	}

	private void verifieFichier(Fichier f) {
		if (f.getNomFichier().equals("soldeinit")) {
			LOG.info("Fichier du solde initial ignore : " + f.getNomFichier());
		} else {
			LOG.info("Traitement fichier {}", f.getNomFichier());
			double soldeFinal = f.getSolde();

			double total = operationRepository.totalOperations(f);

			double total2 = 0.0;
			LocalDate d = null;
			for (Operation o : f.getListeOperations()) {
				total2 += o.getMontant();
				if (d == null) {
					d = o.getDate();
				} else if (d.isAfter(o.getDate())) {
					d = o.getDate();
				}
			}

			if (egal(total, total2)) {
				LOG.info("le montant total est correcte : {}", total);
			} else {
				LOG.error("le montant total est faux : {}!={}", total, total2);
				throw new IllegalStateException("le montant total est faux:" + total + "!=" + total2);
			}

			if (d == null) {
				throw new IllegalStateException("Impossible de trouver la date de debut");
			} else {
				LOG.info("1ere operation : {}", d);
			}

			double totalAvantFichier = operationRepository.totalOperationConsolide(d);

			LOG.info("totalAvantFichier={}", totalAvantFichier);
			LOG.info("soldeFinal={}", soldeFinal);
			LOG.info("totalAvantFichier+total={}", totalAvantFichier + total);

			if (egal(soldeFinal, totalAvantFichier + total)) {
				LOG.info("Solde OK");
			} else {
				LOG.error("Erreur pour le calcul du solde : {} != {}", soldeFinal, totalAvantFichier + total);
				throw new IllegalStateException("Erreur pour le calcul du solde : " + soldeFinal + " != " + (totalAvantFichier + total));
			}

		}
	}

	private boolean egal(double m1, double m2) {
		return Math.abs(m1 - m2) < 0.001;
	}

}
