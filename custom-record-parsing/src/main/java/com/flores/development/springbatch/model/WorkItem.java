package com.flores.development.springbatch.model;

import lombok.Getter;
import lombok.ToString;

/**
 * Simple work item consisting of an id
 * and title.  Lombok.getter is used to generate
 * the accessor methods for brevity.
 * @author jason
 */
@Getter
@ToString
public class WorkItem {

	private final int id;

	private final String title;
	
	private WorkItem(WorkItemBuilder builder) {
		this.id = builder.id;
		this.title = builder.title;
	}
	
	public static WorkItemBuilder builder() {
		return new WorkItemBuilder();
	}
	
	public static class WorkItemBuilder {

		private int id;
		
		private String title;

		private WorkItemBuilder() {
			
		}
		
		public WorkItemBuilder withId(int id) {
			this.id = id;
			return this;
		}
		
		public WorkItemBuilder withTitle(String title) {
			this.title = title;
			return this;
		}
		
		public WorkItem build() {
			return new WorkItem(this);
		}
	}
}
