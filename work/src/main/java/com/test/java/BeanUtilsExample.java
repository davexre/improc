package com.test.java;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;

public class BeanUtilsExample {

	public void doIt(String[] args) throws Exception {
		Map root = new HashMap();

		Map a = new HashMap();
		int arr[] = new int[] { 3, 5, 7 };
		a.put("b", arr);
		
		root.put("A", a);
		Object o = BeanUtils.getProperty(root, "(Z)(b).[1]");
		System.out.println(o);
		System.out.println(o.getClass());
	}

	public static void main(String[] args) throws Exception {
		new BeanUtilsExample().doIt(args);
		System.out.println("Done.");
	}
}
