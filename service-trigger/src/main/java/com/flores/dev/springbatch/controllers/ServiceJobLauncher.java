package com.flores.dev.springbatch.controllers;

import java.util.Optional;
import java.util.Properties;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public final class ServiceJobLauncher {

	@Autowired
	private SimpleJobLauncher jobLauncher;
	
	@Autowired
	private ApplicationContext context;
	
	@Autowired
	private JobExplorer jobExplorer;
	
	@PostMapping("/run")
	public JobLaunchResponse startBatchJob(@RequestBody JobLaunchRequest request) throws Exception {
		String name = request.getName();
		
		log.info("Attempting to launch {} job...", name);

		Job job = context.getBean(name, Job.class);
		JobExecution execution = jobLauncher.run(job, request.getJobParameters());
		
		long id = execution.getId();		
		ExitStatus status = execution.getExitStatus();

		JobLaunchResponse response = JobLaunchResponse.builder()
				.withStatus(status)
				.withId(id)
				.build();
		
		log.debug("Job execution response: " + response);
		return response;
	}

	@GetMapping("/status")
	public JobLaunchResponse getBatchJobstatus(@RequestParam(name = "id") long executionId) throws Exception {
		log.info("Retrieving batch job status for execution id {}", executionId);
		
		JobExecution execution = jobExplorer.getJobExecution(executionId);
		if(Optional.ofNullable(execution).isPresent()) {
			long id = execution.getId();
			ExitStatus status = execution.getExitStatus();
	
			return JobLaunchResponse.builder()
					.withStatus(status)
					.withId(id)
					.build();
		}
		
		log.info("JobExplorer returned no response on execution id {}", executionId);
		return new EmptyJobExecutionStatus();
	}
	
	@Data
	@ToString
	@Builder(setterPrefix = "with")
	public static class JobLaunchResponse {
		
		private long id;
		
		private ExitStatus status;
	}
	
	/**
	 * Payload required to launch a job via http.
	 * @author Jason
	 */
	@Data
	public static class JobLaunchRequest {

		private String name;
		
		private Properties jobParameters;
		
		public JobParameters getJobParameters() {
			return new JobParametersBuilder(jobParameters)
					.toJobParameters();
		}
	}

	/**
	 * Unknown response usedwhen the explorer returns a
	 * null response
	 * 
	 * @author jason
	 */
	public static class EmptyJobExecutionStatus extends JobLaunchResponse {
		public EmptyJobExecutionStatus() {
			super(0, ExitStatus.UNKNOWN);
		}
	}
}
