package com.test.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.slavi.util.swt.SwtUtil;
import com.slavi.util.swt.TaskProgress;

public class TestTaskProgress {

	Shell shell;
	TaskProgress tp;
	boolean dummy;
	
	public TestTaskProgress() {
		shell = new Shell(SWT.SHELL_TRIM);
		shell.setLayout(new GridLayout(1, false));
		Runnable task = new dummyJob();
		tp = new TaskProgress(shell, SWT.INDETERMINATE, task);
		tp.setTitle("Title");
		tp.setProgressMaximum(8 - 1);

		Button btnStart = new Button(shell, SWT.PUSH);
		btnStart.setText("Start");
		btnStart.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				tp.startTask();
			}
		});

		Button btnTest = new Button(shell, SWT.PUSH);
		btnTest.setText("btnTest");
		btnTest.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//tp.infiniteBar.setVisible(dummy);
				dummy = !dummy;
			}
		});
		shell.pack();
	}
	
	class dummyJob implements Runnable {
		public void run() {
			try {
				int maxIter = 8;
				for (int i = 0; i < maxIter; i++) {
					tp.setStatusAndProgressThreadsafe("thread step " + i, i);
					Thread.sleep(1000);
				}
			} catch (InterruptedException e) {
				System.out.println("Thread interrupted");
			}
		}		
	}
	
	public static void test1() {
		TestTaskProgress test = new TestTaskProgress();
		test.shell.open();
		while( !test.shell.isDisposed()  ){
			if(!test.shell.getDisplay().readAndDispatch())
				test.shell.getDisplay().sleep();
		}
	}
	
	static class dummyJob2 implements Runnable {
		private int iterations;
		
		public dummyJob2(int iterations) {
			this.iterations = iterations;
		}
		
		public void run() {
			try {
				for (int i = 0; i < iterations; i++) {
					SwtUtil.activeWaitDialogSetStatus("thread step " + i, i);
					Thread.sleep(1000);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}		
	}

	public static void test2() {
		dummyJob2 job = new dummyJob2(8);
		boolean result = SwtUtil.openWaitDialog(null, "Title", job, 8);
		System.out.println(result);
	}
	
	static class dummyJob3 implements Runnable {
		private int iterations;
		
		public dummyJob3(int iterations) {
			this.iterations = iterations;
		}
		
		public void run() {
			try {
				for (int i = 0; i < iterations; i++) {
					SwtUtil.activeWaitDialogSetStatus("thread step " + i, i);
					Thread.sleep(1000);
					if (i == iterations / 2) {
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								SwtUtil.msgbox(null, "asdasdasd");	
							}
						});
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void test3() {
		dummyJob3 job = new dummyJob3(8);
		boolean result = SwtUtil.openWaitDialog(null, "Base dialog", job, 8);
		System.out.println(result);
	}
	
	static class JobThrowingException implements Runnable {
		public void run() {
			SwtUtil.activeWaitDialogSetStatus("Before exception", 5);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			throw new RuntimeException("Dummy exception");
		}
	}

	public static void testJobThrowingException() {
		JobThrowingException t = new JobThrowingException();
		System.out.println("Before starting job");
		boolean result = SwtUtil.openWaitDialog(null, "Testing", t, 10);
		System.out.println(result);
	}
	
	public static void main(String[] args) {
//		test1();
		test2();
//		test3();
//		testJobThrowingException();
	}
	
}
