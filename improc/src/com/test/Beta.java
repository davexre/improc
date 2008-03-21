package com.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Beta {
	public static class One {
		One foo() {
			System.out.println("One");
			return null;
		}
	}

	public static Iterator reverse(List list) {
		Collections.reverse(list);
		return list.iterator();
	}

	public static void main(String[] args) {
		assert (false) : "qqqqq";
		System.out.println("asdads");
		List list = new ArrayList();
		list.add("1"); list.add("2"); list.add("3");
//		for (Object obj: reverse(list))
//			System.out.print(obj + ",");
	}

	public static class Two extends One {
		protected Two foo() {
			System.out.println("Two");
			return null;
		}
	}
}