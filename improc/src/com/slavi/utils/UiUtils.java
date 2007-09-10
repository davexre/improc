package com.slavi.utils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ArmEvent;
import org.eclipse.swt.events.ArmListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.slavi.ui.TaskProgress;

public class UiUtils {
	static DirectoryDialog browseForFolderDialog = null;
	
	/**
	 * Opens the standart SWT dialog for the current OS for selecting a folder.
	 * <p>
	 * Returns the selected folder or null if the dialog is cancelled
	 */
	public static String browseForFolder(Shell parent, String message, String defaultDir) {
		if (browseForFolderDialog == null) {
			if (parent == null)
				parent = new Shell();
			browseForFolderDialog = new DirectoryDialog(parent);
		}
		browseForFolderDialog.setMessage(message);
		browseForFolderDialog.setFilterPath(defaultDir);
		browseForFolderDialog.setText("Some text");
		
		return browseForFolderDialog.open();
	}

	/**
	 * Returns the BOLD version of the specified font
	 */
	public static Font getBoldFont(Font font) {
		FontData fd[] = font.getFontData();
		if (fd.length > 0) {
			FontData lastfd = fd[fd.length - 1];
			lastfd.setStyle(SWT.BOLD);
		}
		return new Font(font.getDevice(), fd);
	}

	/**
	 * Centers the specified shell according to the primary display bounds.  
	 */
	public static void centerShell(Shell shell) {
		Rectangle displayRect = shell.getDisplay().getBounds();
		Rectangle shellRect = shell.getBounds();
		shellRect.x = (displayRect.width - shellRect.width) / 2;
		shellRect.y = (displayRect.height - shellRect.height) / 2;
		if (shellRect.x < 0)
			shellRect.x = 0;
		if (shellRect.y < 0)
			shellRect.y = 0;
		shell.setBounds(shellRect);
	}
	
	private static TaskProgress waitDialogTaskProgress = null; 

	private static Shell waitDialogShell = null;
	
	/**
	 * Updates the currently open wait dialog's status and progress bar value.
	 * <p>
	 * The method is thread safe. It is INTENDED to be called from the runnable
	 * as specified by {@link #openWaitDialog(String, Runnable, int)}.
	 * @param status The status message
	 * @param taskCompleted The progress bar value. 
	 * 		Should be 0 <= taskCompleted <= maxProgressValue (as specified by
	 *		{@link #openWaitDialog(String, Runnable, int)})
	 * @see #openWaitDialog(String, Runnable, int)
	 */
	public static void activeWaitDialogSetStatus(String status, int taskCompleted) {
		if (waitDialogTaskProgress != null)
			waitDialogTaskProgress.setStatusAndProgressThreadsafe(status, taskCompleted);
	}
	
	/**
	 * Opens a "Please wait..." dialog and starts the runnable thread.
	 * <p>
	 * The dialog is suspends the calling thread until the runnable is 
	 * finished or until the user presses the "Cancel" button or the runnable
	 * thread throws an expection.
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
	 * 		{@link org.eclipse.swt.SWT.INDETERMINATE}
	 * @return True on success, False is the operation is aborted or the 
	 * 		runnable thread throws an unhandled exception.
	 * 
	 * @see #activeWaitDialogSetStatus(String, int)
	 */
	public static boolean openWaitDialog(String title, Runnable runnable, int maxProgressValue) {
		if (waitDialogShell != null)
			return false;
		
		waitDialogShell = new Shell(SWT.MIN | SWT.MAX | SWT.RESIZE);
		waitDialogShell.setLayout(new FillLayout());
		waitDialogTaskProgress = new TaskProgress(waitDialogShell, 
				maxProgressValue < 1 ? SWT.INDETERMINATE : SWT.NONE, runnable);
		waitDialogTaskProgress.setProgressMaximum(maxProgressValue);
		waitDialogTaskProgress.addArmListener(new ArmListener() {
			public void widgetArmed(ArmEvent e) {
				waitDialogShell.close();
			}
		});
		waitDialogShell.pack();
		waitDialogShell.setSize(300, 100);
		centerShell(waitDialogShell);
		Display display = Display.getCurrent();
		waitDialogTaskProgress.setTitle(title);
		waitDialogTaskProgress.setProgressMaximum(maxProgressValue);
		waitDialogTaskProgress.startTask();
		waitDialogShell.open();
		while (!waitDialogShell.isDisposed()) {
			if (display.readAndDispatch()) {
				// Chesk once more if dialog is disposed as the dialog is disposed 
				// in a call to readAndDispatch() and for some reason the
				// readAndDispatch() returns a worng "true" value;
				if (!waitDialogShell.isDisposed()) 
					display.sleep();
			}
		}
		boolean result = !waitDialogTaskProgress.isAborted();
		waitDialogShell.dispose();
		waitDialogTaskProgress = null;
		waitDialogShell = null;
		return result;
	}
}
