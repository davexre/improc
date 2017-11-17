package example.slavi.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.slavi.util.Marker;
import com.slavi.util.Util;
import com.slavi.util.concurrent.TaskSet;


public class HyperThreadingExample {
	static long doIt(int numThreads) throws Exception {
		ExecutorService exec = Executors.newFixedThreadPool(numThreads);
		TaskSet ts = new TaskSet(exec);
		System.out.println("Creating tasks");
		Marker.mark();
		for (int i = 0; i < numThreads; i++) {
			ts.add(new ExampleTask(Integer.toString(i)));
		}
		System.out.println("Waiting for tasks to finish");
		ts.run().get();
		System.out.println("Parallel job finished");
		Marker.State stamp = Marker.release();
		
		exec.shutdown();
		return (stamp.end - stamp.start) / numThreads;
	}	
	
	public static void main(String[] args) throws Exception {
		int maxTasks = 10;
		long results[] = new long[maxTasks];
		for (int i = 0; i < maxTasks; i++) {
			results[i] = doIt(i + 1);
		}
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println("Tasks\tRatio (seconds per task)");
		for (int i = 0; i < maxTasks; i++) {
			System.out.println((i+1) + "\t" + Util.getFormatedMilliseconds(results[i]));
		}
		System.out.println("Done.");
	}	
}
