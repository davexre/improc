package com.test.swt;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class MouseListenerUsing {
	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		Composite c = new Composite(shell, 0);
		c.setBackground(new Color(display, 0,0,0));
		c.addMouseListener(new MouseListener() {

			public void mouseDoubleClick(MouseEvent arg0) {
				System.out.println("mouseDoubleClick");

			}

			public void mouseDown(MouseEvent arg0) {
				System.out.println("mouseDown");

			}

			public void mouseUp(MouseEvent arg0) {
				System.out.println("mouseUp");

			}
		});

		// Display the window
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}
}
