package com.slavi.util.ui;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.concurrent.Callable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import com.slavi.ui.SystemMonitor;
import com.slavi.ui.TaskProgress;

/**
 * This class contains utility methods for creating user interface using
 * the SWT (Standard Widget Toolkit) library.   
 */
public class SwtUtil {
	
	public static Shell makeShell(Shell parent, int style) {
		return parent == null ? new Shell(Display.getDefault(), style) : new Shell(parent, style);
	}

	public static Shell makeShell(Shell parent) {
		return makeShell(parent, SWT.NONE);
	}	
	
	/**
	 * Opens the standard SWT dialog for the current OS for selecting a folder.
	 * <p>
	 * Returns the selected folder or null if the dialog is cancelled.
	 * @param parent		The owner shell. Can be null.
	 */
	public static String browseForFolder(Shell parent, String message, String defaultDir) {
		Shell shell = makeShell(parent);
		DirectoryDialog dialog = new DirectoryDialog(shell);
		dialog.setText("");
		dialog.setMessage(message);
		dialog.setFilterPath(defaultDir);
		String result = dialog.open();
		shell.dispose();
		return result;
	}

	/**
	 * The default file extension filter for the openFile dialog.
	 */
	public static final String[][] defaultFilter = new String[][] {
		{"All files"}, 
		{"*.*"}
	};
	
	/**
	 * A file extension filter for the openFile dialog, enumerating all
	 * image file formats, supported by ImageIO.
	 */
	public static final String[][] imageFileFilter = makeFileFilter("Images (png,jpg,bmp,gif)", "*.png; *.jpg; *.bmp; *.gif");

	public static String[][] makeFileFilter(String displayName, String fileMask) {
		return new String[][] {
				{ displayName, "All files" },
				{ fileMask, "*.*" }
		};
	}
	
	/**
	 * Opens the standard SWT dialog for the current OS for selecting a file to open.
	 * <p>
	 * Returns the selected file or null if the dialog is canceled.
	 * @param parent		The owner shell. Can be null.
	 * @param title			The caption of the dialog. If this parameter 
	 * 						is null then the caption is set to "Open file".
	 * @param defaultDir	The default directory for the open file 
	 * 						dialog. This parameter can be null. 
	 * @param filter		The file extension filters for the dialog.
	 * 						This parameter can be null. See {@link #defaultFilter} 
	 */
	public static String openFile(Shell parent, String title, String defaultDir, String[][] filter) {
		Shell shell = makeShell(parent);
		FileDialog dialog = new FileDialog(shell, SWT.OPEN);
		if (filter == null) {
			dialog.setFilterNames(defaultFilter[0]);
			dialog.setFilterExtensions(defaultFilter[1]);
		} else {
			dialog.setFilterNames(filter[0]);
			dialog.setFilterExtensions(filter[1]);
		}
		if (defaultDir != null)
			dialog.setFilterPath(defaultDir);
		if (title == null) 
			dialog.setText("Open file");
		else
			dialog.setText(title);
		
		String result = dialog.open();
		shell.dispose();
		return result;
	}

	/**
	 * Opens the standard SWT dialog for the current OS for selecting a file to save.
	 * <p>
	 * Returns the selected file or null if the dialog is canceled.
	 * @param parent		The owner shell. Can be null.
	 * @param title			The caption of the dialog. If this parameter 
	 * 						is null then the caption is set to "Save as".
	 * @param defaultDir	The default directory for the open file 
	 * 						dialog. This parameter can be null. 
	 * @param filter		The file extension filters for the dialog.
	 * 						This parameter can be null. See {@link #defaultFilter} 
	 */
	public static String saveFile(Shell parent, String title, String defaultDir, String defaultFileName, String[][] filter) {
		Shell shell = makeShell(parent);
		FileDialog dialog = new FileDialog(shell, SWT.SAVE);
		if (filter == null) {
			dialog.setFilterNames(defaultFilter[0]);
			dialog.setFilterExtensions(defaultFilter[1]);
		} else {
			dialog.setFilterNames(filter[0]);
			dialog.setFilterExtensions(filter[1]);
		}
		if (defaultDir != null)
			dialog.setFilterPath(defaultDir);
		if (defaultFileName != null)
			dialog.setFileName(defaultFileName);
		else
			dialog.setFileName(null);
		if (title == null) 
			dialog.setText("Save as");
		else
			dialog.setText(title);
		
		String result = dialog.open();
		shell.dispose();
		return result;
	}

