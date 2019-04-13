package com.slavi.dbutil;

public interface DbDataValueParser<T> {
	public T parse(String value) throws Exception;
}
