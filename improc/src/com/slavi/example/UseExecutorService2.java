package com.slavi.example;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.slavi.example.UseExecutorService.MainTask;

public class UseExecutorService2 {
	static final int MaxJobsPerTask = 3;

	public static interface SteppedParalelTask<V> {
		void onPrepare();
		Queue<Callable<V>> getNextStepTasks();
		void onError(Callable<V> task, Exception e);
		void onJobFinished(Callable<V> task, V result);
		void onFinally();
	}
	
	public static class MySteppedParallelTask implements SteppedParalelTask<Void> {

		int numSteps;
		
		int curStep;
		
		int jobCount;

		protected static class InternalJob implements Callable<Void> {
			private int id;		
			
			public InternalJob(int id) {
				this.id = id;
			}
			
			public Void call() throws Exception {
				System.out.println("Started job   " + id + " (" + Thread.currentThread().getId() + ")");
				Thread.sleep(500);
				System.out.println("Finished job  " + id + " (" + Thread.currentThread().getId() + ")");
				return null;
			}
		}

		public MySteppedParallelTask(int numSteps) {
			this.numSteps = numSteps;
			curStep = 0;
			jobCount = 0;
		}
		
		public synchronized Queue<Callable<Void>> getNextStepTasks() {
			if (curStep >= numSteps)
				return null;
			curStep++;
			System.out.println("STEP " + curStep);
			Queue<Callable<Void>> result = new LinkedList<Callable<Void>>();
			for (int i = 0; i < MaxJobsPerTask; i++) {
				result.add(new InternalJob(jobCount++));
			}
			return result;
		}

		public void onError(Callable<Void> task, Exception e) {
			System.out.println(e);
		}

		public void onJobFinished(Callable<Void> task, Void result) {
		}

		public void onFinally() {
			System.out.println("FINALLY");
		}

		public void onPrepare() {
			System.out.print("PREPARE");
		}
	}
	
	
	public static class SteppedParalelTaskExecutor<V> {
		
		private ExecutorService exec;
		private volatile boolean finished = false;
		private volatile boolean taskCanceled = false;
		private Exception taskException = null;
		private int maxParallelTasks;
		SteppedParalelTask<V> task;
		
		public SteppedParalelTaskExecutor(ExecutorService exec, int maxParallelTasks, SteppedParalelTask<V> task) {
			if (maxParallelTasks < 1)
				maxParallelTasks = 1;
			this.exec = exec;
			this.maxParallelTasks = maxParallelTasks;
			this.task = task;
		}

		private Object lock = new Object();
		private HashSet<JobWrapper> jobsRunning = new HashSet<JobWrapper>();
		private Queue<Callable<V>> jobsToDo;

		private class JobWrapper implements Callable<Void> {
			private Callable<V> job;
			
			public JobWrapper(Callable<V> job) {
				this.job = job;
			}
			
			public Void call() {
				try {
					V jobResult = job.call();
					
					Callable<V> nextJob = null;
					boolean shouldMakeJobs = false;
					synchronized (lock) {	
						if (jobsToDo != null)
							nextJob = jobsToDo.poll();
						jobsRunning.remove(this);
						if (nextJob == null) {
							if (jobsRunning.isEmpty()) {
								shouldMakeJobs = true;
							}
						}
					}
					
					if (nextJob != null) {
						if (!taskCanceled)
							exec.submit(nextJob);
						task.onJobFinished(job, jobResult);
					} else if (shouldMakeJobs) {
						task.onJobFinished(job, jobResult);
						makeJobs();
					}
				} catch (Exception e) {
					synchronized (lock) {
						taskCanceled = true;
						if (taskException != null) // If more than one tasks throws an exception keep the first thrown one. 
							taskException = e;
						jobsRunning.remove(this);
					}
					try {
						task.onError(job, e);
					} catch (Exception ignoreThis) {
					}
					synchronized (lock) {
						if (jobsRunning.isEmpty()) {
							finished = true;
							try {
								task.onFinally();
							} catch (Exception ignoreThis) {
							}
							lock.notifyAll();
						}
					}
				}
				return null;
			}
		}
		
		private void makeJobs() {
			if (taskCanceled) {
				finished = true;
			}
			synchronized(lock) {
				if (!finished) {
					jobsToDo = task.getNextStepTasks();
					if ((jobsToDo == null) || (jobsToDo.peek() == null)) {
						finished = true;
					} else {
						int i = 0;
						while (i < maxParallelTasks) {
							Callable<V> job = jobsToDo.poll();
							if (job == null)
								break;
							JobWrapper jobWrapper = new JobWrapper(job);
							jobsRunning.add(jobWrapper);
							exec.submit(jobWrapper);					
						}
					}
				}
			}
			if (finished) {
				task.onFinally();
				lock.notifyAll();
				return;
			}
		}
		
		private class InternalFuture implements Future<Void> {
			public boolean cancel(boolean mayInterruptIfRunning) {
				boolean result = !isDone();
				if (result)
					taskCanceled = true;
				return result;
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
				while (!finished) {
					synchronized (lock) {
						if (!finished)
							lock.wait(unit.toNanos(timeout));
						if (finished) {
							if (taskException != null) {
								throw new ExecutionException("Error executing task/subtask", taskException);
							}
							if (taskCanceled) {
								throw new InterruptedException();
							}
							System.out.println("GOT RESULT");
							return null;
						}
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
		
		public Future<Void> start() {
			makeJobs();
			return new InternalFuture();
		}
	}

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		ExecutorService exec;
//		exec = Executors.newSingleThreadExecutor();
		exec = Executors.newFixedThreadPool(5);
		System.out.println("Creating main task" + " (" + Thread.currentThread().getId() + ")");
		MySteppedParallelTask task = new MySteppedParallelTask(3);
		Future<Void> ft = new SteppedParalelTaskExecutor(exec, 2, task).start();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		System.out.println("Submitted main task" + " (" + Thread.currentThread().getId() + ")");
		ft.get();
		System.out.println("Got answer from main task" + " (" + Thread.currentThread().getId() + ")");
		exec.shutdown();
		System.out.println("Main finished." + " (" + Thread.currentThread().getId() + ")");
	}	
}
