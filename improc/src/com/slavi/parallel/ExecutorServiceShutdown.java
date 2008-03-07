package com.slavi.parallel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.slavi.utils.Marker;

public class ExecutorServiceShutdown {

	static class SomeLengthyTask implements Runnable {
		int loops;
		
		public SomeLengthyTask(int loops) {
			this.loops = loops;
		}
		
		public void run() {
			double d = 0.0;
			for (int i = 0; i < loops; i++) 
				for (int j = 0; j < loops; j++)
					for (int k = 0; k < loops; k++)
						if (d != 0.0)
							d = 1.0 + i * j * k;
			System.out.println("ok.");
		}
	}
	
	public static void main(String[] args) throws InterruptedException {
		ExecutorService serv = Executors.newFixedThreadPool(3);
		Marker.mark("start");
		for (int i = 0; i < 50; i++) {
			Runnable task = new SomeLengthyTask(500);
			serv.execute(task);
		}
		serv.shutdown();
		while (!serv.isTerminated()) {
			System.out.println("awaiting");
			serv.awaitTermination(1000, TimeUnit.SECONDS);
		}
		Marker.release();
	}
	
}
