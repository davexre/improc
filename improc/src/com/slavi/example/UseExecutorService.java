package com.slavi.example;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class UseExecutorService {

	public static class MainTask {
		
		private ExecutorService exec;
		private int numTasks;
		private boolean taskCanceled = false;

		public MainTask(ExecutorService exec, int numTasks) {
			this.exec = exec;
			this.numTasks = numTasks;
		}

		private Object lock = new Object();
		private volatile int jobsRunning;

		public class Job implements Callable<Void> {
			private int id;		
			
			public Job(int id) {
				this.id = id;
			}
			
			public Void call() throws Exception {
				try {
					System.out.println("Started job   " + id + " (" + Thread.currentThread().getId() + ")");
					Thread.sleep(500);
					System.out.println("Finished job  " + id + " (" + Thread.currentThread().getId() + ")");
				} finally {
					synchronized (lock) {	
						jobsRunning--;
						if (jobsRunning == 0) {
							makeJobs();
						}
					}
				}
				return null;
			}
		}
		
		final int MaxJobsPerTask = 3;
		private volatile boolean finished = false;
		private int jobCount = 0;
		private void makeJobs() {
			if ((numTasks > 0) && (!taskCanceled)) {
				System.out.println("STARTING TASK " + numTasks + " (" + Thread.currentThread().getId() + ")");
				jobsRunning = MaxJobsPerTask;
				for (int i = 0; i < MaxJobsPerTask; i++) {
					int taskId = jobCount++;
					System.out.println("Creating  job " + taskId + " (" + Thread.currentThread().getId() + ")");
					exec.submit(new Job(taskId));
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}
					System.out.println("Submitted job " + taskId + " (" + Thread.currentThread().getId() + ")");
				}
				System.out.println("FINSHED SUBMITTING JOBS FOR TASK " + numTasks + " (" + Thread.currentThread().getId() + ")");
				numTasks--;
			} else {
				finished = true;
				lock.notifyAll();
				System.out.println("MAKEJOBS FINISHED");
			}
		}
		
		private class MyFuture implements Future<Void> {
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
						lock.wait(unit.toNanos(timeout));
						if (finished) {
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
				return finished | taskCanceled;
			}
			
		}
		
		public Future<Void> start() {
			makeJobs();
			return new MyFuture();
		}
	}
	
	public static void main(String[] args) throws InterruptedException, ExecutionException {
		ExecutorService exec;
		exec = Executors.newSingleThreadExecutor();
//		exec = Executors.newFixedThreadPool(5);
		System.out.println("Creating main task" + " (" + Thread.currentThread().getId() + ")");
		Future<Void> ft = new MainTask(exec, 3).start();
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
