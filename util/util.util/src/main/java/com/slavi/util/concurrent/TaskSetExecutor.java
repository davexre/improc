package com.slavi.util.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Executes a set of tasks. The tasks may be either a Runnable or a Callable.
 * If one task fail, i.e. throws an Exception then all tasks are considered failed
 * and their execution will be aborted also.</br>
 * If the constructor {@link #TaskSetExecutor(ExecutorService)} is used then
 * the tasks are added using one of the add methods and after the last task is
 * added the method {@link #addFinished()} must be invoked.   
 */
public class TaskSetExecutor {
	@SuppressWarnings("rawtypes")
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
				abort();
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
	
	public void abort() {
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
		// The onFinally() must be called outside a synchronized block 
		boolean tmpOnFinallyInvoked;
		synchronized (tasks) {
			if (aborted) {
				if (runningTasks != 0) {
					return false;
				}
			} else if (exec.isShutdown()) {
				abort();
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
	
	public boolean isCanceled() {
		return aborted;
	}
	
	@SuppressWarnings("unchecked")
	private <T> Future<T> internalAdd(Object task) {
		if (task == null) {
			abort();
			throw new NullPointerException();
		}
		if (addingFinished || Thread.currentThread().isInterrupted()) {
			abort();
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
			abort();
			throw t;
		} catch (Throwable t) {
			abort();
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
	
	public void get() throws InterruptedException, ExecutionException, TimeoutException {
		get(0);
	}
	
	public void get(long timeoutMillis) throws InterruptedException, ExecutionException, TimeoutException {
		if (Thread.currentThread().isInterrupted())
			abort();
		synchronized (tasks) {
			if (aborted)
				throw new InterruptedException();
			if (!addingFinished) {
				abort();
				throw new CancellationException("Can not get result prior calling addFinished.");
			}
		}
		boolean done = isDone();
		synchronized (tasks) {
			if (!done && (!onFinallyInvoked)) {
				try {
					tasks.wait(timeoutMillis);
				} catch (InterruptedException e) {
					abort();
					throw e;
				}
			}
			if (firstExceptionThatOccured != null)
				throw new ExecutionException(firstExceptionThatOccured);
			if (aborted) 
				throw new CancellationException();
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
}
