package com.test.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.slavi.util.ui.SwtUtil;

public class TestOptionbox {

	static class OptionBox {
		Shell shell;
		String result = null;
		Combo options;
		String items[];
		
		private void createWidgets(String caption, String selectedItem) {
			int selectedItemIndex = -1;
			for (int i = 0; i < items.length; i++) {
				String item = items[i];
				if (item != null && item.equals(selectedItem)) {
					selectedItemIndex = i;
					break;
				}				
			}
			
			shell = SwtUtil.makeShell(null, SWT.DIALOG_TRIM | SWT.RESIZE);
			GridLayout layout = new GridLayout();
			layout.numColumns = 1;

			shell.setLayout(layout);
			shell.setText(caption);
			
			Label lbl = new Label(shell, SWT.NONE);
			lbl.setText(caption);
			options = new Combo(shell, SWT.READ_ONLY);
			options.setItems(items);
			options.select(selectedItemIndex);
			GridData shellLayoutData = new GridData();
			shellLayoutData.horizontalAlignment = SWT.FILL;
			shellLayoutData.grabExcessHorizontalSpace = true;
			shellLayoutData.widthHint = 220;
			options.setLayoutData(shellLayoutData);
			
			// Create Buttons
			Composite composite = new Composite(shell, SWT.NONE);
			RowLayout btnLayout = new RowLayout(SWT.HORIZONTAL);
			btnLayout.spacing = 4;
			composite.setLayout(btnLayout);

			shellLayoutData = new GridData();
			shellLayoutData.horizontalAlignment = SWT.RIGHT;
			composite.setLayoutData(shellLayoutData);
			
			Button btnOk = new Button(composite, SWT.PUSH);
			btnOk.setText("OK");
			btnOk.setLayoutData(new RowData(75, -1));
			btnOk.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					int indx = options.getSelectionIndex();
					if (indx < 0)
						return;
					result = items[indx];
					shell.close();
				}
			});

			Button btnCancel = new Button(composite, SWT.PUSH);
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
		}
		
		public String optionBox(String caption, String selectedItem, String... items) {
			if (items == null || items.length == 0)
				return null;
			this.items = items;
			createWidgets(caption, selectedItem);
			SwtUtil.centerShell(shell);
			shell.open();
			Display display = shell.getDisplay();
			while(!shell.isDisposed()){
				if(!display.readAndDispatch())
					display.sleep();
			}
			return result;
		}
	}
	
	
	public static void main(String[] args) {
		System.out.println(new OptionBox().optionBox("qqqqq", "zxc", "asd", "qwe", "zxc"));
	}
}
