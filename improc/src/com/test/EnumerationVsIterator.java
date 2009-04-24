package com.test;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import com.slavi.util.Marker;

public class EnumerationVsIterator {
	public static void main(String[] args) {
		Vector<String> v = new Vector<String>();
//		Object element;
		Enumeration<?> enum1;
		Iterator<?> iter;

		for (int i = 0; i < 1000000; i++) {
			v.add("New Element");
		}

		// *****CODE BLOCK FOR ITERATOR**********************
		Marker.mark("ITERATOR");
		for (int i = 0; i < 100; i++) {
			iter = v.iterator();
			while (iter.hasNext()) {
//				element = iter.next();
				iter.next();
			}
		}
		Marker.release();
		// *************END OF ITERATOR BLOCK************************

//		System.gc(); // request to GC to free up some memory

		// *************CODE BLOCK FOR ENUMERATION*******************
		Marker.mark("ENUMERATION");
		for (int i = 0; i < 100; i++) {
			enum1 = v.elements();
			while (enum1.hasMoreElements()) {
//				element = enum1.nextElement();
				enum1.nextElement();
			}
		}
		Marker.release();
		// ************END OF ENUMERATION BLOCK**********************
	}
}
