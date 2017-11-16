package com.slavi.util.concurrent;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.slavi.util.concurrent.IntIterator;
import com.slavi.util.concurrent.TaskSet;

public class TestTaskSetParallelize {

	void doIt() throws Exception {
		ExecutorService exec = Executors.newCachedThreadPool();
		System.out.println("Creating parallel task");
		CompletableFuture f = TaskSet.parallelize(exec, 2, new IntIterator(30), (id) -> {
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
		System.out.println("Waiting all task to finish");
		try {
			f.get();
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
		new TestTaskSetParallelize().doIt();
		System.out.println("Done.");
	}
}
