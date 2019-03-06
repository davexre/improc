package com.slavi.derbi.dbload;

public interface ValueParser<T> {
	public T parse(String value) throws Exception;
}
