package com.example.batch.hello.job2.task;

import com.example.batch.hello.job2.entity.Solde;
import com.example.batch.hello.job2.jest.JestOperation;
import com.example.batch.hello.job2.entity.Operation2;
import com.example.batch.hello.job2.jest.JestSolde;
import com.example.batch.hello.job2.repository.Operation2Repository;
import com.example.batch.hello.job2.repository.SoldeRepository;
import com.google.common.collect.Lists;
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
public class Jest2Task implements Tasklet {

	private static final Logger LOG = LoggerFactory.getLogger(Jest2Task.class);

	private static final String index = "solde";
	private static final String mapping = "tsolde";

	@Autowired
	private SoldeRepository soldeRepository;

	@Override
	public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
		LOG.info("jest2 ...");

		LOG.info("connect ES");
		JestClient jestClient = createJest();

		LOG.info("init");
		init(jestClient);

		LOG.info("insert data");
		insert(jestClient);

		LOG.info("jest2 ok");
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

		res = client.execute(new DeleteIndex.Builder(index).build());
		//checkJest(res);

		res = client.execute(new CreateIndex.Builder(index).build());
		checkJest(res);

		PutMapping putMapping = new PutMapping.Builder(
				index,
				mapping,
				"{ \"" + mapping + "\" : { " +
						"\"properties\" : { " +
						"\"id\" : {\"type\" : \"string\"}, " +
						"\"date\" : {\"type\" : \"date\",\"format\": \"yyyy-MM-dd\"}, " +
						"\"montant\" : {\"type\" : \"double\"}, " +
						"\"solde\" : {\"type\" : \"double\"}, " +
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
		List<Solde> list = Lists.newArrayList(soldeRepository.findAll());
		//operationRepository.findAllOperationConsolide();
		if (list != null && !list.isEmpty()) {

			List<Index> liste = new ArrayList<>();
			for (Solde o : list) {

				JestSolde j = new JestSolde();
				j.setId(o.getId());
				//j.setDate(conv(o.getDate()).getTime());
				j.setDate(conv2(o.getDate()));
				j.setMontant(o.getMontant());
				j.setNoCompte(o.getNoCompte());
				j.setSolde(o.getSolde());

				Index index = new Index.Builder(j).index(this.index).type(mapping).build();
				liste.add(index);
			}

			Bulk bulk = new Bulk.Builder()
					.defaultIndex(index)
					.defaultType(mapping)
					.addAction(liste)
					.build();

			JestResult res = jestClient.execute(bulk);
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
