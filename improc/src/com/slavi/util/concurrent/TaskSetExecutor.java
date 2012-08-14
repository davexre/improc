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
	private class InternalTaskWrapper implements Callable<Void> {
		final Object task;
		InternalTaskWrapper(Object task) {
			this.task = task;
		}
		
		public Void call() {
			synchronized (tasks) {
				if (aborted) {
					isDone();
					return null;
				}
				runningTasks++;
			}
			try {
				Object result = null;
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
					abort();
				}
				try {
					onError(task, t);
				} catch (Throwable t2) {
				}
			}
			synchronized (tasks) {
				runningTasks--;
				finishedTasks++;
			}
			isDone();
			return null;
		}
	}
	
	boolean aborted = false;
	boolean addingFinished = false;
	Throwable firstExceptionThatOccured = null; 
	
	public void abort() {
		synchronized (tasks) {
			if (!aborted) {
				for (Future<?> task : tasks) {
					task.cancel(true);
				}
			}
			aborted = true;
			isDone();
		}
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
		boolean localOnFinallyInvoked;
		synchronized (tasks) {
			if (!addingFinished) {
				return false;
			}
			if (aborted) {
				if (runningTasks != 0) {
					return false;
				}
			} else if (exec.isShutdown()) {
				abort();
			} else if (finishedTasks != tasks.size()) {
				return false;
			}
			localOnFinallyInvoked = onFinallyInvoked;
			onFinallyInvoked = true;
		}
		// Invoke onFinally if not already invoked.
		if (!localOnFinallyInvoked) {
			try {
				onFinally();
			} catch (Throwable t) {
			}
			tasks.notifyAll();
		}
		return true;
	}
	
	public boolean isCanceled() {
		synchronized (tasks) {
			if (aborted)
				return true;
			if (exec.isShutdown()) {
				abort();
				return true;
			}					
			return aborted;
		}
	}
	
	private void internalAdd(Object task) {
		if (task == null) {
			abort();
			throw new NullPointerException();
		}
		if (addingFinished) {
			abort();
			throw new RejectedExecutionException();
		}				
		if (aborted) {
			throw new RejectedExecutionException();
		}
		if (Thread.currentThread().isInterrupted()) {
			abort();
			throw new RejectedExecutionException();
		}
		Future<Void> future = null;
		try {
			future = exec.submit(new InternalTaskWrapper(task));
			synchronized (tasks) {
				if (addingFinished || aborted) {
					future.cancel(true);
				}
				tasks.add(future);
			}
		} catch (Throwable t) {
			abort();
			throw new RejectedExecutionException(t);
		}
	}
	
	public void add(Runnable task) {
		internalAdd(task);
	}

	public void add(Callable<?> task) {
		internalAdd(task);
	}
	
	public void addFinished() {
		synchronized (tasks) {
			addingFinished = true;
		}
	}
	
	public void get() throws InterruptedException, ExecutionException, TimeoutException {
		get(0);
	}
	
	public void get(long timeoutMillis) throws InterruptedException, ExecutionException, TimeoutException {
		if (Thread.currentThread().isInterrupted())
			abort();
		synchronized (tasks) {
			if (aborted)
				throw new CancellationException();
			if (!addingFinished) {
				abort();
				throw new CancellationException("Can not get result prior calling addFinished.");
			}
			if (!isDone())
				try {
					tasks.wait(timeoutMillis);
				} catch (InterruptedException e) {
					abort();
					throw e;
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
