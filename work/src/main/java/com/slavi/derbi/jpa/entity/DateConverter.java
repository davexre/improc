package com.slavi.derbi.jpa.entity;

import java.util.Date;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class DateConverter implements AttributeConverter<Date, Long> {
	@Override
	public Long convertToDatabaseColumn(Date attribute) {
		return attribute == null ? null : attribute.getTime();
	}

	@Override
	public Date convertToEntityAttribute(Long dbData) {
		return dbData == null ? null : new Date(dbData);
	}
}
