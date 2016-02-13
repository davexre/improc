package com.slavi.util.ui;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Timer;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


/**
 * This class contains utility methods for creating user interface using
 * the SWT (Standard Widget Toolkit) library.   
 */
public class SwtUtil {
	
	public static final Timer timer = new Timer("Update timer", true);
	
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
		openModal(shell, true);
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
		Rectangle displayRect = shell.getMonitor().getBounds();
		Rectangle shellRect = shell.getBounds();
		shellRect.x = (displayRect.width - shellRect.width) / 2;
		shellRect.y = (displayRect.height - shellRect.height) / 2;
		if (shellRect.x < 0)
			shellRect.x = 0;
		if (shellRect.y < 0)
			shellRect.y = 0;
		shell.setBounds(shellRect);
	}
	
	private static Shell taskManagerShell = null;

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
	
	public synchronized static boolean isTaskManagerOpened() {
		if ((taskManagerShell == null) || taskManagerShell.isDisposed())
			return false;
		return taskManagerShell.isVisible();
	}
	
	/**
	 * Closes the previously opened task manager dialog.
	 */
	public synchronized static void closeTaskManager() {
		asyncCloseShell(taskManagerShell);
		taskManagerShell = null;
	}
	
	/**
	 * Opens a task manager dialog. 
	 */
	public synchronized static void openTaskManager(final Shell parent, final boolean closeable) {
		asyncCloseShell(taskManagerShell);
		taskManagerShell = makeShell(parent, SWT.TITLE | SWT.BORDER | SWT.RESIZE | (closeable ? SWT.CLOSE : 0));
		taskManagerShell.setLayout(new FillLayout());
		taskManagerShell.setText("Task manager");
		taskManagerShell.setLocation(0, 0);
		new TaskManager(taskManagerShell, SWT.NONE);
		if (closeable) {
			taskManagerShell.addListener (SWT.Traverse, new Listener () {
				public void handleEvent (Event event) {
					switch (event.detail) {
						case SWT.TRAVERSE_ESCAPE:
							taskManagerShell.close();
							event.detail = SWT.TRAVERSE_NONE;
							event.doit = false;
							break;
					}
				}
			});
		}
		taskManagerShell.pack();
		taskManagerShell.open();
	}
	
	/**
	 * Opens a dialog that has the behaviour of the standard SWING JOptionPane dialog.
	 * <p>
	 * Returns the selected object or null is the dialog is canceled.
	 */
	public static String optionBox(Shell parent, String message, String title, String selectedItem, String... items) {
		return SwtOptionBox.optionBox(parent, message, title, selectedItem, items);
	}
	
	/**
	 * Opens a dialog that prompts for the input of a String.
	 * @return The string value entered or null if button cancel is pressed
	 */
	public static String inputBox(Shell parent, String message, String title, String defaultText) {
		final AtomicReference<String> result = new AtomicReference<String>(null);
		
		final Shell shell = makeShell(parent, SWT.DIALOG_TRIM | SWT.RESIZE);
		shell.setText(title);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		shell.setLayout(layout);
		
		new Label(shell, SWT.LEFT).setText(message);
		final Text text = new Text(shell, SWT.SINGLE | SWT.BORDER);
		text.setText(defaultText);
		text.selectAll();
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.widthHint = 350;
		text.setLayoutData(gridData);
		new Label(shell, SWT.NONE);
		
		Composite buttonPanel = new Composite(shell, SWT.NONE);
		RowLayout buttonLayout = new RowLayout(SWT.HORIZONTAL);
		buttonLayout.spacing = 4;
		buttonPanel.setLayout(buttonLayout);
		
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.RIGHT;
		buttonPanel.setLayoutData(gridData);

		Button btnOk = new Button(buttonPanel, SWT.PUSH);
		btnOk.setText("OK");
		btnOk.setLayoutData(new RowData(75, -1));
		btnOk.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				result.set(text.getText());
				shell.close();
			}
		});

		Button btnCancel = new Button(buttonPanel, SWT.PUSH);
		btnCancel.setText("Cancel");
		btnCancel.setLayoutData(new RowData(75, -1));
		btnCancel.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				shell.close();
			}
		});
		
		shell.setDefaultButton(btnOk);
		shell.addListener (SWT.Traverse, new Listener () {
			public void handleEvent (Event event) {
				switch (event.detail) {
					case SWT.TRAVERSE_ESCAPE:
						shell.close ();
						event.detail = SWT.TRAVERSE_NONE;
						event.doit = false;
						break;
				}
			}
		});
		shell.pack();
		openModal(shell, true);
		return result.get();
	}
	
	private static WaitDialog waitDialog;
	private static final Object waitDialogSynch = new Object();
	
	public static void openModal(Shell shell, boolean centerShell) {
		if (shell == null)
			return;
		if (centerShell)
			centerShell(shell);
		shell.open();
		Display display = shell.getDisplay();
		while(!shell.isDisposed()){
			if (Thread.currentThread().isInterrupted()) {
				shell.dispose();
			} else if(!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}
	
	public static void activeWaitDialogAbortTask() {
		synchronized (waitDialogSynch) {
			if (waitDialog != null)
				waitDialog.abortTask();
		}
	}
	
	public static void activeWaitDialogSetStatus(final String status, final int taskCompleted) {
		synchronized (waitDialogSynch) {
			if (waitDialog != null)
				waitDialog.setStatus(status, taskCompleted);
		}
	}

	public static void activeWaitDialogProgress(int amount) {
		synchronized (waitDialogSynch) {
			if (waitDialog != null)
				waitDialog.progress(amount);
		}
	}

	public static boolean openWaitDialog(final Shell parent, final String title, final Runnable runnable, final int maxProgressValue) {
		WaitDialog w;
		synchronized (waitDialogSynch) {
			if (waitDialog != null)
				return false;
			w = waitDialog = new WaitDialog();
		}
		boolean r = w.open(parent, title, runnable, maxProgressValue);
		synchronized (waitDialogSynch) {
			if (w == waitDialog)
				waitDialog= null;
		}
		return r;
	}

	public static <V> V openWaitDialog(final Shell parent, final String title, final Callable<V> callable, final int maxProgressValue) throws Exception {
		WaitDialog w;
		synchronized (waitDialogSynch) {
			if (waitDialog != null)
				return null;
			w = waitDialog = new WaitDialog();
		}
		V r = w.open(parent, title, callable, maxProgressValue);
		synchronized (waitDialogSynch) {
			if (w == waitDialog)
				waitDialog= null;
		}
		return r;
	}

	public static ImageData copyAwtImage(java.awt.image.BufferedImage src, ImageData dest) {
		if ((dest == null) || (dest.palette == null) ||
			(dest.width != src.getWidth() || (dest.height != src.getHeight())) ||
			(dest.palette.redMask != 0xff0000) || (dest.palette.greenMask != 0xff00) || (dest.palette.blueMask != 0xff) ||
			(!dest.palette.isDirect)) {
			dest = new ImageData(src.getWidth(), src.getHeight(), 24, new PaletteData(0xff0000, 0xff00, 0xff));
		}
		for (int x = dest.width - 1; x >= 0; x--)
			for (int y = dest.height - 1; y >= 0; y--) {
				int pixel = src.getRGB(x, y);
				dest.setPixel(x, y, pixel & 0xffffff);
			}
		return dest;
	}
}
