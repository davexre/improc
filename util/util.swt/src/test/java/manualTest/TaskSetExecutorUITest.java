package manualTest;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.slavi.util.Util;
import com.slavi.util.concurrent.TaskSet;
import com.slavi.util.swt.SwtUtil;

public class TaskSetExecutorUITest {
	static class MyJob implements Callable {
		ExecutorService exec;
		int taskCount;
		
		MyJob(ExecutorService exec, int taskCount) {
			this.exec = exec;
			this.taskCount = taskCount;
		}

		public Object call() throws Exception {
			TaskSet ts = new TaskSet(exec) {
				public void onError(Object task, Throwable e) throws Exception {
					ExampleTask t = (ExampleTask) task;
					System.out.println("onERROR in task " + t.taskName + " -> " + Util.exceptionToString(e));
				}
				
				public void onTaskFinished(Object task) throws Exception {
					SwtUtil.activeWaitDialogSetStatus("Done " + 
							Integer.toString(getFinishedTasksCount()) + "/" + Integer.toString(getTasksCount()), 
							getFinishedTasksCount());
				}
			};
			System.out.println("Creating tasks");
			for (int i = 1; i <= taskCount; i++) {
				ExampleTask task = new ExampleTask(Integer.toString(i));
				System.out.println("Created task " + i);
				ts.add(task);
				System.out.println("Submitted task " + i);
			}
			CompletableFuture f = ts.run();
			f.thenRun(() -> {
				System.out.println("TaskSet is done.");
			});
			
			System.out.println("Waiting for tasks to finish");
			try {
				f.get();
			} catch (Throwable t) {
				t.printStackTrace();
				f.cancel(true);
			}
			System.out.println("Parallel job finished");
			return null;
		}
	}
	
	public static void main(String[] args) throws Exception {
//		ExecutorService exec = Util.newBlockingThreadPoolExecutor(2);
		ExecutorService exec = new ThreadPoolExecutor(2, 2, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(100));
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
