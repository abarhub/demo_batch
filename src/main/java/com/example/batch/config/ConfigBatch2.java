package com.example.batch.config;

import com.example.batch.hello.job1.JobCompletionNotificationListener;
import com.example.batch.hello.job2.*;
import com.example.batch.hello.job2.service.FindRessources;
import com.example.batch.hello.job2.task.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.List;

/**
 * Created by Alain on 07/05/2017.
 */
@Configuration
@EnableBatchProcessing
public class ConfigBatch2 {

	private static final Logger LOG = LoggerFactory.getLogger(ConfigBatch2.class);

	@Value("${app.rep_comptes}")
	private String repertoireComptes;

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private FindRessources findRessources;

	@Bean
	public Job processCsv(JobCompletionNotificationListener listener) throws IOException {
		return jobBuilderFactory.get("processCsv")
				.incrementer(new RunIdIncrementer())
				//.listener(listener)
				.flow(taskDeleteAll())
				.next(taskInitFichier())
				.next(step01())
				.next(taskIgnore())
				.next(taskInitDebut())
				.next(taskCheckSoldes())
				.next(taskDb())
				.next(taskJest())
				.end()
				.build();
	}

	@Bean
	public Step step01() throws IOException {
		return stepBuilderFactory.get("step01")
				.<CsvItem, OperationComptableItem>chunk(10)
				//.reader(reader2())
				.reader(reader3())
				.processor(processor2())
				//.writer(writer2())
				.writer(writer3())
				.build();
	}

	@Bean
	public FlatFileItemReader<CsvItem> reader2() {
		FlatFileItemReader<CsvItem> reader = new FlatFileItemReader<CsvItem>();
		reader.setLinesToSkip(8);
		reader.setLineMapper(new DefaultLineMapper<CsvItem>() {{
			setLineTokenizer(new DelimitedLineTokenizer() {{
				setNames(new String[]{"date", "libelle", "montant", "montant2"});
				setDelimiter("\t");
			}});
			setFieldSetMapper(new BeanWrapperFieldSetMapper<CsvItem>() {{
				setTargetType(CsvItem.class);
			}});
		}});
		return reader;
	}

	@Bean
	public MultiResourceItemReader reader3() throws IOException {
		LOG.info("repertoireComptes=" + repertoireComptes);
		MultiResourceItemReader multiResourceItemReader = new MultiResourceItemReader();
		//ClassPathResource tab[] = {new ClassPathResource(repertoireComptes)};
		//multiResourceItemReader.setResources(tab);
		//ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(this.getClass().getClassLoader());
		//Resource[] resources = resolver.getResources(repertoireComptes + "/**/*.tsv");
		List<Resource> listeRessources = findRessources.getListRessources();
		//multiResourceItemReader.setResources(new PathMatchingResourcePatternResolver[]{new PathMatchingResourcePatternResolver()});
		multiResourceItemReader.setResources(listeRessources.toArray(new Resource[0]));
		multiResourceItemReader.setDelegate(reader2());
		return multiResourceItemReader;
	}

	@Bean
	public CsvProcessor processor2() {
		return new CsvProcessor();
	}


	@Bean
	public JdbcBatchItemWriter<OperationComptableItem> writer2() {
		JdbcBatchItemWriter<OperationComptableItem> writer = new JdbcBatchItemWriter<OperationComptableItem>();
		writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<OperationComptableItem>());
		writer.setSql("INSERT INTO operations (date,libelle,montant,montant2,nomFichier) VALUES " +
				"(:date,:libelle,:montant,:montantFrancs,:nomFichier)");
		writer.setDataSource(dataSource);
		return writer;
	}


	@Bean
	public WriterJpa writer3() {
		WriterJpa writerJpa = new WriterJpa();
		return writerJpa;
	}

	@Bean
	public Step taskDeleteAll() {
		return stepBuilderFactory.get("taskDeleteAll")
				.tasklet(deleteAllTask())
				.build();
	}

	@Bean
	public Step taskIgnore() {
		return stepBuilderFactory.get("taskIgnore")
				.tasklet(ignoreTask())
				.build();
	}

	@Bean
	public Step taskInitFichier() {
		return stepBuilderFactory.get("taskInitFichier")
				.tasklet(initFichierTask())
				.build();
	}

	@Bean
	public Step taskInitDebut() {
		return stepBuilderFactory.get("taskInitDebut")
				.tasklet(initDebutTask())
				.build();
	}

	@Bean
	public Step taskCheckSoldes() {
		return stepBuilderFactory.get("taskCheckSoldes")
				.tasklet(checkSoldeTask())
				.build();
	}

	@Bean
	public Step taskJest() {
		return stepBuilderFactory.get("taskJest")
				.tasklet(jestTask())
				.build();
	}

	@Bean
	public Step taskDb() {
		return stepBuilderFactory.get("taskDb")
				.tasklet(dbTask())
				.build();
	}

	@Bean
	public DeleteAllTask deleteAllTask() {
		return new DeleteAllTask();
	}


	@Bean
	public IgnoreTask ignoreTask() {
		return new IgnoreTask();
	}

	@Bean
	public InitFichierTask initFichierTask() {
		return new InitFichierTask();
	}

	@Bean
	public InitDebutTask initDebutTask() {
		return new InitDebutTask();
	}

	@Bean
	public CheckSoldeTask checkSoldeTask() {
		return new CheckSoldeTask();
	}

	@Bean
	public JestTask jestTask() {
		return new JestTask();
	}

	@Bean
	public DbTask dbTask() {
		return new DbTask();
	}
}
