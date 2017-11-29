package com.test.java;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.springframework.context.annotation.Import;

@Import({})
public class CommonLangReflectionTest {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface MyAnnotation {
		String value() default "";
	}

	@MyAnnotation("MyInterface")
	public interface MyInterface {
		@MyAnnotation("MyInterface.getName")
		String getName();
	}
	
	public static class MyBaseClass implements MyInterface {
		@Override
		@MyAnnotation("MyInterface.getName")
		public String getName() {
			return "MyBaseClass";
		}
	}
	
	public static class MyClass extends MyBaseClass {
		public String getName(String param) {
			return "MyClass(String): Hello " + param;
		}

		public String getName(int param) {
			return "MyClass(int): Hello " + param;
		}

		public String getName(Integer param) {
			return "MyClass(Integer): Hello " + param;
		}
	}
	
	public static class MyClass2 extends MyClass {
		@Override
		public String getName() {
			return "MyClass2";
		}
	}

	void doIt() throws Exception {
		System.out.println(MethodUtils.getMethodsListWithAnnotation(MyInterface.class, MyAnnotation.class));
		System.out.println(MethodUtils.getMethodsListWithAnnotation(MyBaseClass.class, MyAnnotation.class));
		System.out.println(MethodUtils.getMethodsListWithAnnotation(MyClass.class, MyAnnotation.class));
		System.out.println(MethodUtils.getMethodsListWithAnnotation(MyClass2.class, MyAnnotation.class));
		MyClass c = new MyClass2();
		System.out.println(MethodUtils.invokeMethod(c, "getName"));
		System.out.println(MethodUtils.invokeMethod(c, "getName", "world"));
		System.out.println(MethodUtils.invokeMethod(c, "getName", 5));
		System.out.println(MethodUtils.invokeMethod(c, "getName", new Object[] {5}, new Class[] { int.class }));
	}

	public static void main(String[] args) throws Exception {
		new CommonLangReflectionTest().doIt();
		System.out.println("Done.");
	}
}
