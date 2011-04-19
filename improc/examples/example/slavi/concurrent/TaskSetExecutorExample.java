package example.slavi.concurrent;

import java.util.concurrent.ExecutorService;

import com.slavi.util.Util;
import com.slavi.util.concurrent.TaskSetExecutor;

public class TaskSetExecutorExample {
	public static void main(String[] args) throws Exception {
		ExecutorService exec = Util.newBlockingThreadPoolExecutor(2);

		System.out.println("Creating tasks");
		TaskSetExecutor ts = new TaskSetExecutor(exec);
		for (int i = 0; i < 10; i++) {
			ExampleTask task = new ExampleTask(Integer.toString(i));
			System.out.println("Created task " + i);
			ts.add(task);
			System.out.println("Submitted task " + i);
		}
		ts.addFinished();
		System.out.println("Waiting for tasks to finish");
		ts.get();
		System.out.println("Parallel job finished");

		exec.shutdown();
		System.out.println("Done.");
	}
}
