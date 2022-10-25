package com.flores.development.springbatch.listeners;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterChunk;
import org.springframework.batch.core.annotation.AfterChunkError;
import org.springframework.batch.core.annotation.BeforeChunk;
import org.springframework.batch.core.scope.context.ChunkContext;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoggingChunkListener {

	@BeforeChunk
	public void beforeChunk(ChunkContext stepExecution) {
		log.debug("Before chunk");
	}

	@AfterChunk
	public void afterChunk(ChunkContext stepExecution) {
		log.debug("After chunk...");
	}

	@AfterChunkError
	public void afterChunkError(ChunkContext context) {
		
	}
}
