package com.flores.dev.springbatch.reader;

import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import com.flores.dev.springbatch.model.Employee;
import com.flores.dev.springbatch.model.WorkItem;
import com.flores.dev.springbatch.model.Employee.EmployeeBuilder;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author jason
 */
@Slf4j
public class EmployeeFileReader implements ItemStreamReader<Employee> {

	/**
	 * The employee object being assembled over several lines;
	 * employee information and work items
	 */
	private Object currentItem;
	
	private final ItemStreamReader<Object> reader;

	public EmployeeFileReader(ItemStreamReader<Object> reader) {
		this.reader = reader;
	}

	@Override
	public Employee read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {

		if(currentItem == null) {
			currentItem = reader.read();
			
			//reader returned no more records
			if(currentItem == null) {
				return null;
			}
		}
		
		//assign an employee to build and reset state to look ahead
		EmployeeBuilder e = (EmployeeBuilder) currentItem;
		currentItem = null;

		List<WorkItem> workItems = new ArrayList<>();
		while(peek() instanceof WorkItem) {
			log.debug("Collecting {}...", currentItem);

			//the current item assigned in peek would be a workitem here
			WorkItem workItem = (WorkItem) currentItem;
			workItems.add(workItem);
			
			currentItem = null;
		}

		return e.withWorkItems(workItems)
				.build();
	}

	private Object peek() throws Exception {
		if(currentItem == null) {
			currentItem = reader.read();
		}
		
		return currentItem;
	}

	@Override
	public void update(ExecutionContext executionContext) throws ItemStreamException {
		reader.update(executionContext);
	}

	@Override
	public void close() throws ItemStreamException {
		reader.close();
	}

	@Override
	public void open(ExecutionContext executionContext) throws ItemStreamException {
		reader.open(executionContext);
	}
}
