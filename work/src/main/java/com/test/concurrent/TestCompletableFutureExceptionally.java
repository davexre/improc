package com.test.concurrent;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestCompletableFutureExceptionally {
	void doIt() throws Exception {
		ExecutorService exec = Executors.newCachedThreadPool();
		
		System.out.println("Creating Failtask");
		CompletableFuture cf = CompletableFuture.runAsync(() -> {
			try {
				System.out.println("Failtask: started");
				Thread.sleep(1000);
				System.out.println("Failtask: Will fail");
				throw new Exception();
			} catch (Exception e) {
				System.out.println("Failtask: Throwing exception");
				throw new CompletionException(e);
			}
		}, exec);

		System.out.println("Creating OnFailure task");
		CompletableFuture cf2 = cf.exceptionally((x) -> {
			try {
				System.out.println("OnFailure: started");
				Thread.sleep(1000);
				System.out.println("OnFailure: finished");
			} catch (InterruptedException e) {
				throw new CompletionException(e);
			}
			return null;
		});
		
		System.out.println("Creating NextTask task");
		CompletableFuture cf3 = cf.thenRun(() -> {
			try {
				System.out.println("NextTask: started");
				Thread.sleep(1000);
				System.out.println("NextTask: finished");
			} catch (InterruptedException e) {
				throw new CompletionException(e);
			}
		});
		
		System.out.println("Waiting for Failtask");
		try {
			cf.get();
		} catch (ExecutionException e) {
			System.out.println("Got exception");
		}
		System.out.println("Got result/failure for Failtask");
		exec.shutdown();
		System.out.println("Calling OnFailure get");
		cf2.get();
		System.out.println("Calling NextTask get");
		try {
			cf3.get();
		} catch (ExecutionException e) {
			System.out.println("Got exception from NextTask");
		}
	}

	public static void main(String[] args) throws Exception {
		new TestCompletableFutureExceptionally().doIt();
		System.out.println("Done.");
	}
}
