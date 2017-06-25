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
import java.util.*;

/**
 * Created by Alain on 07/05/2017.
 */
public class InitDebutTask implements Tasklet {

	private static final Logger LOG = LoggerFactory.getLogger(InitDebutTask.class);

	@Value("${app.rep_comptes}")
	private String repertoireComptes;

	@Autowired
	private OperationRepository operationRepository;

	@Autowired
	private FichierRepository fichierRepository;

	@Override
	public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
		LOG.info("init debut ...");

		Iterable<Fichier> iter = fichierRepository.findAll();

		List<Fichier> liste2 = new ArrayList<>();

		Set<String> setComptes = new TreeSet<>();

		for (Fichier f : iter) {
			liste2.add(f);
			if (f.getNoCompte() != null && !f.getNoCompte().trim().isEmpty()) {
				setComptes.add(f.getNoCompte());
			}
		}

		for (String noCompte : setComptes) {
			calculSoldeInit(noCompte);
		}

		LOG.info("init debut ok");
		return RepeatStatus.FINISHED;
	}

	private void calculSoldeInit(String noCompte) {
		List<Operation> listOperation = operationRepository.findTopByNoCompteOrderByDateAsc(noCompte);

		if (listOperation != null && !listOperation.isEmpty()) {
			Operation operation = listOperation.get(0);
			LOG.info("operation={}", operation);
			Fichier fichier = operation.getFichier();
			LOG.info("fichier={}", fichier);
			//fichier.getDate()

			Optional<Double> optTotal = operationRepository.totalOperations(fichier);
			double total=0.0;
			if(optTotal.isPresent()){
				total=optTotal.get();
			}
			LOG.info("total={}", total);
			double soldeInit = fichier.getSolde() - total;
			LOG.info("soldeInit={}", soldeInit);
			LocalDate date = operation.getDate().minusDays(1);
			LOG.info("date init={}", date);

			Fichier f = new Fichier();
			f.setNomFichier("soldeinit_"+noCompte);
			f.setSolde(soldeInit);
			f.setNoCompte(fichier.getNoCompte());
			f.setDate(date);
			f.setListeOperations(new ArrayList<>());
			f.setSoldeInitial(true);

			Operation operation2 = new Operation();
			operation2.setIgnorer(false);
			operation2.setFichier(f);
			operation2.setMontant(soldeInit);
			operation2.setLibelle("Solde Initial");
			operation2.setDate(date);
			f.getListeOperations().add(operation2);

			operationRepository.save(operation2);
			fichierRepository.save(f);
		}
	}
}
