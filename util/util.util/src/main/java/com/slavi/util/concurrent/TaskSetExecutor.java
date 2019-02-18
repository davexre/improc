package com.slavi.util.concurrent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Executes a set of tasks. The tasks may be either a Runnable or a Callable.
 * If one task fail, i.e. throws an Exception then all tasks are considered failed
 * and their execution will be aborted also.</br>
 * If the constructor {@link #TaskSetExecutor(ExecutorService)} is used then
 * the tasks are added using one of the add methods and after the last task is
 * added the method {@link #addFinished()} must be invoked.
 */
public class TaskSetExecutor implements Future<Void> {
	private class InternalTaskWrapper implements Callable {
		final Object task;
		InternalTaskWrapper(Object task) {
			this.task = task;
		}

		public Object call() {
			synchronized (tasks) {
				runningTasks++;
			}
			Object result = null;
			try {
				if (task instanceof Callable) {
					result = ((Callable<?>) task).call();
				} else if (task instanceof Runnable){
					((Runnable) task).run();
				} else {
					throw new Exception("Unsupported task type " + task);
				}
				onTaskFinished(task, result);
			} catch(Throwable t) {
				synchronized (tasks) {
					if (firstExceptionThatOccured == null)
						firstExceptionThatOccured = t;
				}
				cancel(true);
				try {
					onError(task, t);
				} catch (Throwable t2) {
				}
				return null;
			} finally {
				synchronized (tasks) {
					runningTasks--;
					finishedTasks++;
				}
			}
			isDone();
			return result;
		}
	}

	boolean aborted = false;
	boolean addingFinished = false;
	Throwable firstExceptionThatOccured = null;

	/**
	 * @param mayInterruptIfRunning The specified value is ignored. The behaviour is as if it was true.
	 */
	public boolean cancel(boolean mayInterruptIfRunning) {
		boolean oldAborted;
		synchronized (tasks) {
			oldAborted = aborted;
			aborted = true;
			addingFinished = true;
		}
		if (!oldAborted) {
			for (Future<?> task : tasks) {
				task.cancel(true);
			}
		}
		isDone();
		return !oldAborted;
	}

	final ExecutorService exec;
	final ArrayList<Future<?>> tasks;
	int runningTasks = 0;
	int finishedTasks = 0;

	public TaskSetExecutor(ExecutorService exec) {
		this.exec = exec;
		this.tasks = new ArrayList<Future<?>>();
	}

	public TaskSetExecutor(ExecutorService exec, List<?> tasks) {
		this(exec);
		for (Object task : tasks) {
			if (task instanceof Callable) {
				add((Callable<?>) task);
			} else if (task instanceof Runnable){
				add((Runnable) task);
			} else {
				throw new RuntimeException("Unsupported task type " + task);
			}
		}
		addFinished();
	}

	public int getRunningTasks() {
		synchronized (tasks) {
			return runningTasks;
		}
	}

	public int getTasksCount() {
		synchronized (tasks) {
			return tasks.size();
		}
	}

	public int getFinishedTasksCount() {
		synchronized (tasks) {
			return finishedTasks;
		}
	}

	boolean onFinallyInvoked = false;
	public boolean isDone() {
		if (exec.isShutdown()) {
			// Cancel must be callsed outside a synchronized block.
			cancel(true);
		}
		// The onFinally() must be called outside a synchronized block
		boolean tmpOnFinallyInvoked;
		synchronized (tasks) {
			if (aborted) {
				if (runningTasks != 0) {
					return false;
				}
			} else if (!addingFinished) {
				return false;
			} else if (finishedTasks != tasks.size()) {
				return false;
			}
			tmpOnFinallyInvoked = onFinallyInvoked;
			onFinallyInvoked = true;
		}
		// Invoke onFinally if not already invoked.
		if (!tmpOnFinallyInvoked) {
			try {
				onFinally();
			} catch (Throwable t) {
			}
			synchronized (tasks) {
				tasks.notifyAll();
			}
		}
		return true;
	}

	public boolean isCancelled() {
		return aborted;
	}

	private <T> Future<T> internalAdd(Object task) {
		if (task == null) {
			cancel(true);
			throw new NullPointerException();
		}
		if (addingFinished || Thread.currentThread().isInterrupted()) {
			cancel(true);
		}
		if (aborted) {
			throw new RejectedExecutionException();
		}
		Future<T> future = null;
		try {
			future = exec.submit(new InternalTaskWrapper(task));
			synchronized (tasks) {
				if (addingFinished || aborted) {
					future.cancel(true);
					throw new RejectedExecutionException();
				}
				tasks.add(future);
			}
			return future;
		} catch (RejectedExecutionException t) {
			cancel(true);
			throw t;
		} catch (Throwable t) {
			cancel(true);
			throw new RejectedExecutionException(t);
		}
	}

	public Future<Void> add(Runnable task) {
		return internalAdd(task);
	}

	public <T> Future<T> add(Callable<T> task) {
		return internalAdd(task);
	}

	public void addFinished() {
		synchronized (tasks) {
			addingFinished = true;
		}
		isDone();
	}

	public Void get() throws InterruptedException, ExecutionException {
		return get(0, TimeUnit.MILLISECONDS);
	}

	public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException {
		if (Thread.currentThread().isInterrupted())
			cancel(true);
		synchronized (tasks) {
			if (aborted)
				throw new InterruptedException();
			if (!addingFinished) {
				cancel(true);
				throw new CancellationException("Can not get result prior calling addFinished.");
			}
		}
		boolean done = isDone();
		synchronized (tasks) {
			if (!done && (!onFinallyInvoked)) {
				try {
					tasks.wait(unit.toMillis(timeout));
				} catch (InterruptedException e) {
					cancel(true);
					throw e;
				}
			}
			if (firstExceptionThatOccured != null)
				throw new ExecutionException(firstExceptionThatOccured);
			if (aborted)
				throw new CancellationException();
		}
		return null;
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
	public void onTaskFinished(Object task, Object result) throws Exception {
	}

	/**
	 * Invoked once after the all tasks has been completed or cancelled.
	 * <p>The method is invoked after:
	 * <ul>
	 * <li>the task has been canceled and all running sub-task have
	 * been aborted or finished</li>
	 * <li>an exception has been thrown by some sub-task or a method
	 * like {@link #onTaskFinished(Object, Object)} and all running sub-taks have
	 * been aborted</li>
	 * </ul>
	 * The method might not be invoked in case the ExecutorService is shutdown.
	 */
	public void onFinally() throws Exception {
	}

	public static <T> Future<Void> parallel(ExecutorService exec, int numberOfThreads, Iterator<T> iterator, Consumer<T> task) throws Exception {
		if (numberOfThreads <= 0)
			numberOfThreads = Runtime.getRuntime().availableProcessors();
		TaskSetExecutor taskSet = new TaskSetExecutor(exec);
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
		taskSet.addFinished();
		return taskSet;
	}

	public static <T> Future<Void> parallel(ExecutorService exec, Iterator<T> iterator, Consumer<T> task) throws Exception {
		return parallel(exec, 0, iterator, task);
	}
}
