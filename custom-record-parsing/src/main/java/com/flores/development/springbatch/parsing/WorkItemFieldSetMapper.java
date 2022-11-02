package com.flores.development.springbatch.parsing;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import com.flores.development.springbatch.model.WorkItem;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WorkItemFieldSetMapper implements FieldSetMapper<Object> {

	@Override
	public Object mapFieldSet(FieldSet fieldSet) throws BindException {
		log.debug("Mapping fieldset to workitem");

		return WorkItem.builder()
				.withId(fieldSet.readInt("id"))
				.withTitle(fieldSet.readString("title"))
				.build();
	}

}
