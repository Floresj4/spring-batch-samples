package com.flores.development.springbatch;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@EnableBatchProcessing
@SpringBootApplication
public class App {
	
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
    
    @Bean
    @StepScope
    public FlatFileItemReader<?> reader() {
    	return new FlatFileItemReaderBuilder<>()
    			.name("reader")
    			.build();
    }
    
    @Bean
    @StepScope
    public ItemWriter<?> writer() {
    	//just write them to standard out for now
    	return (items) -> items.forEach(System.out::println);
    }
}