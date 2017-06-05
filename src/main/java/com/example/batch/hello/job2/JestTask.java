package com.example.batch.hello.job2;

import com.example.batch.hello.job2.entity.Operation;
import com.example.batch.hello.job2.repository.FichierRepository;
import com.example.batch.hello.job2.repository.OperationRepository;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Index;
import io.searchbox.indices.CreateIndex;
import io.searchbox.indices.DeleteIndex;
import io.searchbox.indices.mapping.PutMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * Created by Alain on 07/05/2017.
 */
public class JestTask implements Tasklet {

	private static final Logger LOG = LoggerFactory.getLogger(JestTask.class);

	private static final String index = "operation";
	private static final String mapping = "toperation";

	@Value("${app.rep_comptes}")
	private String repertoireComptes;

	@Autowired
	private OperationRepository operationRepository;

	@Autowired
	private FichierRepository fichierRepository;

	@Override
	public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
		LOG.info("jest ...");

		LOG.info("connect ES");
		JestClient jestClient = createJest();

		LOG.info("init");
		init(jestClient);

		LOG.info("insert data");
		insert(jestClient);


//		Optional<Operation> optOperation = operationRepository.findTopByOrderByDateAsc();
//
//		if (optOperation.isPresent()) {
//			Operation operation = optOperation.get();
//			LOG.info("operation={}", operation);
//			Fichier fichier = operation.getFichier();
//			LOG.info("fichier={}", fichier);
//			//fichier.getDate()
//
//			double total = operationRepository.totalOperations(fichier);
//			LOG.info("total={}", total);
//			double soldeInit = fichier.getSolde() - total;
//			LOG.info("soldeInit={}", soldeInit);
//			LocalDate date = operation.getDate().minusDays(1);
//			LOG.info("date init={}", date);
//
//			Fichier f = new Fichier();
//			f.setNomFichier("soldeinit");
//			f.setSolde(soldeInit);
//			f.setNoCompte(fichier.getNoCompte());
//			f.setDate(date);
//			f.setListeOperations(new ArrayList<>());
//
//			Operation operation2 = new Operation();
//			operation2.setIgnorer(false);
//			operation2.setFichier(f);
//			operation2.setMontant(soldeInit);
//			operation2.setLibelle("Solde Initial");
//			operation2.setDate(date);
//			f.getListeOperations().add(operation2);
//
//			operationRepository.save(operation2);
//			fichierRepository.save(f);
//		}

		LOG.info("jest ok");
		return RepeatStatus.FINISHED;
	}

	private JestClient createJest() {
		JestClientFactory factory = new JestClientFactory();
		factory.setHttpClientConfig(new HttpClientConfig
				.Builder("http://localhost:9200")
				.multiThreaded(true)
				//Per default this implementation will create no more than 2 concurrent connections per given route
				//.defaultMaxTotalConnectionPerRoute(<YOUR_DESIRED_LEVEL_OF_CONCURRENCY_PER_ROUTE>)
				// and no more 20 connections in total
				//.maxTotalConnection(<YOUR_DESIRED_LEVEL_OF_CONCURRENCY_TOTAL>)
				.build());
		JestClient client = factory.getObject();
		return client;
	}

	private void init(JestClient client) throws IOException {

		client.execute(new DeleteIndex.Builder(index).build());

		client.execute(new CreateIndex.Builder(index).build());

		PutMapping putMapping = new PutMapping.Builder(
				index,
				mapping,
				"{ \"properties\" : { \"id\" : {\"type\" : \"string\"},\"date\" : {\"type\" : \"date\"},\"libelle\" : {\"type\" : \"string\"},\"montant\" : {\"type\" : \"double\"},} }"
		).build();
		client.execute(putMapping);
	}

	private void insert(JestClient jestClient) throws IOException {
		List<Operation> list = operationRepository.findAllOperationConsolide();
		if (list != null && !list.isEmpty()) {

			for (Operation o : list) {

				JestOperation j = new JestOperation();
				j.setId(o.getId());
				j.setDate(conv(o.getDate()).getTime());
				j.setLibelle(o.getLibelle());
				j.setMontant(o.getMontant());

				Index index = new Index.Builder(j).index(this.index).type(mapping).build();
				jestClient.execute(index);
			}
		}
	}

	private Date conv(LocalDate localDate) {
		Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
		return date;
	}
}
