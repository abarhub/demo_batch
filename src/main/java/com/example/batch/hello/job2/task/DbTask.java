package com.example.batch.hello.job2.task;

import com.example.batch.hello.job2.entity.Operation;
import com.example.batch.hello.job2.entity.Operation2;
import com.example.batch.hello.job2.entity.Solde;
import com.example.batch.hello.job2.repository.FichierRepository;
import com.example.batch.hello.job2.repository.Operation2Repository;
import com.example.batch.hello.job2.repository.OperationRepository;
import com.example.batch.hello.job2.repository.SoldeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.*;

/**
 * Created by Alain on 07/05/2017.
 */
public class DbTask implements Tasklet {

	private static final Logger LOG = LoggerFactory.getLogger(DbTask.class);

	private static final String index = "operation";
	private static final String mapping = "toperation";

	@Value("${app.rep_comptes}")
	private String repertoireComptes;

	@Autowired
	private OperationRepository operationRepository;

	@Autowired
	private FichierRepository fichierRepository;

	@Autowired
	private Operation2Repository operation2Repository;

	@Autowired
	private SoldeRepository soldeRepository;

	@Override
	public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
		LOG.info("db ...");

		traitement();

		LOG.info("db ok");
		return RepeatStatus.FINISHED;
	}

	@Transactional
	public void traitement() {

		LOG.info("delete all");
		operation2Repository.deleteAll();
		soldeRepository.deleteAll();

		LOG.info("find data");
		List<Operation2> liste = new ArrayList<>();
		Map<LocalDate, Solde> listeSolde = new TreeMap<>();
		List<Operation> list = operationRepository.findAllOperationConsolide();
		if (list != null && !list.isEmpty()) {

			for (Operation o : list) {
				LocalDate date = o.getDate();
				Operation2 o2 = new Operation2();
				o2.setDate(date);
				o2.setLibelle(o.getLibelle());
				o2.setMontant(o.getMontant());
				o2.setMontantFrancs(o.getMontantFrancs());
				o2.setNoCompte(o.getFichier().getNoCompte());
				liste.add(o2);

				ajouteMontant(listeSolde, o);
			}
		}

		LOG.info("insert data ...");
		operation2Repository.save(liste);
		LOG.info("insert data ok");

		LOG.info("insert solde ...");
		List<Solde> liste2 = new ArrayList<>();
		Map<String, Double> solde = new HashMap<>();
		for (Map.Entry<LocalDate, Solde> tmp : listeSolde.entrySet()) {
			Solde s = tmp.getValue();
			String numcpt = s.getNoCompte();
			double s2 = 0.0;
			if (solde.containsKey(numcpt)) {
				s2 = solde.get(numcpt);
			}
			s2 += s.getMontant();
			s.setSolde(s2);
			solde.put(numcpt, s2);
			liste2.add(s);
		}
		soldeRepository.save(liste2);
		LOG.info("insert solde ok");
	}

	private void ajouteMontant(Map<LocalDate, Solde> listeSolde, Operation o) {
		LocalDate date = o.getDate();
		if (listeSolde.containsKey(date)) {
			Solde s = listeSolde.get(date);
			s.setMontant(s.getMontant() + o.getMontant());
			s.setMontantFrancs(s.getMontantFrancs() + o.getMontantFrancs());
		} else {
			Solde s = new Solde();
			s.setDate(date);
			s.setMontant(o.getMontant());
			s.setMontantFrancs(o.getMontantFrancs());
			s.setNoCompte(o.getFichier().getNoCompte());
			listeSolde.put(date, s);
		}
	}

}
