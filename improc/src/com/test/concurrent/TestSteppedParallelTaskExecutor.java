package com.test.concurrent;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.slavi.util.concurrent.SteppedParallelTask;
import com.slavi.util.concurrent.SteppedParallelTaskExecutor;

public class TestSteppedParallelTaskExecutor {
	static final int MaxJobsPerTask = 3;

	public static class MySteppedParallelTask implements SteppedParallelTask<Void> {

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
//				if (id == 5)
//					throw new Exception("InternalJob.call");
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
			System.out.println("ONERROR: " + e);
//			throw new RuntimeException("onError");
		}

		public void onSubtaskFinished(Callable<Void> task, Void result) {
//			throw new RuntimeException("onJobFinished");
		}

		public void onFinally() {
			System.out.println("FINALLY");
//			throw new RuntimeException("onFinally");
		}

		public void onPrepare() {
			System.out.println("PREPARE");
//			throw new RuntimeException("onPrepare");
		}
	}
	
	
	public static void main(String[] args) throws InterruptedException, ExecutionException {
		ExecutorService exec;
//		exec = Executors.newSingleThreadExecutor();
		exec = Executors.newFixedThreadPool(2);
		System.out.println("Creating main task" + " (" + Thread.currentThread().getId() + ")");
		MySteppedParallelTask task = new MySteppedParallelTask(3);
		Future<Void> ft = new SteppedParallelTaskExecutor(exec, 2, task).start();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		System.out.println("Submitted main task" + " (" + Thread.currentThread().getId() + ")");
		try {
			//ft.cancel(true);
			exec.shutdown();
			ft.get();
			System.out.println("Got answer from main task" + " (" + Thread.currentThread().getId() + ")");
		} finally {
			exec.shutdown();
		}
		System.out.println("Main finished." + " (" + Thread.currentThread().getId() + ")");
	}	
}
