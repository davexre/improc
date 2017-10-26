package com.slavi.util.swt;

import java.util.concurrent.Callable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;


public class WaitDialog {

	public WaitDialog() {
		
	}
	
	TaskProgress taskProgress = null; 

	Shell shell = null;
	
	/**
	 * Updates the currently open wait dialog's status and progress bar value.
	 * <p>
	 * The method is thread safe. It is INTENDED to be called from the runnable
	 * as specified by {@link #openWaitDialog(String, Runnable, int)}.
	 * @param status The status message. If message = null the message will 
	 * 		not be modified.
	 * @param taskCompleted The progress bar value. 
	 * 		Should be 0 <= taskCompleted <= maxProgressValue (as specified by
	 *		{@link #openWaitDialog(String, Runnable, int)}).
	 *		If the taskCompleted < 0 then it will not be set
	 * @see #openWaitDialog(String, Runnable, int)
	 */
	public void setStatus(final String status, final int taskCompleted) {
		taskProgress.setStatusAndProgressThreadsafe(status, taskCompleted);
	}

	public void progress(int amount) {
		taskProgress.progress(amount);
	}
	
	public void abortTask() {
		if (taskProgress != null)
			taskProgress.abortTask();
	}
	
	/**
	 * Opens a "Please wait..." dialog and starts the runnable thread.
	 * <p>
	 * The dialog is suspends the calling thread until the runnable is 
	 * finished or until the user presses the "Cancel" button or the runnable
	 * thread throws an exception.
	 * 
	 * @param title The title of the task
	 * @param runnable The runnable instance that will be started when the 
	 * 		dialog is opened and will be waited for to finish.
	 * @param maxProgressValue The maximum value for the progress bar. The 
	 * 		current progress value of the progress bar can be updated from
	 * 		the runnable thread using the method 
	 * 		{@link #activeWaitDialogSetStatus(String, int)}. The minimum 
	 * 		progress bar value is always 0. If the maxProgressValue is 
	 * 		negative (maxProgressValue < 0) the progress bar is set as
	 * 		org.eclipse.swt.SWT.INDETERMINATE
	 * @return True on success, False is the operation is aborted or the 
	 * 		runnable thread throws an unhandled exception.
	 * 
	 * @see #activeWaitDialogSetStatus(String, int)
	 */
	public boolean open(final Shell parent, final String title, final Runnable runnable, final int maxProgressValue) {
		shell = SwtUtil.makeShell(parent, SWT.MIN | SWT.MAX | SWT.RESIZE);
		shell.setLayout(new FillLayout());
		taskProgress = new TaskProgress(shell, 
				maxProgressValue < 1 ? SWT.INDETERMINATE : SWT.NONE, runnable);
		taskProgress.setProgressMaximum(maxProgressValue);
		taskProgress.addListener(TaskProgress.TaskFinished, new Listener() {
			public void handleEvent(Event event) {
				shell.close();
			}
		});
		taskProgress.setTitle(title);
		taskProgress.setProgressMaximum(maxProgressValue);
		shell.addListener(SWT.Traverse, new Listener () {
			public void handleEvent (Event event) {
				switch (event.detail) {
					case SWT.TRAVERSE_ESCAPE:
						taskProgress.abortTask();
						event.detail = SWT.TRAVERSE_NONE;
						event.doit = false;
						break;
				}
			}
		});

		shell.pack();
		// shell.setSize(300, 100); // This is no longer necessary and was commented out because caused poorly sized boxes. 
		SwtUtil.centerShell(shell);
		shell.open();
		
		taskProgress.startTask();

		Display display = Display.getCurrent();
		while (!shell.isDisposed()) {
			if (display.readAndDispatch()) {
				// Check once more if dialog is disposed as the dialog is disposed 
				// in a call to readAndDispatch() and for some reason the
				// readAndDispatch() returns a wrong "true" value;
				if (!shell.isDisposed()) 
					display.sleep();
			}
		}
		boolean result = !taskProgress.isAborted();
		shell.dispose();
		return result;
	}
	
	private class CallableWrapper<V> implements Runnable {
		final Callable<V> callable;
		V result;
		Exception exception;
		
		public CallableWrapper(Callable<V> callable) {
			this.callable = callable;
		}
		
		public void run() {
			try {
				result = callable.call();
			} catch (Exception e) {
				exception = e;
				abortTask();
			}
		}
	}
	
	public <V> V open(final Shell parent, final String title, final Callable<V> callable, final int maxProgressValue) throws Exception {
		CallableWrapper<V> wrapper = new CallableWrapper<V>(callable);
		boolean res = open(parent, title, wrapper, maxProgressValue);
		if (res && (wrapper.exception == null)) {
			return wrapper.result;
		}
		if (wrapper.exception != null)
			throw wrapper.exception;
		throw new InterruptedException("Aborted");
	}
}
