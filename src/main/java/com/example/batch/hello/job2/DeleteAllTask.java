package com.example.batch.hello.job2;

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
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

/**
 * Created by Alain on 07/05/2017.
 */
@Component
public class DeleteAllTask implements Tasklet {

	private static final Logger LOG = LoggerFactory.getLogger(DeleteAllTask.class);

	@Autowired
	private OperationRepository operationRepository;

	@Autowired
	private FichierRepository fichierRepository;

	@Autowired
	private Operation2Repository operation2Repository;

	@Override
	@Transactional
	public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
		LOG.info("delete all ...");
		operation2Repository.deleteAll();
		operationRepository.deleteAll();
		fichierRepository.deleteAll();
		LOG.info("delete all ok");
		return RepeatStatus.FINISHED;
	}
}
