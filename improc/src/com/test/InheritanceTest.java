package com.test;

public class InheritanceTest {

	public static class BaseClass {
		public BaseClass() {
			System.out.println("BaseClass()");
		}
		
		public BaseClass(String param1) {
			System.out.println("BaseClass(String), param1=" + param1);
		}
	}

	public static class Child1Class extends BaseClass {
		public Child1Class() {
			System.out.println("Child1()");
		}
	}
		
	public static void main(String[] args) {
		BaseClass c;
		c = new BaseClass();
		c = new BaseClass("asd");
		c = new Child1Class();
		// c = new Child1Class("asd"); // -> error
		if (c == null)
			System.out.println(c);
	}
}
