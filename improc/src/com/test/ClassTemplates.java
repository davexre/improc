package com.test;

import java.util.ArrayList;
import java.util.Iterator;

public class ClassTemplates {

	public static class BaseDataClass {
	}

	public static class SomeDataClass extends BaseDataClass {
	}
	
	public static void someMethod(Iterator<? extends BaseDataClass> a) {
		BaseDataClass b = a.next();
		System.out.println(b);
	}

	public static void main(String[] args) {
		ArrayList<SomeDataClass>aa = new ArrayList<SomeDataClass>();
		someMethod(aa.iterator());
	}
}
