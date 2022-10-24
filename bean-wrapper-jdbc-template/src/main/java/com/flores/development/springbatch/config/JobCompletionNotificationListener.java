package com.flores.development.springbatch.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.flores.development.springbatch.model.Person;

@Component
public class JobCompletionNotificationListener extends JobExecutionListenerSupport {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	private static final Logger logger = LoggerFactory.getLogger(JobCompletionNotificationListener.class
			.getSimpleName());
	
	@Override
	public void afterJob(JobExecution jobExecution) {
		if(jobExecution.getStatus() == BatchStatus.COMPLETED) {
			logger.info("Job completed!  Verifying results...");

			jdbcTemplate.query("select first_name, last_name from people", 
					(resultset, row) -> new Person.PersonBuilder()
						.withFirstname(resultset.getString(1))
						.withLastname(resultset.getString(2))
						.build())
					.forEach(p -> logger.info("Found Person: {} in the database", p));
		}
	}
}
