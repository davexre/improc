package com.test.concurrent;

import java.util.concurrent.ExecutorService;

import com.slavi.util.Util;
import com.slavi.util.concurrent.TaskSetExecutor;

public class BlockingThreadPoolExecutorTest {
	
	ExecutorService exec;
	
	public static class Task implements Runnable {
		final String taskName;
		
		public Task(String taskName) {
			this.taskName = taskName;
		}
		
		public void run() {
			System.out.println("Task " + taskName + " started.");
			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
			}
			System.out.println("Task " + taskName + " finished.");
		}
	}

	void doIt() throws Exception {
		exec = Util.newBlockingThreadPoolExecutor(2);
//		exec = new ThreadPoolExecutor(2, 2, 5, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(1));
		System.out.println("Creating tasks");
		TaskSetExecutor ts = new TaskSetExecutor(exec);
		for (int i = 0; i < 10; i++) {
			Task task = new Task(Integer.toString(i));
			System.out.println("Created task " + i);
			ts.add(task);
			System.out.println("Submitted task " + i);
		}
		ts.addFinished();
		System.out.println("Add finished");
		ts.get();
		System.out.println("Finished creating tasks");
		exec.shutdown();
	}

	public static void main(String[] args) throws Exception {
		new BlockingThreadPoolExecutorTest().doIt();
	}
}
