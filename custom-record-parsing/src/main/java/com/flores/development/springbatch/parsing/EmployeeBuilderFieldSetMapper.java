package com.flores.development.springbatch.parsing;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import com.flores.development.springbatch.model.Employee;
import com.flores.development.springbatch.model.Employee.EmployeeBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EmployeeBuilderFieldSetMapper implements FieldSetMapper<Object> {

	private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
	
	@Override
	public EmployeeBuilder mapFieldSet(FieldSet fieldSet) throws BindException {
		log.debug("Mapping fieldset to employee.builder");
		
		//use local date for less work in processor later
		String birthDateStr = fieldSet.readString("birth_date");
		LocalDate birthDate = LocalDate.parse(birthDateStr, dateTimeFormatter);
		
		//return a partially filled builder to append work items to
		return Employee.builder()
				.withId(fieldSet.readInt("id"))
				.withDeptId(fieldSet.readInt("dept_id"))
				.withTitle(fieldSet.readString("title"))
				.withName(fieldSet.readString("name"))
				.withBirthDate(birthDate);
	}

}
