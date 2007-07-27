package com.test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;

import com.slavi.ui.TaskProgress;
import com.slavi.utils.UiUtils;

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
	
	public static void main1(String[] args) {
		TestTaskProgress test = new TestTaskProgress();
		test.shell.open();
		while( !test.shell.isDisposed()  ){
			if(!test.shell.getDisplay().readAndDispatch())
				test.shell.getDisplay().sleep();
		}
	}
	
	static class dummyJob2 implements Runnable {
		public void run() {
			try {
				for (int i = 0; i < 8; i++) {
					UiUtils.activeWaitDialogSetStatus("thread step " + i, i);
					Thread.sleep(1000);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}		
	}

	public static void main(String[] args) {
		dummyJob2 job = new dummyJob2();
		boolean result = UiUtils.openWaitDialog("Title", job, -1);
		System.out.println(result);
	}
	
}
