package com.flores.developmenet.springbatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import com.flores.developmenet.springbatch.model.Person;

public class PersonProcessor implements ItemProcessor<Person, Person> {

	private static final Logger logger = LoggerFactory.getLogger(PersonProcessor.class);
	
	@Override
	public Person process(Person item) throws Exception {
		logger.debug("Processing person: " + item);
		Person modified = new Person(
				item.getFirstName().toUpperCase(),
				item.getLastName().toUpperCase());
		
		return modified;
	}
	
}
