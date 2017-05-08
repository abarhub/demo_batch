package com.example.batch.hello;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alain on 07/05/2017.
 */
@Configuration
public class Startup {

	private static final Logger log = LoggerFactory.getLogger(Startup.class);

	@Autowired
	private Job importUserJob;

	@Autowired
	private Job processCsv;

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private SimpleJobOperator simpleJobOperator;

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {

			//System.out.println("Let's inspect the beans provided by Spring Boot:");
			log.info("Let's inspect the beans provided by Spring Boot:");

			log.info("debut");
			jobLauncher.run(importUserJob, new JobParameters());
			log.info("fin");

			log.info("***********************************");

			if (true) {
				Map<String, JobParameter> map = new HashMap<>();
				map.put("time", new JobParameter(System.currentTimeMillis()));
				log.info("debut2");
				JobExecution je = jobLauncher.run(processCsv, new JobParameters(map));
				log.info("fin2(id=" + je.getJobId() + ")");
			} else {
				log.info("debut3");
				long id = simpleJobOperator.startNextInstance("processCsv");
				log.info("fin3(id=" + id + ")");
			}

			/*String[] beanNames = ctx.getBeanDefinitionNames();
			Arrays.sort(beanNames);
			for (String beanName : beanNames) {
				System.out.println(beanName);
			}*/

		};
	}
}
