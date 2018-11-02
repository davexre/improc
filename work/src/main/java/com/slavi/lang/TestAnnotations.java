package com.slavi.lang;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;

@TestAnnotations.MyAnnotation(v="asd")
public class TestAnnotations {

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.ANNOTATION_TYPE)
	public @interface MyBaseAnnotation {
		String value() default "";
	}

	@Retention(RetentionPolicy.RUNTIME)
	@MyBaseAnnotation("qwe")
	public @interface MyAnnotation {
		String v() default "";
	}


	public void doIt(String[] args) throws Exception {
		Class clazz = getClass();
		System.out.println(Arrays.toString(clazz.getAnnotationsByType(MyBaseAnnotation.class)));
//		for (Annotation a : clazz.getAnnotations())
//			System.out.println(a.toString());
	}

	public static void main(String[] args) throws Exception {
		new TestAnnotations().doIt(args);
		System.out.println("Done.");
	}
}
