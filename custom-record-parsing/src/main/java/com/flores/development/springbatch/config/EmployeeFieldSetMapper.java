package com.flores.development.springbatch.config;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import com.flores.development.springbatch.model.Employee;

public class EmployeeFieldSetMapper implements FieldSetMapper<Employee>{

	private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
	
	@Override
	public Employee mapFieldSet(FieldSet fieldSet) throws BindException {

		//use local date for less work in processor later
		String birthDateStr = fieldSet.readString("birth_date");
		LocalDate birthDate = LocalDate.parse(birthDateStr, dateTimeFormatter);
		
		return Employee.builder()
				.withId(fieldSet.readInt("id"))
				.withDeptId(fieldSet.readInt("dept_id"))
				.withTitle(fieldSet.readString("title"))
				.withName(fieldSet.readString("name"))
				.withBirthDate(birthDate)
				.build();
	}
}
