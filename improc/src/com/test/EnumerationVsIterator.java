package com.test;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

public class EnumerationVsIterator {
	public static void main(String[] args) {
		Vector v = new Vector();
		Object element;
		Enumeration enum1;
		Iterator iter;
		long start;

		for (int i = 0; i < 1000000; i++) {
			v.add("New Element");
		}

		// *****CODE BLOCK FOR ITERATOR**********************
		start = System.currentTimeMillis();
		for (int i = 0; i < 100; i++) {
			iter = v.iterator();
			while (iter.hasNext()) {
				element = iter.next();
			}
		}
		System.out.println("Iterator took " + (System.currentTimeMillis() - start));
		// *************END OF ITERATOR BLOCK************************

//		System.gc(); // request to GC to free up some memory

		// *************CODE BLOCK FOR ENUMERATION*******************
		start = System.currentTimeMillis();
		for (int i = 0; i < 100; i++) {
			enum1 = v.elements();
			while (enum1.hasMoreElements()) {
				element = enum1.nextElement();
			}
		}
		System.out.println("Enumeration took " + (System.currentTimeMillis() - start));
		// ************END OF ENUMERATION BLOCK**********************
	}
}
