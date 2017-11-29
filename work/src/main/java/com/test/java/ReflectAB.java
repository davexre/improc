package com.test.java;

@SuppressWarnings("unused")
public class ReflectAB {
	public class A {

		public String str;

		public class B {
			private int i;
		}
	}

	public void testAccessToOuterClass() throws Exception {
		final A a = new A();
		final A.B b = a.new B();
		final Class[] parent = A.class.getDeclaredClasses();
		for (Class c : parent) {
			System.out.println(c);
		}
		System.out.println("com.test.A$B " + parent[0].getName());
		System.out.println("i " + parent[0].getDeclaredFields()[0].getName());
		System.out.println("int " + parent[0].getDeclaredFields()[0].getType().getName());
		//assertSame(a, a2);
	}

	public static void main(String[] args) throws Exception {
		new ReflectAB().testAccessToOuterClass();
	}
}
