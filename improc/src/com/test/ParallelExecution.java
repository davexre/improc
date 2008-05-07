package com.test;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ParallelExecution {

	class SomeLengthyTask implements Runnable {
		int taskId;
		int amount;
		double dummy;
		
		public SomeLengthyTask(int taskId, int amount) {
			this.taskId = taskId;
			this.amount = amount;
		}
		
		public void run() {
			dummy = 1.0;
			System.out.println("Started  task with id = " + taskId);
			for (int i = amount; i >= 0; i--) {
				for (int j = amount; j >= 0; j--) {
					for (int k = amount; k >= 0; k--) {
						if (dummy != 0.0)
							dummy = i * j * k; 
					}
				}
			}
			System.out.println("Finished task with id = " + taskId);
		}
	}
	
	void test() throws InterruptedException {
		ExecutorService serv = Executors.newFixedThreadPool(2);
		long start = System.nanoTime();
		for (int i = 0; i < 5; i++)
			serv.execute(new SomeLengthyTask(i, (new Random()).nextInt(500) * 10));
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
