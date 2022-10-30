package com.flores.development.springbatch.config;

import org.springframework.batch.item.file.transform.DefaultFieldSetFactory;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.batch.item.file.transform.LineTokenizer;

import lombok.extern.slf4j.Slf4j;

/**
 * A custom line tokenizer.  The employee.csv file is a standard
 * comma-separated value file that can be easily handled by the delimited
 * builder of the FlatFileItemReader, but we want to explore this option
 * for future use.
 * @author jason
 */
@Slf4j
public class EmployeeLineTokenizer implements LineTokenizer {

	private static final String DELIMITER = ",";
	private static final String[] FIELD_NAMES = new String[] {"id", "dept_id", "title", "name"};

	@Override
	public FieldSet tokenize(String line) {
		log.debug("Tokenizing {}", line);
		
		//nothing happening here, straight forward mapping to fields
		String[] fields = line.split(DELIMITER);
		return new DefaultFieldSetFactory().create(fields, FIELD_NAMES);
	}

}
