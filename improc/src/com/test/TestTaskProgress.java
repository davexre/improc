package com.test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;

import com.slavi.ui.TaskProgress;
import com.slavi.utils.SwtUtl;

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
					SwtUtl.activeWaitDialogSetStatus("thread step " + i, i);
					Thread.sleep(1000);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}		
	}

	public static void test2() {
		dummyJob2 job = new dummyJob2(8);
		boolean result = SwtUtl.openWaitDialog("Title", job, 8);
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
					SwtUtl.activeWaitDialogSetStatus("thread step " + i, i);
					Thread.sleep(1000);
					if (i == iterations / 2) {
						SwtUtl.getActiveWaitDialogShell().getDisplay().asyncExec(new Runnable() {
							public void run() {
								SwtUtl.msgbox(null, "asdasdasd");	
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
		boolean result = SwtUtl.openWaitDialog("Base dialog", job, 8);
		System.out.println(result);
	}
	
	public static void main(String[] args) {
		test3();
	}
	
}
