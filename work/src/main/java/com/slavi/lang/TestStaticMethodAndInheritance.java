package com.slavi.lang;

import java.lang.reflect.Method;

public class TestStaticMethodAndInheritance {
	public static class Base {
		public static void myMethod() {
			System.out.println("myMethod");
		}
	}

	public static class Class1 extends Base {
	}

	public static void main(String[] args) throws Exception {
		Method m = Class1.class.getMethod("myMethod", null);
		m.invoke(null, null);
		System.out.println(m);
	}
}
