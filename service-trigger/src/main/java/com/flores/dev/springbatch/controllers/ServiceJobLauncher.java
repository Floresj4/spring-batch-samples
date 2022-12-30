package com.flores.dev.springbatch.controllers;

import java.util.Properties;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
	
	@PostMapping("/run")
	public JobLaunchResponse startBatchJob(@RequestBody JobLaunchRequest request) throws Exception {
		String name = request.getName();
		Job job = context.getBean(name, Job.class);
		log.info("Attempting to launch {} job...", name);

		//set an async executor to allow response to continue
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
}
