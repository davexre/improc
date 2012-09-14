package manualTest.slavi.ui;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import com.slavi.util.Util;
import com.slavi.util.concurrent.TaskSetExecutor;
import com.slavi.util.ui.SwtUtil;

import example.slavi.concurrent.ExampleTask;

public class TaskSetExecutorUITest {
	static class MyJob implements Callable<Void> {
		ExecutorService exec;
		int taskCount;
		
		MyJob(ExecutorService exec, int taskCount) {
			this.exec = exec;
			this.taskCount = taskCount;
		}
		
		public Void call() throws Exception {
			TaskSetExecutor ts = new TaskSetExecutor(exec) {
				public void onError(Object task, Throwable e) throws Exception {
					ExampleTask t = (ExampleTask) task;
					System.out.println("ERROR in task " + t.taskName + " -> " + e.getMessage());
				}
				
				public void onTaskFinished(Object task, Object result) throws Exception {
					SwtUtil.activeWaitDialogSetStatus("Done " + 
							Integer.toString(getFinishedTasksCount()) + "/" + Integer.toString(getTasksCount()), 
							getFinishedTasksCount());
				}
				
				public void onFinally() throws Exception {
					System.out.println("TaskSet is done.");
				}
			};
			System.out.println("Creating tasks");
			for (int i = 1; i <= taskCount; i++) {
				ExampleTask task = new ExampleTask(Integer.toString(i));
				System.out.println("Created task " + i);
				ts.add(task);
				System.out.println("Submitted task " + i);
			}
			ts.addFinished();
			System.out.println("Waiting for tasks to finish");
			ts.get();
			System.out.println("Parallel job finished");
			return null;
		}
	}
	
	public static void main(String[] args) throws Exception {
		ExecutorService exec = Util.newBlockingThreadPoolExecutor(2);
//		ExecutorService exec = new FakeThreadExecutor();
		int maxTasks = 10;
		try {
			SwtUtil.openTaskManager(null, true);
			SwtUtil.openWaitDialog(null, "Test", new MyJob(exec, maxTasks), maxTasks);
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			exec.shutdown();
		}
		SwtUtil.closeTaskManager();
		System.out.println("Done.");
	}
}
