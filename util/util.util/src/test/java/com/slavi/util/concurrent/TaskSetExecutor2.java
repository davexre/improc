package com.slavi.util.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskSetExecutor2 {

	public static CompletableFuture<Void> allOf(Executor exec, List<Runnable> tasks) {
		CompletableFuture<Void> r = new CompletableFuture();
		List<CompletableFuture> list = new ArrayList<>(tasks.size());
		for (Runnable t : tasks) {
			if (r.isDone())
				break;
			CompletableFuture<Void> cf = exec == null ? CompletableFuture.runAsync(t) : CompletableFuture.runAsync(t, exec);
			list.add(cf);
			cf.exceptionally((x) -> {
				System.out.println("Failed task " + t.toString());
				r.completeExceptionally(x);
				return null;
			});
		}
		r.exceptionally((x) -> {
			System.out.println("Exception handler: Canceling all tasks");
			for (CompletableFuture f : list) {
				f.cancel(true);
			}
			System.out.println("Exception handler: All tasks canceled");
			return null;
		});
		CompletableFuture.allOf(list.toArray(new CompletableFuture[list.size()])).thenRun(() -> r.complete(null));
		return r;
	}
	
	void doIt() throws Exception {
		ExecutorService exec = Executors.newFixedThreadPool(2);

		ArrayList<Runnable> tasks = new ArrayList<>();
		for (int i = 0; i < 8; i++) {
			int id = i;
			tasks.add(new Runnable() {
				public void run() {
					try {
						System.out.println("Task " + id + ": started");
						if (id == 4) {
							Thread.sleep(200);
							throw new Exception();
						}
						Thread.sleep(1000);
						System.out.println("Task " + id + ": completed");
					} catch (Exception e) {
						System.out.println("Task " + id + ": failed");
						throw new CompletionException(e);
					}
				}
				
				public String toString() {
					return "Task " + id;
				}
			});
		}
		
		System.out.println("Starting all tasks");
		CompletableFuture f = allOf(exec, tasks);
		System.out.println("Waiting for tasks to complete");
		try {
			f.get();
		} catch (ExecutionException e) {
			System.out.println("Got exception");
		}
		System.out.println("Shutting down the executor service");
		exec.shutdown();
	}

	public static void main(String[] args) throws Exception {
		new TaskSetExecutor2().doIt();
		System.out.println("Done.");
	}
}
