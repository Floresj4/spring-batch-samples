package com.flores.development.springbatch.config;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import com.flores.development.springbatch.model.Employee;

public class EmployeeFieldSetMapper implements FieldSetMapper<Employee>{

	@Override
	public Employee mapFieldSet(FieldSet fieldSet) throws BindException {
		return Employee.builder()
				.withId(fieldSet.readInt("id"))
				.withDeptId(fieldSet.readInt("dept_id"))
				.withTitle(fieldSet.readString("title"))
				.withName(fieldSet.readString("name"))
				.withBirthDate(fieldSet.readDate("birth_date", "YYYYmmdd"))
				.build();
	}
}
