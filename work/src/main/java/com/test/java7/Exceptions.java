package com.test.java7;

public class Exceptions {

	void doIt() throws Exception {
		Exception e = new Exception("Exception");
		Exception s1 = new Exception("Suppressed 1");
		Exception s2 = new Exception("Suppressed 2");
		Exception ss1 = new Exception("Suppressed 1/1");
		s1.addSuppressed(ss1);
		e.addSuppressed(s1);
		e.addSuppressed(s2);
		throw e;
	}

	public static void main(String[] args) throws Exception {
		new Exceptions().doIt();
		System.out.println("Done.");
	}
}
