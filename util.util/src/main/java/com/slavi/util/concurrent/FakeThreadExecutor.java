package com.slavi.util.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Implements the ExecutorService interface without using any threads, instead
 * all tasks are executed immedeately as they are submitted for execution.
 */
public class FakeThreadExecutor implements ExecutorService {

	boolean shutdown = false;
	
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return shutdown;
	}

	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
		ArrayList<Future<T>> result = new ArrayList<Future<T>>(tasks.size());
		for (Callable<T> task : tasks) {
			result.add(submit(task));
		}
		return result;
	}

	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException {
		ArrayList<Future<T>> result = new ArrayList<Future<T>>(tasks.size());
		long maxTime = System.nanoTime() + unit.toNanos(timeout);
		for (Callable<T> task : tasks) {
			result.add(submit(task));
			if (System.nanoTime() >= maxTime) {
				FutureTask<T> canceledTask = new FutureTask<T>(task);
				canceledTask.cancel(true);
				result.add(canceledTask);
			}
		}
		return result;
	}

	public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
		Throwable lastException = null;
		for (Callable<T> task : tasks) {
			try {
				return submit(task).get();
			} catch (Throwable t) {
				lastException = t;
			}
		}
		throw new ExecutionException(lastException);
	}

	public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		Throwable lastException = null;
		long maxTime = System.nanoTime() + unit.toNanos(timeout);
		
		for (Callable<T> task : tasks) {
			try {
				return submit(task).get();
			} catch (Throwable t) {
				lastException = t;
			}
			if (System.nanoTime() >= maxTime) 
				throw new TimeoutException();
		}
		throw new ExecutionException(lastException);
	}

	public boolean isShutdown() {
		return shutdown;
	}

	public boolean isTerminated() {
		return shutdown;
	}

	public void shutdown() {
		shutdown = true;
	}

	public List<Runnable> shutdownNow() {
		shutdown = true;
		return Collections.EMPTY_LIST;
	}

	public <T> Future<T> submit(Callable<T> task) {
		if (task == null)
			throw new NullPointerException();
		if (shutdown || Thread.currentThread().isInterrupted())
			throw new RejectedExecutionException();
		FutureTask<T> result = new FutureTask<T>(task);
		try {
			result.run();
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return result;
	}

	public Future<?> submit(Runnable task) {
		return submit(task, null);
	}

	public <T> Future<T> submit(Runnable task, T resultType) {
		if (task == null)
			throw new NullPointerException();
		if (shutdown || Thread.currentThread().isInterrupted())
			throw new RejectedExecutionException();
		FutureTask<T> result = new FutureTask<T>(task, resultType);
		try {
			result.run();
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return result;
	}

	public void execute(Runnable command) {
		submit(command);
	}
}
