package com.test.concurrent;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.slavi.util.Marker;
import com.slavi.util.Util;

public class TestHyperThreading {

	static class Task implements Runnable {

		String name;
		CountDownLatch start;
		CountDownLatch countDown;
		
		public Task(String name, CountDownLatch start, CountDownLatch countDown) {
			this.name = name;
			this.start = start;
			this.countDown = countDown;
		}
		
		public void run() {
			try {
				start.await();
				for (int i = 0; i < 1000; i++) {
					for (int j = 0; j < 1000; j++) {
						for (int k = 0; k < 10000; k++) {
							
						}						
					}					
				}				
				countDown.countDown();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}		
	}
	
	static void doIt(int numThreads) throws InterruptedException {
		ExecutorService exec = Executors.newFixedThreadPool(numThreads);
		CountDownLatch start = new CountDownLatch(1);
		CountDownLatch countDown = new CountDownLatch(numThreads);
		for (int i = 0; i < numThreads; i++) {
			exec.submit(new Task(Integer.toString(i), start, countDown));
		}
		System.out.println("\n\nStarting all threads (" + numThreads + ")");
		Marker.mark();
		start.countDown();
		countDown.await();
		Marker.State state = Marker.release();
		System.out.println("Ratio is " + Util.getFormatedMilliseconds((state.end - state.start) / numThreads));
		exec.shutdown();
	}
	
	public static void main(String[] args) throws Exception {
		for (int i = 1; i < 10; i++) {
			doIt(i);
		}
	}
}
