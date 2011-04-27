package com.slavi.util.tree;

import java.util.concurrent.atomic.AtomicInteger;

public class KDTreeTestData {
	public static AtomicInteger idCounter = new AtomicInteger();
	
	public final int id;
	
	public final double value[];
	
	public KDTreeTestData(double value[]) {
		id = idCounter.getAndIncrement(); 
		this.value = new double[value.length];
		for (int i = value.length - 1; i >= 0; i--)
			this.value[i] = value[i];
	}
	
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append(id);
		buf.append(' ');
		String prefix = "";
		for (int i = 0; i < value.length; i++) {
			buf.append(prefix);
			buf.append(value[i]);
			prefix = ", ";
		}
		return buf.toString();
	}
}
