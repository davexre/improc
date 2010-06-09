package com.test.concurrent;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.slavi.util.Marker;
import com.slavi.util.Util;

public class TestHyperThreading {

	static class Task implements Runnable {
		String name;
		CountDownLatch countDown;
		CountDownLatch startLatch;
		
		public Task(String name, CountDownLatch startLatch, CountDownLatch countDown) {
			this.name = name;
			this.startLatch = startLatch;
			this.countDown = countDown;
		}
		
		public void run() {
			try {
				startLatch.await();
				long start = System.currentTimeMillis();
				for (int i = 0; i < 1000; i++) {
					for (int j = 0; j < 1000; j++) {
						for (int k = 0; k < 10000; k++) {
							
						}					
					}				
				}
				long end = System.currentTimeMillis();
//				System.out.println("Task " + name + " finished in " + Util.getFormatedMilliseconds(end - start));
				countDown.countDown();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	static void doIt(int numThreads) throws InterruptedException {
		CountDownLatch startLatch = new CountDownLatch(1);
		CountDownLatch countDown = new CountDownLatch(numThreads);
		ExecutorService exec = Executors.newFixedThreadPool(numThreads);
		for (int i = 0; i < numThreads; i++) {
			exec.submit(new Task(Integer.toString(i), startLatch, countDown));
		}
		System.out.println("Starting all threads (" + numThreads + ")\n\n");
		Marker.mark();
		startLatch.countDown();
		countDown.await();
		Marker.State stamp = Marker.release();
		System.out.println("\n\n Ratio: " + Util.getFormatedMilliseconds((stamp.end - stamp.start) / numThreads) + "\n\n");
		exec.shutdown();		
	}	
	
	public static void main(String[] args) throws InterruptedException {
		for (int i = 1; i < 10; i++) {
			doIt(i);
		}
	}	
}
