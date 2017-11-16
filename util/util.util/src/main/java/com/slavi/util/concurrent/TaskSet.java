package com.slavi.util.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Consumer;

/**
 * Executes a set of tasks. If one task fails, i.e. throws an Exception then 
 * all tasks are considered failed and their execution will be aborted via
 * Future.cancel(true) which effectively invokes Thread.interrupt().</br>
 */
public class TaskSet {
	final ExecutorService exec;
	final CompletableFuture result = new CompletableFuture();
	final ArrayList<Future> tasks = new ArrayList<>();
	int remaining = 0;
	boolean addingFinished = false;

	public TaskSet(ExecutorService exec) {
		this.exec = exec;
		result.handle((r, throwable) -> {
			for (Future f : tasks) {
				f.cancel(true);
			}
			return null;
		});
	}

	public CompletableFuture<Void> run() {
		boolean alreadyFinished;
		synchronized (tasks) {
			addingFinished = true;
			alreadyFinished = remaining == 0;
		}
		if (alreadyFinished)
			result.complete(null);
		return result;
	}

	public TaskSet add(Runnable task) {
		synchronized (task) {
			if (result.isDone() || addingFinished) {
				throw new RejectedExecutionException();
			}
			remaining++;
		}
		Future f = exec.submit(() -> {
			try {
				if (!result.isDone()) {
					task.run();
					boolean lastOne;
					synchronized (tasks) {
						lastOne = (0 == remaining--) && addingFinished;
					}
					if (lastOne) {
						result.complete(null);
					}
				}
			} catch (Throwable t) {
				result.completeExceptionally(t);
			}
			return null; // This line forces the usage of submit(Callable)
		});
		synchronized (task) {
			if (result.isDone() || addingFinished) {
				f.cancel(true);
				throw new RejectedExecutionException();
			}
			tasks.add(f);
		}
		return this;
	}

	public TaskSet addAll(Runnable ... tasks) {
		for (Runnable t : tasks)
			add(t);
		return this;
	}

	public TaskSet addAll(Collection<Runnable> tasks) {
		for (Runnable t : tasks)
			add(t);
		return this;
	}

	public static <T> CompletableFuture<Void> parallelize(ExecutorService exec, int numberOfThreads, Iterator<T> iterator, Consumer<T> task) throws Exception {
		if (numberOfThreads <= 0)
			numberOfThreads = Runtime.getRuntime().availableProcessors();
		TaskSet taskSet = new TaskSet(exec);
		for (int i = 0; i < numberOfThreads; i++) {
			taskSet.add(() -> {
				while (true) {
					if (Thread.interrupted())
						Thread.currentThread().interrupt();
					T item;
					try {
						item = iterator.next();
					} catch (NoSuchElementException e) {
						break;
					}
					task.accept(item);
				}
			});
		}
		return taskSet.run();
	}

	public static <T> CompletableFuture<Void> parallelize(ExecutorService exec, Iterator<T> iterator, Consumer<T> task) throws Exception {
		return parallelize(exec, 0, iterator, task);
	}
}
