package com.test.ui;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class TestPaintSWT {

	public static class SmileyCanvas extends Composite {

		public SmileyCanvas(Composite parent, int style) {
			super(parent, style);
			addListener(SWT.Paint, new Listener() {
				public void handleEvent(Event event) {
			    	Rectangle rect = getClientArea();
			    	paint(event.gc, rect.width, rect.height);
				}
			});
		}

		public void paint(GC gc, int width, int height) {
	        int d = Math.min(width, height); // diameter
	        int ed = d/20; // eye diameter
	        int x = (width - d)/2;
	        int y = (height - d)/2;

	        // draw head (color already set to foreground)
	        gc.setBackground(new Color(getDisplay(), 255, 255, 0));
	        gc.fillOval(x, y, d, d);
	        gc.setForeground(new Color(getDisplay(), 0, 0, 0));
	        gc.drawOval(x, y, d, d);

	        // draw eyes
	        gc.setBackground(new Color(getDisplay(), 0, 0, 0));
	        gc.fillOval(x+d/3-(ed/2), y+d/3-(ed/2), ed, ed);
	        gc.fillOval(x+(2*(d/3))-(ed/2), y+d/3-(ed/2), ed, ed);

	        //draw mouth
	        gc.drawArc(x+d/4, y+2*(d/5), d/2, d/3, 0, -180);
		}
	}
	
	public static void main(String[] args) {
		Shell shell = new Shell();
		shell.setLayout(new FillLayout());
		shell.setText("Have a nice day!");
		SmileyCanvas c = new SmileyCanvas(shell, SWT.DOUBLE_BUFFERED);
		c.setBackground(new Color(shell.getDisplay(), 255, 0, 0));
		shell.pack();
		shell.setSize(300, 300);
		shell.open();
		Display display = shell.getDisplay();
		while(!shell.isDisposed()){
			if(!display.readAndDispatch())
				display.sleep();
		}
    }
}
