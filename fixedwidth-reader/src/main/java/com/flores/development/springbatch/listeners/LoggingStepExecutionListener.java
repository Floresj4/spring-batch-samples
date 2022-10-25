package com.flores.development.springbatch.listeners;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoggingStepExecutionListener {

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
    	log.debug("Before step...");
    }
    
    @AfterStep
    public ExitStatus afterStep(StepExecution stepExecution) {
    	log.debug("After step...");
    	return stepExecution.getExitStatus();
    }
}
