package com.test.java7;

import java.io.Closeable;
import java.io.IOException;

public class TryWithResources {

	public static class MyResource implements Closeable {

		final String name;
		final boolean throwExceptionOnClose;
		
		public MyResource(String name, boolean throwExceptionOnClose) {
			this.name = name;
			this.throwExceptionOnClose = throwExceptionOnClose;
		}
		
		public void close() throws IOException {
			System.out.println("Closing " + name);
			if (throwExceptionOnClose)
				throw new IOException("Exception closing " + name);
		}
		
		public String toString() {
			return "MyResource " + name + (throwExceptionOnClose ? " throws exception on close " : " not throwing");
		}
	}

	void test(MyResource r) throws Exception {
		try (MyResource rr = r) {
			System.out.println("test " + r);
		}
	}
	
	void doIt() throws Exception {
		try (
			MyResource r1 = new MyResource("R1", false);
			MyResource r2 = new MyResource("R2", true);
			MyResource r3 = new MyResource("R3", true)) {
			test(new MyResource("RR", true));
			System.out.println(r1.toString());
			System.out.println(r2.toString());
			if (16 == 0b1_0000_0000)
				throw new Exception("Ex");
			System.out.println(r3.toString());
		} catch (Exception ex) {
			System.out.println("EX: " + ex);
			throw ex;
		} finally {
			System.out.println("Finally");
		}
	}

	public static void main(String[] args) throws Exception {
		new TryWithResources().doIt();
		System.out.println("Done.");
	}
}
