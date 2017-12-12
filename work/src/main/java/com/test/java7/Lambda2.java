package com.test.java7;

import java.util.Arrays;
import java.util.function.Function;

public class Lambda2 {

	@FunctionalInterface
	public interface MyFunction {
		String apply2(String str);
	}
	
	public String doSomething (String str) {
		return str.trim();
	}
	
	public static String doSomething2(String str) {
		return str.trim();
	}
	
	public void asd(Class clazz) {
		System.out.println(clazz);
		System.out.println(Arrays.toString(clazz.getDeclaredMethods()));
		System.out.println();
	}
	
	void doIt() throws Exception {
		Function<String, String> add = e -> e+'.';
		Function<String, String> trim = this::doSomething;
		Function<String, String> trim2 = Lambda2::doSomething2;
		MyFunction trim3 = Lambda2::doSomething2;
		
		asd(add.getClass());
		asd(trim.getClass());
		asd(trim2.getClass());
		asd(trim3.getClass());
	}

	public static void main(String[] args) throws Exception {
		new Lambda2().doIt();
		System.out.println("Done.");
	}
}
