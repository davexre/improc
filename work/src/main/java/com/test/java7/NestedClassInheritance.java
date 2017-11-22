package com.test.java7;

public class NestedClassInheritance {

	public static class BaseClass {
		public NestedBaseClass createNested () {
			return new NestedBaseClass();
		}
		
		public class NestedBaseClass {
			public NestedBaseClass() {
				System.out.println("NestedBaseClass");
			}
		}
	}
	
	public static class ChildClass extends BaseClass {
		public NestedChildClass createNested () {
			return new NestedChildClass();
		}
		
		public class NestedChildClass extends NestedBaseClass  {
			public NestedChildClass() {
				System.out.println("NestedChildClass");
			}
		}
	}

	public static void main(String[] args) {
		new ChildClass().createNested();
	}
}
