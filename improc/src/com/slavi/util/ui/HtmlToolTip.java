package com.slavi.util.ui;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class HtmlToolTip {
	static Shell tipShell;
	static Control tippedControl;
	static Browser browser;
	static int tipWidth = 200;  
	static int tipHeight = 100;  
	
	static final Listener controlListener = new Listener() {
		public void handleEvent(Event event) {
			if (event.widget instanceof Control) {
				Control control = (Control) event.widget;
				switch (event.type) {
				case SWT.MouseExit: {
					if ((tipShell == null) || (tipShell.isDisposed()))
						break;
					Point p = control.toDisplay(event.x, event.y);
					if (!tipShell.getBounds().contains(p)) {
						showToolTip(null);
					}
					break;
				}
				case SWT.Dispose:
				case SWT.FocusOut:
				case SWT.Hide:
					showToolTip(null);
					break;
				case SWT.MouseHover:
					showToolTip(control);
					break;
				}
			}
		}
	};
	
	static final Listener tipListener = new Listener() {
		public void handleEvent(Event event) {
			if (event.widget instanceof Control) {
				Control control = (Control) event.widget;
				switch (event.type) {
				case SWT.Resize: {
					Rectangle r = tipShell.getBounds();
					tipWidth = r.width;
					tipHeight = r.height;
					break;
				}
				case SWT.MouseExit: {
					if ((tipShell == null) || (tipShell.isDisposed()))
						break;
					Point p = control.toDisplay(event.x, event.y);
					if (!tipShell.getBounds().contains(p)) {
						showToolTip(null);
					}
					break;
				}
				case SWT.Dispose:
				case SWT.FocusOut:
					showToolTip(null);
					break;
				}
			}
		}
	};
	
	static void showToolTip(Control control) {
		if (tippedControl == control)
			return;
		if (tipShell != null) {
			tipShell.dispose();
			tipShell = null;
			tippedControl = null;
		}
		if ((control == null) || control.isDisposed())
			return;
		
		Object o = control.getData("htmlHelp");
		if (!(o instanceof String) && !(o instanceof URL))
			return;
		tippedControl = control;
		
		Shell parent = control.getShell();
		tipShell = new Shell(parent, SWT.ON_TOP | SWT.NO_FOCUS | SWT.TOOL | SWT.BORDER | SWT.RESIZE);
		tipShell.setLayout(new FillLayout());
		browser = new Browser(tipShell, SWT.NONE);
		if (o instanceof String)
			browser.setText((String) o);
		else if (o instanceof URL)
			browser.setUrl(((URL)o).toExternalForm());
		Rectangle rect = control.getBounds();
		Point point = control.toDisplay(0, rect.height);
		tipShell.setBounds(point.x, point.y, tipWidth, tipHeight);
		tipShell.addListener(SWT.Resize, tipListener);
		tipShell.addListener(SWT.Dispose, tipListener);
		//tipShell.addListener(SWT.FocusOut, tipListener);
		//tipShell.addListener(SWT.MouseExit, tipListener);
		browser.addListener(SWT.FocusOut, tipListener);
		browser.addListener(SWT.MouseExit, tipListener);
		
		browser.addLocationListener(new LocationListener() {
			public void changing(LocationEvent event) {
				System.out.println("changing: " + event);
			}
			
			public void changed(LocationEvent event) {
				System.out.println("changED:  " + event);
			}
		});
		
		tipShell.setVisible(true);
	}

	private static void internalAddHtmlTooltip(Control control, Object htmlHelp) {
		if (control.isDisposed())
			return;
		control.setToolTipText(null);
		control.setData("htmlHelp", htmlHelp);
		
		control.addListener(SWT.Dispose, controlListener);
		control.addListener(SWT.FocusOut, controlListener);
		control.addListener(SWT.MouseExit, controlListener);
		control.addListener(SWT.Hide, controlListener);
		control.addListener(SWT.MouseHover, controlListener);
	}

	public static void addHtmlTooltip(Control control, String htmlHelp) {
		internalAddHtmlTooltip(control, htmlHelp);
	}
	
	public static void addHtmlTooltip(Control control, URL helpUrl) {
		internalAddHtmlTooltip(control, helpUrl);
	}
	
	public static void main(String[] args) throws Exception {
		final Display display = new Display();
		final Shell shell = new Shell(display);
		shell.setLayout(new GridLayout(2, false));

		Text text;
		new Label(shell, SWT.None).setText("Some label 1");
		text = new Text(shell,SWT.None);
		URL helpUrl = HtmlToolTip.class.getResource("HtmlToolTip.html");
		addHtmlTooltip(text, helpUrl);
		new Label(shell, SWT.None).setText("Some label 2");
		text = new Text(shell,SWT.None);
		addHtmlTooltip(text, "<html><body><p><big>2</bid>Kuku ruku<br/><b>Vafla chudna</b></p></body></html>");
		new Label(shell, SWT.None).setText("Some label 3");
		text = new Text(shell,SWT.None);
		addHtmlTooltip(text, "<html><body><p><big>3</bid>Kuku ruku<br/><b>Vafla chudna</b></p></body></html>");
		new Label(shell, SWT.None).setText("dir.g");
		text = new Text(shell,SWT.None);
		addHtmlTooltip(text, new URL("http://www.dir.bg"));

		shell.pack();
		SwtUtil.centerShell(shell);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}
}
