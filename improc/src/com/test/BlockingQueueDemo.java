package com.test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class BlockingQueueDemo {

	public static class Task {
		final String taskId;
		
		public Task(String taskId) {
			this.taskId = taskId;
		}
	}
	
	public static class Worker implements Runnable {
		final String workerName;
		final BlockingQueue<? extends Task> tasks;
		
		public Worker(String workerName, BlockingQueue<? extends Task> tasks) {
			this.workerName = workerName;
			this.tasks = tasks;
		}
		
		public void run() {
			System.out.println("Worker " + workerName + " started.");
			try {
				Task task;
				while ((task = tasks.take()) != null) {
					System.out.println("Worker " + workerName + " got task " + task.taskId);
					Thread.sleep(1500);
				}
			} catch (InterruptedException e) {
			}
			System.out.println("Worker " + workerName + " finished.");
		}
	}
	
	Thread[] workerThreads;
	Worker[] workers;
	BlockingQueue<Task> tasks;
	
	public void doIt() {
		// Create workers
		int numberOfWorkers = 5;
		workers = new Worker[numberOfWorkers];
		workerThreads = new Thread[numberOfWorkers];
		tasks = new ArrayBlockingQueue<Task>(10); 
		for (int i = 0; i < numberOfWorkers; i++) {
			workers[i] = new Worker(Integer.toString(i), tasks);
			workerThreads[i] = new Thread(workers[i]);
			workerThreads[i].start();			
		}
		
		// Make some job
		int maxTasks = 20;
		try {
			for (int i = 0; i < maxTasks; i++) {
				tasks.put(new Task(Integer.toString(i)));
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println("Finished producing tasks.");
		
		// Wait for workers to finish
		while (!tasks.isEmpty()) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("Stopping the workers.");
		// Stop the workers
		for (Thread thread : workerThreads) 
			thread.interrupt();
		System.out.println("Workers stopped.");
	}
	
	public static void main(String[] args) {
		BlockingQueueDemo test = new BlockingQueueDemo();
		test.doIt();
	}
}
