package com.flores.development.springbatch.listeners;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.annotation.AfterJob;
import org.springframework.batch.core.annotation.BeforeJob;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoggingJobExecutionListener {

	@BeforeJob
	public void beforeJob(JobExecution jobExecution) {
		log.debug("Before job...");
	}
	
	@AfterJob
	public void afterJob(JobExecution jobExecution) {
		log.debug("After job...");
	}
}
