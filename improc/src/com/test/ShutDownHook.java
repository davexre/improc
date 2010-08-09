package com.test;

public class ShutDownHook {
	public static void main(String[] args) {
		System.err.println("Starting main program");
		Runtime rt = Runtime.getRuntime();
		System.err.println("Main: adding shutdown hook");
		rt.addShutdownHook(new Thread() {
			public void run() {
				// In real life this might close a Connection or something.
				System.err.println("Running my shutdown hook");
			}
		});
		System.err.println("Main: calling Runtime.exit()");
		System.exit(0);
	}
}
