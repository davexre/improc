package com.slavi.lang;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.List;

public class TestReflectionGenerics {

	public List<String> lst;
	public String str;


	public static void main(String[] args) throws Exception {
		Class c = TestReflectionGenerics.class;
		Field f = c.getField("lst");
		ParameterizedType pt = (ParameterizedType) f.getGenericType();
		System.out.println(pt.getActualTypeArguments()[0]);

		f = c.getField("str");
		System.out.println(f.getGenericType());
		System.out.println((Class) f.getType());
	}
}
