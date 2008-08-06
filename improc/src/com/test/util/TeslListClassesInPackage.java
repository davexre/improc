package com.test.util;

import java.io.IOException;

import com.slavi.util.ListClassesInPackage;

public class TeslListClassesInPackage {
	
	public static void main(String[] args) throws IOException {
//		String packageName = "org.jdom";
//		String packageName = "com.slavi";
//		String packageName = "com.slavi.ui";
		
//		List<String> r = getClassNamesInPackage(packageName, false);
//		for (String item : r) {
//			System.out.println(item);
//		}
		Class theClass = ListClassesInPackage.class;
		String s = ListClassesInPackage.getClassRootLocation(theClass);
		
		System.out.println(s);
		System.out.println(theClass.getName());
	}	
}
