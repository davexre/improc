package com.slavi.util.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class CompletableFutureDemo {

	Function toFunction(final Supplier supplier) {
		return new Function() {
			public Object apply(Object t) {
				return supplier.get();
			}
		};
	}

	Supplier makeTask(final String name) {
		return makeTask(name, 1000);
	}

	Supplier makeTask(final String name, final long delay) {
		return makeTask(name, delay, false);
	}

	Supplier makeTask(final String name, final long delay, boolean shouldFail) {
		return new Callable() {
			public Object call() {
				System.out.println("start " + name + (shouldFail ? " WILL fail" : ""));
				if (shouldFail)
					throw new RuntimeException("Exception in " + name);
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println("exit  " + name);
				return null;
			}
		}::call;
	}

	public static void throwingException() throws Exception {
		System.out.println("Throwing exception");
		throw new Exception("Throwing exception");
	}

	void doIt() throws Exception {
		CompletableFuture.supplyAsync(() -> {
			try {
				throwingException();
			} catch (Exception e) {
				throw new CompletionException(e);
			}
			return null;
		}).get();
	}

	void doIt2() throws Exception {
		Object result = CompletableFuture
			.supplyAsync(() -> {
				System.out.println("1");
				return "1";
			}).thenApply((x) -> {
				System.out.println("2 got X=" + x);
				final CompletableFuture r = CompletableFuture.allOf((CompletableFuture[])
					IntStream
						.rangeClosed(1, 10)
						.mapToObj((i) -> CompletableFuture.supplyAsync(makeTask("Task " + i, 1000, i==3)))
						.toArray(CompletableFuture[]::new));
				r.exceptionally((e) -> {
						//((Throwable) e).printStackTrace();
						System.out.println("Error. Aborting...");
						//r.cancel(true);

						return null;
					});
				return r;
			}).get()
			.thenRun(() -> {
				System.out.println("3");
			})
			.get();

		System.out.println(result);
	}

	void doIt1() throws Exception {
		CompletableFuture
			.supplyAsync(makeTask("Entry task", 1000))
			.thenAccept((action) ->
				CompletableFuture
					.allOf((CompletableFuture[])
						IntStream
							.rangeClosed(1, 10)
							.mapToObj((i) -> CompletableFuture.supplyAsync(makeTask("Task " + i, 1000)))
							.toArray(CompletableFuture[]::new))
					.thenRun(() -> makeTask("a2"))
					.thenRun(() -> { System.out.println("Finished"); })
			)
			.get();
	}

	public static void main(String[] args) throws Exception {
		new CompletableFutureDemo().doIt();
		System.out.println("Done.");
		Thread.sleep(5000);
	}
}
