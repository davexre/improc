package com.test;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Dummy implements Serializable {

	protected List<String> stringList = new ArrayList<String>();

	public List<String> getStringList(List<String> qqq) {
		return this.stringList;
	}

	void doIt() throws Exception {
		Method method = Dummy.class.getMethod("getStringList", List.class);

		Type returnType = method.getGenericReturnType();

		if (returnType instanceof ParameterizedType) {
			ParameterizedType type = (ParameterizedType) returnType;
			Type[] typeArguments = type.getActualTypeArguments();
			for (Type typeArgument : typeArguments) {
				Class typeArgClass = (Class) typeArgument;
				System.out.println("typeArgClass = " + typeArgClass);
			}
		}
	}

	public static void main(String[] args) throws Exception {
		new Dummy().doIt();
		System.out.println("Done.");
	}
}
