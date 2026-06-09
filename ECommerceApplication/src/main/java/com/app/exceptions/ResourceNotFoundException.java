package com.app.exceptions;

import lombok.Getter;
import java.io.Serial;

@Getter
public class ResourceNotFoundException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 1L;

	private final String resourceName;
	private final String field;
	private final String fieldValue;

	public ResourceNotFoundException(
			String resourceName,
			String field,
			String fieldValue
	) {
		super(String.format(
				"%s not found with %s: %s",
				resourceName,
				field,
				fieldValue
		));

		this.resourceName = resourceName;
		this.field = field;
		this.fieldValue = fieldValue;
	}

	public ResourceNotFoundException(
			String resourceName,
			String field,
			Long fieldValue
	) {
		this(
				resourceName,
				field,
				String.valueOf(fieldValue)
		);
	}

}