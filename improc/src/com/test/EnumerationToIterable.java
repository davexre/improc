package com.test;

import java.io.File;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.StringTokenizer;


public class EnumerationToIterable<E> implements Iterable<E> {
	
	public static class EnumerationToIterator<E> implements Iterator<E> {
	
		private Enumeration<E> e;
	
		public EnumerationToIterator(Enumeration<E> e) {
			this.e = e;
		}
		
		public boolean hasNext() {
			return e.hasMoreElements();
		}
	
		public E next() {
			return e.nextElement();
		}
	
		public void remove() {
			throw new Error("Not applicable");
		}
	}	

	private Enumeration<E> e;
	
	public EnumerationToIterable(Enumeration<E> e) {
		this.e = e;
	}
	
	public Iterator<E> iterator() {
		Iterator<E> i = new EnumerationToIterator<E>(e);
		e = null;
		return i;
	}
	
	public static void main(String[] args) {
		String path = System.getProperty("java.class.path");
		StringTokenizer st = new StringTokenizer(path, File.pathSeparator);
		for (Object s : new EnumerationToIterable(st)) {
			System.out.println(s);
		}
	}

}