	/**
	 * Show an HTML document in a resizable modal dialog box. The dialog box has a "save" button.  
	 */
	public static void showHTML(final Shell parent, final String message, final String html) {
		final Shell shell = SwtUtil.makeShell(parent, SWT.DIALOG_TRIM);
		shell.setText(message);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		shell.setLayout(layout);
		shell.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_ESCAPE) {
					shell.close();
				}
			}
		});
		
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayout(new RowLayout(SWT.HORIZONTAL));
		
		Button closeBtn = new Button(composite, SWT.PUSH);
		closeBtn.setText("C&lose");
		closeBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				shell.close();
			}
		});
		shell.setDefaultButton(closeBtn);
		
		Button saveBtn = new Button(composite, SWT.PUSH);
		saveBtn.setText("&Save");
		saveBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String fouName = SwtUtil.saveFile(shell, "Save as", "", "", SwtUtil.makeFileFilter("HTML files", "*.html"));
				if (fouName == null)
					return;
				PrintWriter fou = null;
				try {
					fou = new PrintWriter(fouName);
					fou.print(html);
				} catch (FileNotFoundException ee) {
				} finally {
					try {
						if (fou != null && fou.checkError()) {
							SwtUtil.msgboxError(shell, "Save failed");
						}
						if (fou != null) {
							fou.close();
						}
					} catch (Exception ex) {
					}
				}
			}
		});

		try {
			Browser browser = new Browser(shell, SWT.NONE);
			browser.setText(html);
			browser.setSize(600, 450);
			GridData layoutData = new GridData();
			layoutData.horizontalAlignment = SWT.FILL;
			layoutData.verticalAlignment = SWT.FILL;
			layoutData.grabExcessHorizontalSpace = true;
			layoutData.grabExcessVerticalSpace = true;
			browser.setLayoutData(layoutData);
		} catch (SWTError e) {
			System.out.println("Could not instantiate Browser: " + e.getMessage());
			return;
		}
		shell.pack();
		shell.open();
		SwtUtil.centerShell(shell);
		Display display = shell.getDisplay();
		if (display == null)
			display = Display.getDefault();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		shell.dispose();
	}
		
	/**
	 * Displays the standard error dialog box with the specified message.
	 * @param parent		The owner shell. Can be null.
	 */
	public static void msgboxError(final Shell parent, final String errorMsg) {
		Shell shell = makeShell(parent);
		MessageBox dialog = new MessageBox(shell, SWT.ICON_ERROR);
		dialog.setText("Error");
		dialog.setMessage(errorMsg);
		dialog.open();
		shell.dispose();
	}
	
	public static void msgbox(final Shell parent, final String message) {
		msgbox(parent, "", message);
	}
	
	/**
	 * Displays the standard information dialog box with the specified message.
	 * @param parent		The owner shell. Can be null.
	 */
	public static void msgbox(final Shell parent, final String title, final String message) {
		Shell shell = makeShell(parent);
		MessageBox dialog = new MessageBox(shell, SWT.ICON_INFORMATION);
		dialog.setText(title);
		dialog.setMessage(message);
		dialog.open();
		shell.dispose();
	}
	
	/**
	 * Returns the BOLD version of the specified font
	 */
	public static Font getBoldFont(final Font font) {
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
	public static void centerShell(final Shell shell) {
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
	public static void activeWaitDialogSetStatus(final String status, final int taskCompleted) {
		if (waitDialogTaskProgress != null)
			waitDialogTaskProgress.setStatusAndProgressThreadsafe(status, taskCompleted);
	}
	
	public static void activeWaitDialogAbortTask() {
		if (waitDialogTaskProgress != null)
			waitDialogTaskProgress.abortTask();
	}
	
	public static Shell getActiveWaitDialogShell() {
		return waitDialogShell;
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
	public static boolean openWaitDialog(final Shell parent, final String title, final Runnable runnable, final int maxProgressValue) {
		if (waitDialogShell != null)
			return false;
		
		waitDialogShell = makeShell(parent, SWT.MIN | SWT.MAX | SWT.RESIZE);
		waitDialogShell.setLayout(new FillLayout());
		waitDialogTaskProgress = new TaskProgress(waitDialogShell, 
				maxProgressValue < 1 ? SWT.INDETERMINATE : SWT.NONE, runnable);
		waitDialogTaskProgress.setProgressMaximum(maxProgressValue);
		waitDialogTaskProgress.addListener(TaskProgress.TaskFinished, new Listener() {
			public void handleEvent(Event event) {
				waitDialogShell.close();
			}
		});
		waitDialogShell.pack();
		waitDialogShell.setSize(300, 100);
		centerShell(waitDialogShell);
		Display display = Display.getCurrent();
		waitDialogTaskProgress.setTitle(title);
		waitDialogTaskProgress.setProgressMaximum(maxProgressValue);
		waitDialogShell.addListener(SWT.Traverse, new Listener () {
			public void handleEvent (Event event) {
				switch (event.detail) {
					case SWT.TRAVERSE_ESCAPE:
						waitDialogTaskProgress.abortTask();
						event.detail = SWT.TRAVERSE_NONE;
						event.doit = false;
						break;
				}
			}
		});
		waitDialogShell.open();
		waitDialogTaskProgress.startTask();

		while (!waitDialogShell.isDisposed()) {
			if (display.readAndDispatch()) {
				// Check once more if dialog is disposed as the dialog is disposed 
				// in a call to readAndDispatch() and for some reason the
				// readAndDispatch() returns a wrong "true" value;
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
	
	private static class CallableWrapper<V> implements Runnable {
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
				SwtUtil.activeWaitDialogAbortTask();
			}
		}
	}
	
	public static <V> V openWaitDialog(final Shell parent, final String title, final Callable<V> callable, final int maxProgressValue) throws Exception {
		CallableWrapper<V> wrapper = new CallableWrapper<V>(callable);
		boolean res = openWaitDialog(parent, title, wrapper, maxProgressValue);
		if (res && (wrapper.exception == null)) {
			return wrapper.result;
		}
		if (wrapper.exception != null)
			throw wrapper.exception;
		throw new InterruptedException("Aborted");
	}
	
	private static Shell memoryMonitorShell = null;

	/**
	 * Closes safely a shell using the {@link Display#asyncExec(Runnable)}. 
	 */
	public static void asyncCloseShell(final Shell shell) {
		if ((shell == null) || shell.isDisposed())
			return;
		Display d = shell.getDisplay();
		if ((d == null) || d.isDisposed())
			return;
		d.asyncExec(new Runnable() {
			public void run() {
				if ((shell == null) || shell.isDisposed())
					return;
				shell.close();
			}
		});
	}
	
	public synchronized static boolean isMemoryMonitorOpened() {
		if ((memoryMonitorShell == null) || memoryMonitorShell.isDisposed())
			return false;
		return memoryMonitorShell.isVisible();
	}
	
	/**
	 * Closes the previously opened memory monitor dialog.
	 */
	public synchronized static void closeMemoryMonitor() {
		asyncCloseShell(memoryMonitorShell);
		memoryMonitorShell = null;
	}
	
	/**
	 * Opens a memory monitor dialog. 
	 */
	public synchronized static void openMemoryMonitor(final Shell parent, final boolean closeable) {
		asyncCloseShell(memoryMonitorShell);
		memoryMonitorShell = makeShell(parent, closeable ? SWT.TITLE | SWT.CLOSE : SWT.TITLE);

		GridLayout layout = new GridLayout();
		layout.numColumns = 1;

		memoryMonitorShell.setLayout(layout);
		memoryMonitorShell.setText("System monitor");
		
		SystemMonitor sysMon = new SystemMonitor(memoryMonitorShell, SWT.NONE);
		GridData shellLayoutData = new GridData();
		shellLayoutData.horizontalAlignment = SWT.FILL;
		shellLayoutData.grabExcessHorizontalSpace = true;
		sysMon.setLayoutData(shellLayoutData);
		
		if (closeable) {
			memoryMonitorShell.addListener (SWT.Traverse, new Listener () {
				public void handleEvent (Event event) {
					switch (event.detail) {
						case SWT.TRAVERSE_ESCAPE:
							memoryMonitorShell.close();
							event.detail = SWT.TRAVERSE_NONE;
							event.doit = false;
							break;
					}
				}
			});
		}
		memoryMonitorShell.pack();
		memoryMonitorShell.open();
	}
	
	/**
	 * Opens a dialog that has the behaviour of the standard SWING JOptionPane dialog.
	 * <p>
	 * Returns the selected object or null is the dialog is canceled.
	 */
	public static String optionBox(Shell parent, String message, String title, String selectedItem, String... items) {
		return SwtOptionBox.optionBox(parent, message, title, selectedItem, items);
	}
}
