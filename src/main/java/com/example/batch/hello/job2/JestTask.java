package com.example.batch.hello.job2;

import com.example.batch.hello.job2.entity.JestOperation;
import com.example.batch.hello.job2.entity.Operation;
import com.example.batch.hello.job2.repository.FichierRepository;
import com.example.batch.hello.job2.repository.OperationRepository;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Bulk;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
		JestResult res;

		res=client.execute(new DeleteIndex.Builder(index).build());
		checkJest(res);

		res=client.execute(new CreateIndex.Builder(index).build());
		checkJest(res);

		PutMapping putMapping = new PutMapping.Builder(
				index,
				mapping,
				"{ \"" + mapping + "\" : { " +
						"\"properties\" : { " +
						"\"id\" : {\"type\" : \"string\"}, " +
						"\"date\" : {\"type\" : \"date\",\"format\": \"yyyy-MM-dd\"}, " +
						"\"libelle\" : {\"type\" : \"string\"}, " +
						"\"montant\" : {\"type\" : \"double\"}, " +
						"\"noCompte\" : {\"type\" : \"string\"} " +
						"} } }"
		).build();
		res = client.execute(putMapping);
		checkJest(res);
	}

	private void checkJest(JestResult res) {
		if (res != null) {
			if (!res.isSucceeded()) {
				LOG.error("Error Jest (" + res.getResponseCode() + ") : " + res.getErrorMessage());
				throw new IllegalStateException("Error Jest : " + res.getErrorMessage());
			}
		}
	}

	private void insert(JestClient jestClient) throws IOException {
		List<Operation> list = operationRepository.findAllOperationConsolide();
		if (list != null && !list.isEmpty()) {

			List<Index> liste=new ArrayList<>();
			for (Operation o : list) {

				JestOperation j = new JestOperation();
				j.setId(o.getId());
				//j.setDate(conv(o.getDate()).getTime());
				j.setDate(conv2(o.getDate()));
				j.setLibelle(o.getLibelle());
				j.setMontant(o.getMontant());
				j.setNoCompte(o.getFichier().getNoCompte());

				Index index = new Index.Builder(j).index(this.index).type(mapping).build();
				liste.add(index);
			}

			Bulk bulk = new Bulk.Builder()
					.defaultIndex(index)
					.defaultType(mapping)
					.addAction(liste)
					.build();

			JestResult res=jestClient.execute(bulk);
			checkJest(res);
		}
	}

	private Date conv(LocalDate localDate) {
		Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
		return date;
	}

	private String conv2(LocalDate localDate) {
		return localDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
	}
}
