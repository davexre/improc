package com.test.ui;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import com.slavi.util.Util;
import com.slavi.util.concurrent.TaskSet;
import com.slavi.util.swt.SwtUtil;

public class TestWaitDialog {

	AtomicInteger counter = new AtomicInteger();
	
	public class ProcessOne implements Runnable {
		
		int id;
		
		public ProcessOne(int id) {
			this.id = id;
		}
		
		public void run() {
			try {
				for (int i = 0; i < 10; i++) {
					if (Thread.currentThread().isInterrupted())
						throw new InterruptedException();
					int count = counter.incrementAndGet();
					SwtUtil.activeWaitDialogSetStatus("Completed " + count, -1);
					System.out.println("Worker " + id + " at iteration " + i);
					Thread.sleep(100);
				}
				System.out.println("Worker " + id + " finished");
			} catch (Exception e) {
				throw new CompletionException(e);
			}
		}
	}
	
	public class ProcessAll implements Callable<Void> {
		public Void call() throws Exception {
			ExecutorService exec = Util.newBlockingThreadPoolExecutor(30);
//			ExecutorService exec = new FakeThreadExecutor();
			TaskSet taskSet = new TaskSet(exec);
			try {
				for (int i = 0; i < 10; i++) {
					System.out.println("Adding task " + i); 
					taskSet.add(new ProcessOne(i));
				}
				System.out.println("All tasks added");
				Thread thread = new Thread(new Runnable() {
					public void run() {
						try {
							Thread.sleep(1000);
							System.out.println("calling  SwtUtil.activeWaitDialogAbortTask()");
							SwtUtil.activeWaitDialogAbortTask();
							System.out.println("returned SwtUtil.activeWaitDialogAbortTask()");
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
				System.out.println("Abort thread start");
				thread.start();
				System.out.println("Abort thread started");
				System.out.println("Calling taskSet.get()");
				taskSet.run().get();
				System.out.println("Returned from taskSet.get()");
			} finally {
				exec.shutdown();
				System.out.println("Exec shutdown");
			}
			return null;
		}		
	}
	
	private void doIt() throws Exception {
		Shell parent;
		parent = new Shell((Shell) null, SWT.NONE);
		parent.setBounds(-10, -10, 1, 1); // fixes a bug in SWT
		parent.open();

		System.out.println("Start.");
//		SwtUtil.openTaskManager(parent, true);
		try {
			SwtUtil.openWaitDialog(parent, "Dummy job", new ProcessAll(), -1);
		} finally {
			parent.close();
//			SwtUtil.closeTaskManager();
		}
		System.out.println("Done.");
	}

	public static void main(String[] args) throws Exception {
		new TestWaitDialog().doIt();
	}
}
