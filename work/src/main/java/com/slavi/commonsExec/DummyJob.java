package com.slavi.commonsExec;

import java.io.InputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.slavi.util.ParseArgs;

public class DummyJob {
	
	int numThreads;
	int timeout;
	boolean silent;
	
	class DummyTask implements Runnable {
		String name;
		
		public DummyTask(String name) {
			this.name = name;
		}
		
		public void run() {
			if (!silent) {
				System.out.println("Starting task " + name);
			}
			try {
				long nextPrint = System.currentTimeMillis() + 1000;
				long endAt = System.currentTimeMillis() + timeout;
				while (System.currentTimeMillis() < endAt) {
					if (!silent && System.currentTimeMillis() >= nextPrint) {
						nextPrint = System.currentTimeMillis() + 1000;
						System.out.println("... still running task " + name);
					}
				}
			} catch (Exception e) {
				if (!silent) {
					System.out.println("Error in task " + name + "\n" + e.toString());
				}
			}
			if (!silent) {
				System.out.println("Exiting task " + name);
			}
		}
	}
	
	class DummyInput implements Runnable {
		public void run() {
			try {
				InputStream is = System.in;
				byte buf[] = new byte[256];
				int len;
				while ((len = is.read(buf)) > 0) {
					if (!silent) {
						System.out.write(buf, 0, len);
					}
				}
			} catch (Exception e) {
				if (!silent)
					e.printStackTrace();
			}
			if (!silent) {
				System.out.println("Exiting task Input reader");
			}
		}
	}
	
	void doIt(String[] args) throws Exception {
		ParseArgs pargs = ParseArgs.parse(args);
		numThreads = Integer.parseInt(pargs.getOption("-t", "1"));
		timeout = Integer.parseInt(pargs.getOption("-n", "1000"));
		silent = !"0".equals(pargs.getOption("-s", "0"));
		
		ThreadPoolExecutor exec = new ThreadPoolExecutor(numThreads + 1, numThreads + 1, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(1));
		exec.submit(new DummyInput());
		for (int i = 0; i < numThreads; i++) {
			exec.submit(new DummyTask(Integer.toString(i)));
		}
		exec.shutdown();
		exec.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
		if (!silent)
			System.out.println("DummyJob Done.");
	}

	public static void main(String[] args) throws Exception {
		//args = new String[]{ "-t", "1", "-s", "", "-n", "5000" };
		new DummyJob().doIt(args);
	}
}
