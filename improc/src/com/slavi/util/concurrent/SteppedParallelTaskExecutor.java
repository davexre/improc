package com.slavi.util.concurrent;

import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SteppedParallelTaskExecutor<V> {
	
	ExecutorService exec;
	int maxParallelTasks;
	SteppedParallelTask<V> task;

	Object lock = new Object();
	volatile boolean finished = false;
	volatile boolean taskCanceled = false;
	Exception taskException = null;
	HashMap<SubtaskWrapper, Future<Void>> subtasksSubmitted = new HashMap<SubtaskWrapper, Future<Void>>();
	Queue<Callable<V>> subtasksToDo;
	
	public SteppedParallelTaskExecutor(ExecutorService exec, int maxParallelTasks, SteppedParallelTask<V> task) {
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
			Exception e = null;
			try {
				synchronized (lock) {
					if (taskCanceled) {
						subtasksSubmitted.remove(this);
						if (subtasksSubmitted.isEmpty())
							internalFinish();
						return null;
					}
				}
				V subtaskResult = subtask.call();
				
				Callable<V> nextSubtask = null;
				boolean finishedAllSubtasks = false;
				synchronized (lock) {	
					if (subtasksToDo != null)
						nextSubtask = subtasksToDo.poll();
					subtasksSubmitted.remove(this);
					if (nextSubtask == null) {
						if (subtasksSubmitted.isEmpty()) {
							finishedAllSubtasks = true;
						}
					}
				}
				
				if (nextSubtask != null) {
					if (!taskCanceled) {
						SubtaskWrapper subtaskWrapper = new SubtaskWrapper(nextSubtask);
						Future<Void> f = exec.submit(subtaskWrapper);
						subtasksSubmitted.put(subtaskWrapper, f);
					}
					task.onSubtaskFinished(subtask, subtaskResult);
				} else if (finishedAllSubtasks) {
					task.onSubtaskFinished(subtask, subtaskResult);
					makeSubtasks();
				}
			} catch (Exception ex) {
				e = ex;
			}
			if (e != null) {
				internalCancelTask(true);
				boolean shouldFinish;
				synchronized (lock) {
					if (taskException == null) // If more than one tasks throws an exception keep the first thrown one. 
						taskException = e;
					subtasksSubmitted.remove(this);
					shouldFinish = subtasksSubmitted.isEmpty();
				}
				try {
					task.onError(subtask, e);
				} catch (Exception ignoreThis) {
				}
				if (shouldFinish) {
					internalFinish();
				}
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
		}
	}
	
	void makeSubtasks() {
		if (taskCanceled) {
			internalFinish();
			return;
		}
		synchronized(lock) {
			if (!finished) {
				try {
					subtasksToDo = task.getNextStepTasks();
				} catch (Exception e) {
					taskCanceled = true;
					if (taskException == null) // If more than one tasks throws an exception keep the first thrown one. 
						taskException = e;
					internalFinish();
				}
				if ((subtasksToDo == null) || (subtasksToDo.peek() == null)) {
					internalFinish();
					return;
				}
				int i = 0;
				while (i < maxParallelTasks) {
					Callable<V> subtask = subtasksToDo.poll();
					if (subtask == null)
						break;
					SubtaskWrapper subtaskWrapper = new SubtaskWrapper(subtask);
					Future<Void> f = exec.submit(subtaskWrapper);
					subtasksSubmitted.put(subtaskWrapper, f);
				}
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
		} catch (Exception e) {
			synchronized (lock) {
				taskCanceled = true;
				taskException = e;					
			}
		}
		makeSubtasks();
		return new InternalFuture();
	}
}
