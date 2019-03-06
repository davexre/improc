package com.slavi.derbi.dbload;

public interface DbValueParse<T> {
	public T parse(String value) throws Exception;
}
