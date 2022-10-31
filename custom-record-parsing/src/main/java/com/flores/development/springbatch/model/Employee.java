package com.flores.development.springbatch.model;

import java.time.LocalDate;

import lombok.Getter;

/**
 * Employee model.  Only using lombok.getter annotation
 * here to save some time on the boilerplate.  Although lombok
 * provides builder annotations, there is value in seeing
 * and managing the builder implementation.
 * @author jason
 */
@Getter
public class Employee {
	
	private final int id;
	private final int deptId;
	private final int age;
	
	private final String name;
	private final String title;

	private final LocalDate birthDate;
	
	private Employee(EmployeeBuilder e) {
		id = e.id;
		deptId = e.deptId;
		age = e.age;
		name = e.name;
		title = e.title;
		birthDate = e.birthDate;
	}
	
	public static EmployeeBuilder builder() {
		return new EmployeeBuilder();
	}
	
	/**
	 * Builder class to support immutability
	 * @author jason
	 */
	public static class EmployeeBuilder {

		private int id;
		private int deptId;
		private int age;
		
		private String name;
		private String title;

		private LocalDate birthDate;
		
		public EmployeeBuilder withId(int id) {
			this.id = id;
			return this;
		}
		
		public EmployeeBuilder withDeptId(int deptId) {
			this.deptId = deptId;
			return this;
		}
		
		public EmployeeBuilder withAge(int age) {
			this.age = age;
			return this;
		}
		
		public EmployeeBuilder withName(String name) {
			this.name = name;
			return this;
		}
		
		public EmployeeBuilder withTitle(String title) {
			this.title = title;
			return this;
		}
		
		public EmployeeBuilder withBirthDate(LocalDate birthDate) {
			this.birthDate = birthDate;
			return this;
		}
		
		public Employee build() {
			return new Employee(this);
		}
	}
}
