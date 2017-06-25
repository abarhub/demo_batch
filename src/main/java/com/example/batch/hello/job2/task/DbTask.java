package com.example.batch.hello.job2.task;

import com.example.batch.hello.job2.entity.Operation;
import com.example.batch.hello.job2.entity.Operation2;
import com.example.batch.hello.job2.repository.FichierRepository;
import com.example.batch.hello.job2.repository.Operation2Repository;
import com.example.batch.hello.job2.repository.OperationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

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

		LOG.info("find data");
		List<Operation2> liste = new ArrayList<>();
		List<Operation> list = operationRepository.findAllOperationConsolide();
		if (list != null && !list.isEmpty()) {

			for (Operation o : list) {
				Operation2 o2 = new Operation2();
				o2.setDate(o.getDate());
				o2.setLibelle(o.getLibelle());
				o2.setMontant(o.getMontant());
				o2.setMontantFrancs(o.getMontantFrancs());
				o2.setNoCompte(o.getFichier().getNoCompte());
				liste.add(o2);
			}
		}

		LOG.info("insert data ...");
		operation2Repository.save(liste);
		LOG.info("insert data ok");
	}

}
