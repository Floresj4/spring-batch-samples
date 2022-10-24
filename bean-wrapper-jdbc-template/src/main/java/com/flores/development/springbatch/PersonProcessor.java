package com.flores.development.springbatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import com.flores.development.springbatch.model.Person;

public class PersonProcessor implements ItemProcessor<Person, Person> {

	private static final Logger logger = LoggerFactory.getLogger(PersonProcessor.class);
	
	@Override
	public Person process(Person item) throws Exception {
		logger.debug("Processing person: " + item);
		
		//build a new/modified Person
		Person modified = new Person.PersonBuilder()
				.withFirstname(item.getFirstName().toUpperCase())
				.withLastname(item.getLastName().toUpperCase())
				.build();
		
		return modified;
	}
}
