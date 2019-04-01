package com.slavi.util.concurrent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Consumer;

/**
 * Executes a set of tasks. If one task fails, i.e. throws an Exception then
 * all tasks are considered failed and their execution will be aborted via
 * Future.cancel(true) which effectively invokes Thread.interrupt().</br>
 * If a task fails it can safely wrap any exceptions in CompletionException.
 * The first thrown exception will be raised again upon run().get().
 */
public class TaskSet {
	final ExecutorService exec;
	final CompletableFuture result = new CompletableFuture() {
		public boolean cancel(boolean mayInterruptIfRunning) {
			return mayInterruptIfRunning ? completeExceptionally(new InterruptedException()) : super.cancel(false);
		}
	};
	final ArrayList<Future> tasks = new ArrayList<>();
	int remaining = 0;
	boolean addingFinished = false;

	public TaskSet(ExecutorService exec) {
		this.exec = exec;
		result.handle((r, throwable) -> {
			synchronized (tasks) {
				addingFinished = true;
				for (Future f : tasks) {
					f.cancel(true);
				}
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
		return addInternal(task);
	}

	public TaskSet add(Callable task) {
		return addInternal(task);
	}

	private TaskSet addInternal(Object task) {
		synchronized (tasks) {
			if (result.isDone() || addingFinished) {
				throw new RejectedExecutionException();
			}
			remaining++;
		}
		Future f = exec.submit(() -> {
			try {
				if (!result.isDone()) {
					if (task instanceof Runnable)
						((Runnable) task).run();
					else if (task instanceof Callable)
						((Callable) task).call();
					boolean lastOne;
					synchronized (tasks) {
						lastOne = (0 == --remaining) && addingFinished;
					}
					onTaskFinished(task);
					if (lastOne) {
						result.complete(null);
					}
				}
			} catch (Throwable t) {
				result.completeExceptionally(t);
				onError(task, t);
			}
			return null; // This line forces the usage of submit(Callable)
		});
		synchronized (tasks) {
			if (result.isDone() || addingFinished) {
				f.cancel(true);
				throw new RejectedExecutionException();
			}
			tasks.add(f);
		}
		return this;
	}

	public int getTasksCount() {
		synchronized (tasks) {
			return tasks.size();
		}
	}

	public int getFinishedTasksCount() {
		synchronized (tasks) {
			return tasks.size() - remaining;
		}
	}

	public int getRemainingTasksCount() {
		synchronized (tasks) {
			return remaining;
		}
	}

	/**
	 * Invoked upon an exception in some of the sub-tasks. The method MUST BE
	 * thread safe, i.e. may be invoked multiple time from different threads.
	 */
	public void onError(Object task, Throwable e) throws Exception {
	}

	/**
	 * Invoked upon an successful execution of a task. The method MUST BE
	 * thread safe, i.e. may be invoked multiple time from different threads.
	 */
	public void onTaskFinished(Object task) throws Exception {
	}

	public TaskSet addAll(Runnable ... tasks) {
		for (Runnable t : tasks)
			add(t);
		return this;
	}

	public TaskSet addAll(Iterable<Runnable> tasks) {
		for (Runnable t : tasks)
			add(t);
		return this;
	}

	public static <T> CompletableFuture<Void> parallel(ExecutorService exec, int numberOfThreads, Iterator<T> iterator, Consumer<T> task) throws Exception {
		if (numberOfThreads <= 0)
			numberOfThreads = Runtime.getRuntime().availableProcessors();
		TaskSet taskSet = new TaskSet(exec);
		final Object sync = new Object();
		for (int i = 0; i < numberOfThreads; i++) {
			taskSet.add(() -> {
				while (true) {
					if (Thread.interrupted())
						Thread.currentThread().interrupt();
					T item;
					synchronized (sync) {
						item = iterator.hasNext() ? iterator.next() : null;
					}
					task.accept(item);
				}
			});
		}
		return taskSet.run();
	}

	public static <T> CompletableFuture<Void> parallel(ExecutorService exec, Iterator<T> iterator, Consumer<T> task) throws Exception {
		return parallel(exec, 0, iterator, task);
	}
}
