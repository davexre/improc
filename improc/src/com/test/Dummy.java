package com.test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

public class Dummy implements Serializable {
	String prefix;
	private void popPrefix() {
		int last = prefix.lastIndexOf('.');
		if (last < 0) {
			prefix = "";
		} else {
			prefix = prefix.substring(0, last);
		}
	}
	
	void doIt() throws Exception {
		int v = 1355;
		System.out.println(new String(new int[] { v }, 0, 1));
	}
	
	public static void main(String[] args) throws Exception {
		new Dummy().doIt();
		System.out.println("Done.");
	}
}
