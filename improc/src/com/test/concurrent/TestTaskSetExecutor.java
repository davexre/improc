package com.test.concurrent;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import com.slavi.util.concurrent.TaskSetExecutor;
import com.slavi.util.ui.SwtUtil;

public class TestTaskSetExecutor {
	private static class MyTask implements Runnable {
		static AtomicInteger taskCount = new AtomicInteger();
		
		int id = taskCount.incrementAndGet();

		public void run() {
			System.out.println("Started task id " + id);
			long end = System.currentTimeMillis() + 1000;
			while (end > System.currentTimeMillis()) {
				try {
					Thread.sleep(100);
				} catch (Exception e) {
				}
			}
			System.out.println("Finished task id " + id);
		}
	}
	
	static ArrayList<MyTask> getTasks() {
		ArrayList<MyTask> result = new ArrayList<MyTask>();
		for (int i = 0; i < 10; i++)
			result.add(new MyTask());
		return result;
	}
	
	static Thread mainThread = null;
	
	static class MyJob implements Callable<Void> {
		ExecutorService exec;
		
		MyJob(ExecutorService exec) {
			this.exec = exec;
		}
		
		public Void call() throws Exception {
			TaskSetExecutor tset = new TaskSetExecutor(exec, getTasks());
			tset.get();
			return null;
		}
	}
	
	public static void main(String[] args) throws Exception {
		mainThread = Thread.currentThread();
		ExecutorService exec;
		exec = Executors.newFixedThreadPool(3);
//		exec = Executors.newSingleThreadScheduledExecutor();
//		exec = new FakeThreadExecutor();

		try {
			SwtUtil.openWaitDialog("Test", new MyJob(exec), -1);
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			exec.shutdown();
		}
		System.out.println("Done.");
	}
}
