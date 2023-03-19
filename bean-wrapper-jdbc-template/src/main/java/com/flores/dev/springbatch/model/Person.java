package com.flores.dev.springbatch.model;

public class Person {

	private String firstName;
	private String lastName;
	
	public Person(String firstname, String lastname) {
		this.firstName = firstname;
		this.lastName = lastname;
	}

	public Person() {
		
	}
	
	public void setFirstName(String firstname) {
		this.firstName = firstname;
	}
	
	public void setLastName(String lastname) {
		this.lastName = lastname;
	}

	private Person(PersonBuilder builder) {
		this.firstName = builder.firstname;
		this.lastName = builder.lastname;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}
	
	public String toString() {
		return "First name: " + this.firstName
				+ " Last name: " + this.lastName;
	}
	
	public static class PersonBuilder {
		
		private String firstname;
		private String lastname;
	
		public PersonBuilder() {
			
		}
		
		public PersonBuilder withFirstname(String first) {
			this.firstname = first;
			return this;
		}
		
		public PersonBuilder withLastname(String last) {
			this.lastname = last;
			return this;
		}
		
		public Person build() {
			return new Person(this);
		}
	}
}
