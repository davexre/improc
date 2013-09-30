package com.slavi.util.ui;

import org.eclipse.swt.SWT;
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


public class TaskProgress extends Composite {

	/**
	 * The Task Finished event type (value is 1001).
	 * 
	 * This event can be handled by invoking:
	 * <code>
	 * taskProgress.addListener(TaskProgress.TaskFinished, new Listener() {
	 *     public void handleEvent(Event event) {
	 *         ...
	 *     }
	 * });
	 * </code>
	 * @see org.eclipse.swt.widgets.Widget#addListener
	 * @see org.eclipse.swt.widgets.Display#addFilter
	 * @see org.eclipse.swt.widgets.Event
	 */
	public static int TaskFinished = 1001;
	
	public enum TaskState {
		NOTSTARTED,
		RUNNING,
		ABORTING,
		ABORTED,
		FINISHED
	}
	
	Label title;
	
	Label taskStatusText;
	
	ProgressBar progressBar;
	
	NotifyingThread thread;
	
	Runnable task;
	
	Button btnAbort;

	AsyncRefresh asyncRefresh;
	
	volatile TaskState taskState;
	
	volatile int progressBarMaximum;

	private class AsyncRefresh implements Runnable {
		private String newTaskStatusText;
		private boolean isNewTaskStatusTextSet = false;
		
		private int newTaskCompleted;
		private boolean isNewTaskCompletedSet = false;
		
		private boolean isRefreshScheduled = false;

		private synchronized void scheduleRefresh() {
			if ((!isDisposed()) && (!isRefreshScheduled)) {
				isRefreshScheduled = true;
				getDisplay().asyncExec(asyncRefresh);
			}
		}
		
		public synchronized void setStatusAndProgress(String newStatus, int taskCompleted) {
			if (newStatus != null) {
				this.newTaskStatusText = newStatus;
				this.isNewTaskStatusTextSet = true;
			}
			if (taskCompleted >= 0) {
				this.newTaskCompleted = taskCompleted;
				this.isNewTaskCompletedSet = true;
			}
			scheduleRefresh();
		}		
		
		public synchronized void run() {
			isRefreshScheduled = false;
			if (isDisposed())
				return;
			if (isNewTaskStatusTextSet) {
				taskStatusText.setText(newTaskStatusText);
				newTaskStatusText = null;
				isNewTaskStatusTextSet = false;
			}
			if (isNewTaskCompletedSet) {
				progressBar.setSelection(newTaskCompleted);
				newTaskCompleted = -1;
				isNewTaskCompletedSet = false;
			}
		}
	}
	
	public TaskProgress(Composite parent, int style, Runnable task) {
		super(parent, style);
		createWidgets();
		asyncRefresh = new AsyncRefresh();
		thread = null;
		this.task = task;
		taskState = TaskState.NOTSTARTED;
	}

	/**
	 * Sets the text description of the task, thread UNsafe.
	 */
	public void setTitle(String title) {
		this.title.setText(title);
	}

	/**
	 * Returns the text description of the task, thread UNsafe.
	 */
	public String getTitle() {
		return title.getText();
	}
	
	/**
	 * Returns the execution state of the task, thread SAFE. 
	 */
	public TaskState getTaskState() {
		return taskState;
	}

	/**
	 * Aborts the execution of the task, thread SAFE.
	 * 
	 * If the task has not yet started, then the task state
	 * will be {@link TaskState#ABORTED}, the task will not 
	 * be started and the event {@link #TaskFinished} will be fired.
	 * <p>
	 * If the task is running, then the task state will be
	 * {@link TaskState#ABORTING} and the task's thread Thread.interrupt()
	 * will be invoked. The event {@link #TaskFinished} will be fired
	 * when the task truly finished/aborts/throws an exception.
	 */
	public synchronized void abortTask() {
		if (taskState == TaskState.NOTSTARTED) {
			asyncRefresh.setStatusAndProgress("Aborted and not run", -1);
			taskState = TaskState.ABORTED;
			btnAbort.setEnabled(false);
			Display display = getDisplay();
			if ((display != null) && (!display.isDisposed())) {
				display.asyncExec(new Runnable() {
					public void run() {
						// Task will not run, so send a finished event
						notifyListeners(TaskFinished, null); 
					}
				});
			}
		} else if (taskState == TaskState.RUNNING) {
			asyncRefresh.setStatusAndProgress("Aborting...", -1);
			taskState = TaskState.ABORTING;
			if (thread != null)
				thread.interrupt();
		}
	}

