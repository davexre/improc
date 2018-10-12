package com.slavi.lang;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestThreadLocal {

	ThreadLocal<String> tl = new ThreadLocal<>();

	public void doIt(String[] args) throws Exception {
		ExecutorService exec = Executors.newFixedThreadPool(1);
		System.out.println(exec.submit(() -> {
			return tl.get();
		}).get());

		exec.submit(() -> {
			tl.set("My val");
		}).get();

		System.out.println(exec.submit(() -> {
			return tl.get();
		}).get());
	}

	public static void main(String[] args) throws Exception {
		new TestThreadLocal().doIt(args);
		System.out.println("Done.");
	}
}
