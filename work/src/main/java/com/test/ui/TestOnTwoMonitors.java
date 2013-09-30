package com.test.ui;

import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.slavi.util.ui.SwtUtil;

public class TestOnTwoMonitors {

	public static void openModal(Shell parent) {
		final Shell shell = new Shell(parent, SWT.APPLICATION_MODAL | SWT.SHELL_TRIM);
		
		shell.setLayout(new RowLayout());
		Button btn = new Button(shell, SWT.PUSH);
		btn.setText("Push me!");
		btn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				System.out.println(shell.getMonitor().getBounds());
				openModal(shell);
//				SwtUtil.msgbox(shell, "asd");
			}
		});
		shell.pack();
		SwtUtil.centerShell(shell);
		shell.open();
		Display display = shell.getDisplay();
		while (!shell.isDisposed()) {
			if (display.readAndDispatch()) {
				if (!shell.isDisposed()) 
					display.sleep();
			}
			
		}
//		shell.dispose();
	}
	
	public static void main(String[] args) throws IOException {
		openModal(null);
	}
}
