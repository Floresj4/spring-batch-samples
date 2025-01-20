package com.flores.dev.springbatch.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@Builder(setterPrefix = "with")
public class WeightEntry {

	private String date;
	
	private double value;
}
