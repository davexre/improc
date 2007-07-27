package com.slavi.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ArmListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.TypedListener;

import com.slavi.utils.UiUtils;

public class TaskProgress extends Composite {

	Label title;
	
	Label status;
	
	ProgressBar progressBar;
	
	NotifyingThread thread;
	
	Button btnAbort;

	AsyncRefresh asyncRefresh;
	
	Display display;
	
	public enum TaskStatus {
		 NOTSTARTED,
		 RUNNING,
		 ABORTING,
		 ABORTED,
		 FINISHED
	}
	
	TaskStatus taskStatus;
	
	public TaskProgress(Composite parent, int style, Runnable task) {
		super(parent, style);
		createWidgets();
		display = getDisplay();
		asyncRefresh = new AsyncRefresh();
		thread = new NotifyingThread(task);
		thread.setPriority(Thread.MIN_PRIORITY);
		taskStatus = TaskStatus.NOTSTARTED;
	}

	public void setTitle(String title) {
		this.title.setText(title);
	}
	
	public String getTitle() {
		return title.getText();
	}
	
	public void addArmListener(ArmListener listener) {
		checkWidget ();
		if (listener == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
		TypedListener typedListener = new TypedListener (listener);
		addListener(SWT.Arm, typedListener);
	}

	public TaskStatus getTaskStatus() {
		return taskStatus;
	}

	public void abortTask() {
		if (taskStatus == TaskStatus.NOTSTARTED) {
			setStatusThreadsafe("Aborted and not run");
			taskStatus = TaskStatus.ABORTED;
			btnAbort.setEnabled(false);
			notifyListeners(SWT.Arm, null); // Task will not run, so send a finished event
		} else if (taskStatus == TaskStatus.RUNNING) {
			setStatusThreadsafe("Aborting...");
			taskStatus = TaskStatus.ABORTING;
			thread.interrupt();
		}
	}
	
	public boolean isAborted() {
		return (taskStatus == TaskStatus.ABORTED) || (taskStatus == TaskStatus.ABORTING);
	}
	
	private void taskFinished() {
		status.setText(isAborted() ? "Aborted" : "Finished");
		btnAbort.setEnabled(false);
		notifyListeners(SWT.Arm, null);
	}
	
	public void startTask() {
		if (taskStatus == TaskStatus.NOTSTARTED) {
			taskStatus = TaskStatus.RUNNING;
			setStatusThreadsafe("Running...");
			thread.start();
		}
	}
	
	private class TaskFinishedNotification implements Runnable {
		public void run() {
			if (!isDisposed())
				taskFinished();
		}
	}
	
	private class NotifyingThread extends Thread {
		public NotifyingThread(Runnable task) {
			super(task);
		}

		public void run() {
			try {
				super.run();
			} catch (Throwable t) {
				taskStatus = TaskStatus.ABORTED;
				t.printStackTrace();
			} finally {
				if (taskStatus == TaskStatus.RUNNING)
					taskStatus = TaskStatus.FINISHED;
				display.asyncExec(new TaskFinishedNotification());
			}
		}
	}
	
	private String threadsafeStatus;
	
	private int threadsafeTaskCompleted;
		
	private class AsyncRefresh implements Runnable {
		public void run() {
			if (isDisposed())
				return;
			if (threadsafeStatus != null) {
				status.setText(threadsafeStatus);
				threadsafeStatus = null;
			}
			if (threadsafeTaskCompleted >= 0) {
				progressBar.setSelection(threadsafeTaskCompleted);
				threadsafeTaskCompleted = -1;
			}
		}
	}
	
	public void setStatusThreadsafe(String status) {
		setStatusAndProgressThreadsafe(threadsafeStatus = status, -1);
	}
		
	public void setProgressThreadsafe(int taskCompleted) {
		setStatusAndProgressThreadsafe(null, taskCompleted);
	}
	
	public void setStatusAndProgressThreadsafe(String status, int taskCompleted) {
		threadsafeStatus = status;
		threadsafeTaskCompleted = taskCompleted;
		if (!isDisposed())
			getDisplay().asyncExec(asyncRefresh);
	}
	
	public void setProgressMaximum(int maxValue) {
		if (maxValue > 0)
			progressBar.setMaximum(maxValue);
	}
	
	public int getProgressMaximum() {
		return progressBar.getMaximum();
	}
	
	private void createWidgets() {
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		setLayout(layout);
				
		title = new Label(this, SWT.LEFT | SWT.WRAP);
		GridData layoutData = new GridData();
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.grabExcessVerticalSpace = true;
		layoutData.horizontalAlignment = GridData.FILL;
		layoutData.horizontalSpan = 2;
		title.setLayoutData(layoutData);
		title.setFont(UiUtils.getBoldFont(title.getFont()));
		
		progressBar = new ProgressBar(this, 
				SWT.HORIZONTAL | (getStyle() & SWT.INDETERMINATE));
		progressBar.setMinimum(0);
		progressBar.setMaximum(100);
		progressBar.setSelection(0);
		layoutData = new GridData();
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.horizontalAlignment = GridData.FILL;
		progressBar.setLayoutData(layoutData);

		btnAbort = new Button(this, SWT.PUSH);
		btnAbort.setText("Abort");
		btnAbort.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				abortTask();
			}
		});
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				abortTask();
			}
		});

		status = new Label(this, SWT.LEFT | SWT.WRAP);
		layoutData = new GridData();
		status.setText("Not started");
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.grabExcessVerticalSpace = true;
		layoutData.horizontalAlignment = GridData.FILL;
		layoutData.horizontalSpan = 2;
		status.setLayoutData(layoutData);
	}
}
