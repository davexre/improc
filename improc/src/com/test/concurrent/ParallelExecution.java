package com.test.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ParallelExecution {

	static class SomeLengthyTask implements Runnable {
		int taskId;
		
		public SomeLengthyTask(int taskId) {
			this.taskId = taskId;
		}
		
		public void run() {
			System.out.println("Started  task with id = " + taskId);
			try {
				Thread.sleep(500);
			} catch (Exception e) {
			}
			System.out.println("Finished task with id = " + taskId);
		}
	}
	
	void test() throws InterruptedException {
//		ExecutorService serv = new FakeThreadExecutor();
		ExecutorService serv = Executors.newFixedThreadPool(2);
//		ExecutorService serv = Executors.newSingleThreadExecutor();
		long start = System.nanoTime();
		for (int i = 0; i < 5; i++)
			serv.execute(new SomeLengthyTask(i));
		System.out.println("Tasks submitted");
		serv.shutdown();
		serv.awaitTermination(0, TimeUnit.SECONDS);
		long end = System.nanoTime();
		System.out.println("Elapsed " + (end - start));
	}
	
	public static void main(String[] args) throws InterruptedException {
		ParallelExecution test = new ParallelExecution();
		test.test();
		System.out.println(Runtime.getRuntime().maxMemory()/1024/1024);
	}
}
