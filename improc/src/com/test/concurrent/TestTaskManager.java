package com.test.concurrent;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.slavi.util.ui.SwtUtil;

public class TestTaskManager {

	Shell shell;

	// Control references
	Text txtImagesRoot;
	Text txtKeyPointFileRoot;
	Text txtOutputDir;
	Button btnPinPoints;
	Button btnUseColorMasks;
	Button btnUseImageMaxWeight;
	
	Button btnCancel;
	Button btnNext;
	boolean result;
	
	private void createWidgets() {
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;

		shell.setLayout(layout);
		shell.setText("Image process");
		
		// Create settings group
		Group group = new Group(shell, SWT.NONE);
		group.setText("Task manager");
		layout= new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		GridData shellLayoutData = new GridData();
		shellLayoutData.horizontalAlignment = GridData.FILL;
		shellLayoutData.grabExcessHorizontalSpace = true;
		group.setLayoutData(shellLayoutData);
		
		Label label;
		
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.widthHint = 300;

		label = new Label(group, SWT.RIGHT);
		label.setText("Total memory ");
		txtImagesRoot = new Text(group, SWT.BORDER);
		txtImagesRoot.setLayoutData(gridData);
		
		Button button;
		button = new Button(group, SWT.PUSH);
		button.setText("Browse");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String result = SwtUtil.browseForFolder(shell, "Images root folder", txtImagesRoot.getText());
				if (result != null)
					txtImagesRoot.setText(result); 
			}
		});
		
		/////////////
		
		// Create Buttons
		Composite composite = new Composite(shell, SWT.NONE);
		FillLayout btnLayout = new FillLayout(SWT.HORIZONTAL);
		btnLayout.spacing = 4;
		composite.setLayout(btnLayout);

		shellLayoutData = new GridData();
		shellLayoutData.horizontalAlignment = GridData.END;
		composite.setLayoutData(shellLayoutData);
		
		btnCancel = new Button(composite, SWT.PUSH);
		btnCancel.setText("Cancel");
		btnCancel.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				shell.close();
			}
		});
		
		btnNext = new Button(composite, SWT.PUSH);
		btnNext.setText("Next >");
		btnNext.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (validateInput()) {
					result = true;
					settings.imagesRootStr = txtImagesRoot.getText();
					settings.keyPointFileRootStr = txtKeyPointFileRoot.getText();
					settings.outputDirStr = txtOutputDir.getText();
					settings.pinPoints = btnPinPoints.getSelection();
					settings.useColorMasks = btnUseColorMasks.getSelection();
					settings.useImageMaxWeight = btnUseImageMaxWeight.getSelection();
					shell.close();
				}
			}
		});

		shell.setDefaultButton(btnNext);
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

	private void doIt() {
		Shell parent = null;
		shell = new Shell(parent, SWT.SHELL_TRIM);
		createWidgets();
		SwtUtil.centerShell(shell);
		
	}

	public static void main(String[] args) {
		new TestTaskManager().doIt();
	}
}
