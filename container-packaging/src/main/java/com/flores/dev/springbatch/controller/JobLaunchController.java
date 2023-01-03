package com.flores.dev.springbatch.controller;

import java.util.Map.Entry;
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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class JobLaunchController {

	@Autowired
	private ApplicationContext context;

	@Autowired
	private JobExplorer jobExplorer;

	@Autowired
	private SimpleJobLauncher jobLauncher;

	@PostMapping("/run")
	public JobLaunchResponse launchJob(@RequestBody JobLaunchRequest request) throws Exception {
		
		String name = request.getName();
		log.info("Launching job {}", name);

		Job job = context.getBean(name, Job.class);		

		JobParametersBuilder jobParametersBuilder = new JobParametersBuilder(jobExplorer)
				.getNextJobParameters(job);
		
		//add user posted properties as strings
		for(Entry<Object, Object> e : request.getJobParameters().entrySet()) {
			jobParametersBuilder.addString(
					String.valueOf(e.getKey()),
					String.valueOf(e.getValue()));
		}

		JobParameters jobParameters = jobParametersBuilder.toJobParameters();		
		JobExecution execution = jobLauncher.run(job, jobParameters);

		log.info("Executed job {} with parameters {}", jobParameters);
		
		long id = execution.getId();
		ExitStatus status = execution.getExitStatus();

		return JobLaunchResponse.builder()
				.withStatus(status)
				.withId(id)
				.build();
	}

	@GetMapping("/status")
	public JobLaunchResponse jobStatus(@RequestParam long id) throws Exception {
		log.info("Retrieving execution status for job id {}", id);

		JobExecution execution = jobExplorer.getJobExecution(id);
		if(Optional.ofNullable(execution).isPresent()) {
			ExitStatus status = execution.getExitStatus();
			return JobLaunchResponse.builder()
					.withStatus(status)
					.withId(id)
					.build();
		}

		return new EmptyJobLaunchResponse(id);
	}

	@Data
	@ToString
	public static class JobLaunchRequest {

		private String name;
		
		private Properties jobParameters;
		
		public JobParametersBuilder toJobParametersBuilder() {
			return new JobParametersBuilder(jobParameters);
		}
	}

	@Getter
	@Builder(setterPrefix = "with")
	@AllArgsConstructor
	public static class JobLaunchResponse {

		private long id;
		
		private ExitStatus status;
	}
	
	/**
	 * Return unknown instead of exposing certain status codes
	 * 
	 * @author jason
	 */
	private static class EmptyJobLaunchResponse extends JobLaunchResponse {
		public EmptyJobLaunchResponse(long id) {
			super(id, ExitStatus.UNKNOWN);
		}
	}
}