	/**
	 * Start the task in a new thread, thread SAFE.
	 * 
	 * The task will be started if not meanwhile aborted, i.e.
	 * the {@link #abortTask()} is not invoked. If the task has
	 * already been started, or has already completed this method
	 * will do nothing and just return.
	 */
	public synchronized void startTask() {
		if (taskState == TaskState.NOTSTARTED) {
			thread = new NotifyingThread(task);
			thread.setPriority(Thread.MIN_PRIORITY);
			asyncRefresh.setStatusAndProgress("Running...", -1);
			taskState = TaskState.RUNNING;
			thread.start();
		}
	}
	
	/**
	 * Returns the task execution state, thread SAFE.
	 * 
	 * @return True if the task execution state is
	 * 		{@link TaskState#ABORTED} or {@link TaskState#ABORTING}. 
	 */
	public boolean isAborted() {
		return (taskState == TaskState.ABORTED) || (taskState == TaskState.ABORTING);
	}

	class TaskFinishedNotification implements Runnable {
		public void run() {
			if (!isDisposed()) {
				taskStatusText.setText(isAborted() ? "Aborted" : "Finished");
				btnAbort.setEnabled(false);
				notifyListeners(TaskFinished, null);
			}
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
				taskState = TaskState.ABORTED;
				t.printStackTrace();
			} finally {
				if (taskState == TaskState.RUNNING)
					taskState = TaskState.FINISHED;
				Display display = getDisplay();
				if ((display != null) && (!display.isDisposed()))
					display.asyncExec(new TaskFinishedNotification());
			}
		}
	}
	
	/**
	 * Sets the status bar of the task progress component, thread SAFE.
	 * 
	 * This method could be invoked from the task's methods to set the
	 * status bar of the component.
	 *   
 	 * @see #setStatusAndProgressThreadsafe(String, int)
 	 */
	public void setStatusThreadsafe(String status) {
		if (taskState == TaskState.RUNNING) {
			asyncRefresh.setStatusAndProgress(status, -1);
		}
	}
	
	/**
	 * Sets the progress bar of the task progress component, thread SAFE.
	 * 
	 * This method could be invoked from the task's methods to set the
	 * progress bar of the component. 
	 * 
	 * @see #setStatusAndProgressThreadsafe(String, int)
	 */
	public void setProgressThreadsafe(int taskCompleted) {
		if (taskState == TaskState.RUNNING) {
			asyncRefresh.setStatusAndProgress(null, taskCompleted);
		}
	}
	
	public void progress(int amount) {
		if (taskState == TaskState.RUNNING) {
			int cur = progressBar.getSelection();
			asyncRefresh.setStatusAndProgress(null, cur + amount);
		}
	}

	/**
	 * Sets both the status and the progress bar of the task progress 
	 * component, thread SAFE.
	 *
	 * @see #setStatusThreadsafe(String)
	 * @see #setProgressThreadsafe(int)
	 */
	public void setStatusAndProgressThreadsafe(String status, int taskCompleted) {
		if (taskState == TaskState.RUNNING) {
			asyncRefresh.setStatusAndProgress(status, taskCompleted);
		}
	}
	
	/**
	 * Sets the maximum value of the component's progress bar, thread UNsafe.
	 * 
	 * The value must be greater than 0. The default value is 100. The minimum
	 * progress bar value is 0.
	 * 
	 * @see #setProgressThreadsafe(int)
	 */
	public void setProgressMaximum(int maxValue) {
		if (maxValue > 0) {
			progressBarMaximum = maxValue;
			progressBar.setMaximum(progressBarMaximum);
		}
	}
	
	/**
	 * Returns the maximum value of the component's progress bar, thread SAFE.
	 */
	public int getProgressMaximum() {
		return progressBarMaximum;
	}
	
	private void createWidgets() {
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		setLayout(layout);
				
		title = new Label(this, SWT.LEFT | SWT.WRAP);
		GridData layoutData = new GridData();
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.grabExcessVerticalSpace = true;
		layoutData.horizontalAlignment = SWT.FILL;
		layoutData.horizontalSpan = 2;
		title.setLayoutData(layoutData);
		title.setFont(SwtUtil.getBoldFont(title.getFont()));
		
		progressBar = new ProgressBar(this, 
				SWT.HORIZONTAL | (getStyle() & SWT.INDETERMINATE));
		progressBar.setMinimum(0);
		progressBarMaximum = 100;
		progressBar.setMaximum(progressBarMaximum);
		progressBar.setSelection(0);
		layoutData = new GridData();
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.horizontalAlignment = SWT.FILL;
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

		taskStatusText = new Label(this, SWT.LEFT | SWT.WRAP);
		layoutData = new GridData();
		taskStatusText.setText("Not started");
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.grabExcessVerticalSpace = true;
		layoutData.horizontalAlignment = SWT.FILL;
		layoutData.horizontalSpan = 2;
		taskStatusText.setLayoutData(layoutData);
	}
}
