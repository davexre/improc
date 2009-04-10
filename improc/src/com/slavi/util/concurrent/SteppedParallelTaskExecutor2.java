package com.slavi.util.concurrent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SteppedParallelTaskExecutor2<V> {
	
	ExecutorService exec;
	SteppedParallelTask<V> task;

	Object lock = new Object();
	volatile boolean finished = false;
	volatile boolean taskCanceled = false;
	Throwable taskException = null;
	int runningTasks;
	ArrayList<Callable<V>> subtasksToDo = new ArrayList<Callable<V>>();
	
	HashMap<SubtaskWrapper, Future<Void>> subtasksSubmitted = new HashMap<SubtaskWrapper, Future<Void>>();
	int maxParallelTasks;
	
	public SteppedParallelTaskExecutor2(ExecutorService exec, int maxParallelTasks, SteppedParallelTask<V> task) {
		if (maxParallelTasks < 1)
			maxParallelTasks = 1;
		this.exec = exec;
		this.maxParallelTasks = maxParallelTasks;
		this.task = task;
	}

	private class SubtaskWrapper implements Callable<Void> {
		private Callable<V> subtask;
		
		public SubtaskWrapper(Callable<V> subtask) {
			this.subtask = subtask;
		}
		
		public Void call() {
			if (!taskCanceled) {
				try {
					V result = subtask.call();
					task.onSubtaskFinished(subtask, result);
				} catch (Throwable t) {
					synchronized (lock) {
						if (taskException == null)
							taskException = t;
						internalCancelTask(true);
					}
					try {
						task.onError(subtask, t); 
					} catch (Throwable t2) {
					}
				}
			}
			int curRunning;
			synchronized (lock) {
				curRunning = --runningTasks;
			}
			if (curRunning == 0) {
				makeSubtasks();
			}
			return null;
		}
	}
	
	void internalFinish() {
		boolean shouldNotify = false;
		synchronized (lock) {
			if (!finished) {
				finished = true;
				shouldNotify = true;
			}
		}
		if (shouldNotify) {
			try {
				task.onFinally();
				synchronized (lock) {
					lock.notifyAll();
				}
			} catch (Exception e) {
				synchronized (lock) {
					taskCanceled = true;
					if (taskException == null)
						taskException = e;
					lock.notifyAll();
				}
			}
		} else {
			System.err.println("Shit happens");
		}
	}
	
	void makeSubtasks() {
		if (taskCanceled) {
			internalFinish();
			return;
		}
		synchronized(lock) {
			if (finished) 
				return;
			try {
				subtasksToDo.clear();
				subtasksSubmitted.clear();
				Queue<Callable<V>> todo = task.getNextStepTasks();
				if (todo != null) {
					subtasksToDo.addAll(todo);
				}
				runningTasks = subtasksToDo.size();
			} catch (Exception e) {
				taskCanceled = true;
				if (taskException == null) // If more than one tasks throws an exception keep the first thrown one. 
					taskException = e;
				internalFinish();
				return;
			}
			if (runningTasks == 0) {
				internalFinish();
				return;
			}
		}
		for (int i = 0; i < subtasksToDo.size(); i++) {
			Callable<V> subtask = subtasksToDo.get(i);
			SubtaskWrapper subtaskWrapper = new SubtaskWrapper(subtask);
			try {
				Future<Void> f = exec.submit(subtaskWrapper);
				synchronized(lock) {
					subtasksSubmitted.put(subtaskWrapper, f);
				}
			} catch (Exception e) {
				internalCancelTask(true); 
				break;
			}
		}
	}

	void internalCancelTask(boolean mayInterrupt) {
		synchronized (lock) {
			if (!taskCanceled) {
				taskCanceled = true;
				for (Future<Void> f : subtasksSubmitted.values()) {
					f.cancel(mayInterrupt);
				}
			}
		}
	}
	
	class InternalFuture implements Future<Void> {
		public boolean cancel(boolean mayInterruptIfRunning) {
			internalCancelTask(mayInterruptIfRunning);
			return true;
		}

		public Void get() throws InterruptedException, ExecutionException {
			try {
				return get(0, TimeUnit.NANOSECONDS);
			} catch (TimeoutException e) {
				return null;
			}
		}

		public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException,
				TimeoutException {
			synchronized (lock) {
				if (!finished) {
					lock.wait(unit.toNanos(timeout));
				}
				if (finished) {
					if (taskException != null) {
						throw new ExecutionException("Error executing task/subtask", taskException);
					}
					if (taskCanceled) {
						throw new InterruptedException();
					}
					return null;
				}
			}
			return null;
		}

		public boolean isCancelled() {
			return taskCanceled;
		}

		public boolean isDone() {
			return finished || taskCanceled;
		}
		
	}

	/**
	 * Invokes {@link SteppedParallelTask#onPrepare()} then
	 * {@link SteppedParallelTask#getNextStepTasks()} and starts the subtasks.
	 * Returns a Future object which is used to check the status of the task
	 * and to obtain the result of the execution of the task (success/failure).
	 * @return
	 */
	public Future<Void> start() {
		try {
			task.onPrepare();
			makeSubtasks();
		} catch (Exception e) {
			synchronized (lock) {
				finished = true;
				taskCanceled = true;
				taskException = e;
			}
		}
		return new InternalFuture();
	}
}
