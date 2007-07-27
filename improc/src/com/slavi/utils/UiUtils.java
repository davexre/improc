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
	
	public static String browseForFolder(Shell parent, String message, String defaultDir) {
		if (browseForFolderDialog == null) {
			browseForFolderDialog = new DirectoryDialog(parent);
		}
		browseForFolderDialog.setMessage(message);
		browseForFolderDialog.setFilterPath(defaultDir);
		browseForFolderDialog.setText("Some text");
		
		return browseForFolderDialog.open();
	}
	
	public static Font getBoldFont(Font font) {
		FontData fd[] = font.getFontData();
		if (fd.length > 0) {
			FontData lastfd = fd[fd.length - 1];
			lastfd.setStyle(SWT.BOLD);
		}
		return new Font(font.getDevice(), fd);
	}

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
	
	public static void activeWaitDialogSetStatus(String status, int taskCompleted) {
		if (waitDialogTaskProgress != null)
			waitDialogTaskProgress.setStatusAndProgressThreadsafe(status, taskCompleted);
	}
	
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
