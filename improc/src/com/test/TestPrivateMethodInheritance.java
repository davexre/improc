package com.test;

@SuppressWarnings("all")
public class TestPrivateMethodInheritance {

	public static class Base {
		public void exec() {
			privateMethod();
			protectedMethod();
		}
		
		private void privateMethod() {
			System.out.println("Base:privateMethod");
		}
		
		protected void protectedMethod() {
			System.out.println("Base:protectedMethod");
		}
	}
	
	public static class Child extends Base {
		public void privateMethod() {
			System.out.println("Child:privateMethod");
		}

		protected void protectedMethod() {
			System.out.println("Child:protectedMethod");
		}
	}

	public static void main(String[] args) {
		Base b = new Child();
		b.exec();
	}
}
