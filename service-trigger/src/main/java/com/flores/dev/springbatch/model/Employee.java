package com.flores.dev.springbatch.model;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Employee {
	
	private String id;
	private String name;
	private String deptId;
	private String title;
	private String birthDate;
	
	private List<WorkItem> items = new ArrayList<>();
}
