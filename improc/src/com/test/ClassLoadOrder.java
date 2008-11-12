package com.test;

public class ClassLoadOrder {

	public static class A {
		static {
			System.out.println("A: Class loaded");
		}
	}

	public static void q() {
		System.out.println("q: Entered");
		A a = new A();
		System.out.println("q: Created A=" + a.toString());
	}
	
	boolean asd = false;
	
	public void w() {
		System.out.println("w: Entered");
		if (asd) {
			System.out.println("w: Before creating A");
			A a = new A();
			System.out.println("w: After creating A=" + a.toString());
		}
	}
	
	static {
		System.out.println("ClassLoadOrder: Class loaded");
	}
	
	public static void main(String[] args) {
		System.out.println("main: Entered");
		ClassLoadOrder l = new ClassLoadOrder();
		System.out.println("main: ClassLoadOrder created");
		l.w();
		ClassLoadOrder.q();
		System.out.println("DONE.");		
	}
}
