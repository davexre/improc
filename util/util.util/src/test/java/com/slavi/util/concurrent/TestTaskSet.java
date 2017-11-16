package com.slavi.util.concurrent;

import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TestTaskSet {

	void doIt() throws Exception {
		ExecutorService exec = Executors.newFixedThreadPool(2);

		TaskSet tasks = new TaskSet(exec);
		System.out.println("Creating tasks");
		for (int i = 0; i < 8; i++) {
			int id = i;
			tasks.add(() -> {
				try {
					System.out.println("Task " + id + ": started");
					if (id == 4) {
						Thread.sleep(200);
						throw new Exception("Aborting...");
					}
					Thread.sleep(1000);
					System.out.println("Task " + id + ": completed");
				} catch (Exception e) {
					System.out.println("Task " + id + ": failed " + e.getMessage());
					throw new CompletionException(e);
				}
			});
		}
		
		System.out.println("Waiting for tasks to complete");
		Thread.sleep(500);
		try {
			tasks.run().get();
			//tasks.run().complete(null);
		} catch (Exception e) {
			System.out.println("Got exception");
			e.printStackTrace(System.out);
		}
		System.out.println("Shutting down the executor service");
		exec.shutdown();
		System.out.println("Waiting for threads to finish");
		exec.awaitTermination(1, TimeUnit.DAYS);
	}

	public static void main(String[] args) throws Exception {
		new TestTaskSet().doIt();
		System.out.println("Done.");
	}
}
