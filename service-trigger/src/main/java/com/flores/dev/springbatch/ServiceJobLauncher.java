package com.flores.dev.springbatch;

import java.util.Properties;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public final class ServiceJobLauncher {

	@Autowired
	private JobLauncher jobLauncher;
	
	@Autowired
	private ApplicationContext context;
	
	@PostMapping("/run")
	public ExitStatus startBatchJob(@RequestBody JobLaunchRequest request) throws Exception {
		String name = request.getName();
		
		log.info("Attempting to launch {} job...", name);
		Job job = context.getBean(name, Job.class);
		JobExecution execution = jobLauncher.run(job, request.getJobParameters());
		
		ExitStatus status = execution.getExitStatus();
		return status;
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
