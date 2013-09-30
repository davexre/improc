package example.slavi.concurrent;

public class ExampleTask implements Runnable {
	public final String taskName;
	
	public ExampleTask(String taskName) {
		this.taskName = taskName;
	}
	
	public void run() {
		System.out.println("Task " + taskName + " started.");
		long started = System.currentTimeMillis();
		while (System.currentTimeMillis() - started < 1000) {
			if (Thread.currentThread().isInterrupted()) {
				String msg = "Task " + taskName + " aborted";
				System.out.println(msg);
				throw new RuntimeException(msg);
			}
		}
		System.out.println("Task " + taskName + " finished.");
	}
}