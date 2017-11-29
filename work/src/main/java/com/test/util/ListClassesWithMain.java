package com.test.util;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

import com.slavi.util.ListClassesInPackage;

public class ListClassesWithMain {
	public static void main(String[] args) {
		List<String> classNames = ListClassesInPackage.getClassNamesInPackage("com.slavi", true);
		Class<?>[] mainArgs = new Class[] { args.getClass() };
		for (String className : classNames) {
			try {
				Class<?> c = Class.forName(className);
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
	
	public static void main1(String[] args) {
		Reflections reflections = new Reflections("com.slavi", new ResourcesScanner());
		Set<String> r = reflections.getResources(Pattern.compile(".*\\.txt", Pattern.CASE_INSENSITIVE));
		for (String i : r)
			System.out.println(i);
/*
		File dir = new File("D:/Users/S/LJS/Wink/");
		File files[] = dir.listFiles();
		for (File f : files) {
			if (f.isFile() && f.getName().endsWith(".jar")) {
				System.out.println("===== " + f.getName() + " ============");
				List<String> res = ListClassesInPackage.getSubPackageNames(f.getAbsolutePath(), "");
				for (String i : res) {
					System.out.println(i);
				}
			}
		}*/
	}
}
