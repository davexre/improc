package com.slavi.util.concurrent;

import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class CompletableFutureDemo {

	Supplier makeTask(final String name, final long delay) {
		return new Callable() {
			public Object call() {
				System.out.println("start " + name);
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
	
	void doIt() throws Exception {
		CompletableFuture
			.supplyAsync(makeTask("Entry task", 1000))
			.allOf(
					IntStream
						.rangeClosed(1, 10)
						.mapToObj((i) -> CompletableFuture.supplyAsync(makeTask("Task " + i, 1000)))
						.toArray(CompletableFuture[]::new)
					)
			
			.thenRun(() -> {
				System.out.println("Finished");
			})
			.get();
	}

	public static void main(String[] args) throws Exception {
		new CompletableFutureDemo().doIt();
		System.out.println("Done.");
	}
}
