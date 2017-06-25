package com.example.batch.hello.job2.task;

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

import java.util.List;

/**
 * Created by Alain on 07/05/2017.
 */
public class IgnoreTask implements Tasklet {

	private static final Logger LOG = LoggerFactory.getLogger(IgnoreTask.class);

	@Autowired
	private OperationRepository operationRepository;

	@Autowired
	private FichierRepository fichierRepository;

	@Override
	public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
		LOG.info("ignore ...");
		for (Fichier f : fichierRepository.findAll()) {
			LOG.info("traitement du fichier {} ...", f.getNomFichier());

			if (f.getListeOperations() != null && !f.getListeOperations().isEmpty()) {

				for (Operation o : f.getListeOperations()) {
					List<Operation> liste = operationRepository.findOperations(o.getDate(), o.getLibelle(), o.getMontant(), o.getId(),o.getFichier(),o.getId());
					if (liste != null && !liste.isEmpty()) {
						LOG.info("Presence multiple({})", liste.size());
						o.setIgnorer(true);
						operationRepository.save(o);
					}

				}
			}

			LOG.info("traitement du fichier {} ok", f.getNomFichier());
		}
		LOG.info("ignore ok");
		return RepeatStatus.FINISHED;
	}
}
