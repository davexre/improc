package com.slavi.derbi.jpa.entity;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class NullStringConverter implements AttributeConverter<String, String> {
	@Override
	public String convertToDatabaseColumn(String attribute) {
		System.out.println("\n\nconvertToDatabaseColumn " + attribute + "\n\n");
		return attribute == null ? "" : attribute;
	}

	@Override
	public String convertToEntityAttribute(String dbData) {
		System.out.println("\n\nconvertToEntityAttribute " + dbData + "\n\n");
		return dbData == null ? "" : dbData;
	}
}
