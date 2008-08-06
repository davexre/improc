package com.test.util;

import java.lang.reflect.Method;
import java.util.List;

import com.slavi.util.ListClassesInPackage;

public class ListClassesWithMain {
	public static void main(String[] args) {
		List<String> classNames = ListClassesInPackage.getClassNamesInPackage("com.slavi", true);
		Class[] mainArgs = new Class[] { args.getClass() };
		for (String className : classNames) {
			try {
				Class c = Class.forName(className);
				Method mainMethod = c.getDeclaredMethod("main", mainArgs);
				if (mainMethod != null) {
					System.out.println(className);
				}
			} catch (ClassNotFoundException e) {
				System.out.println("CLASS NOT FOUND: " + className);
			} catch (SecurityException e) {
			} catch (NoSuchMethodException e) {
			}
		}
	}
}
