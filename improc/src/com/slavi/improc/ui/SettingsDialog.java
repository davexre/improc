package com.slavi.improc.ui;

import java.io.File;
import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.slavi.util.ui.SwtUtil;

public class SettingsDialog {
	
	Shell shell;

	// Control references
	Text txtImagesRoot;
	Text txtKeyPointFileRoot;
	Text txtKeyPointPairFileRoot;
	Button btnPinPoints;
	Button btnUseColorMasks;
	Button btnUseImageMaxWeight;
	
	Button btnCancel;
	Button btnNext;
	boolean result;
	
	DirectoryDialog browseForFolderDialog = null;
	
	public SettingsDialog(Shell parent) {
		shell = new Shell(parent, SWT.SHELL_TRIM);
		createWidgets();
		SwtUtil.centerShell(shell);
	}

	private void createWidgets() {
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;

		shell.setLayout(layout);
		shell.setText("Image process");
		
		// Create settings group
		Group group = new Group(shell, SWT.NONE);
		group.setText("Settings");
		layout= new GridLayout();
		layout.numColumns = 3;
		group.setLayout(layout);
		GridData shellLayoutData = new GridData();
		shellLayoutData.horizontalAlignment = GridData.FILL;
		shellLayoutData.grabExcessHorizontalSpace = true;
		group.setLayoutData(shellLayoutData);
		
		Label label;
		Button button;
		
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.widthHint = 300;

		label = new Label(group, SWT.RIGHT);
		label.setText("Images root folder");
		txtImagesRoot = new Text(group, SWT.BORDER);
		txtImagesRoot.setLayoutData(gridData);
		
		button = new Button(group, SWT.PUSH);
		button.setText("Browse");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String result = SwtUtil.browseForFolder(shell, "Message 1", txtImagesRoot.getText());
				if (result != null)
					txtImagesRoot.setText(result); 
			}
		});

		//////////////

		label = new Label(group, SWT.RIGHT);
		label.setText("Key point files root folder");
		txtKeyPointFileRoot = new Text(group, SWT.BORDER);
		txtKeyPointFileRoot.setLayoutData(gridData);
		
		button = new Button(group, SWT.PUSH);
		button.setText("Browse");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String result = SwtUtil.browseForFolder(shell, "Message 2", txtKeyPointFileRoot.getText());
				if (result != null)
					txtKeyPointFileRoot.setText(result); 
			}
		});

		//////////////
		
		label = new Label(group, SWT.RIGHT);
		label.setText("Key point pair files root folder");
		txtKeyPointPairFileRoot = new Text(group, SWT.BORDER);
		txtKeyPointPairFileRoot.setLayoutData(gridData);
		
		button = new Button(group, SWT.PUSH);
		button.setText("Browse");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String result = SwtUtil.browseForFolder(shell, "Message 3", txtKeyPointPairFileRoot.getText());
				if (result != null)
					txtKeyPointPairFileRoot.setText(result); 
			}
		});
		
		//////////////
		
		group = new Group(shell, SWT.NONE);
		group.setText("Rendering options");
		group.setLayout(new FillLayout(SWT.VERTICAL));
		shellLayoutData = new GridData();
		shellLayoutData.horizontalAlignment = GridData.FILL;
		shellLayoutData.grabExcessHorizontalSpace = true;
		group.setLayoutData(shellLayoutData);

		/////////////
		
		btnPinPoints = new Button(group, SWT.CHECK);
		btnPinPoints.setText("Pin points");
		
		btnUseColorMasks = new Button(group, SWT.CHECK);
		btnUseColorMasks.setText("Use image color masks");
		
		btnUseImageMaxWeight = new Button(group, SWT.CHECK);
		btnUseImageMaxWeight.setText("Use image with max weight");
		
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
					properties.setProperty("ImagesRoot", txtImagesRoot.getText());
					properties.setProperty("KeyPointFileRoot", txtKeyPointFileRoot.getText());
					properties.setProperty("KeyPointPairFileRoot", txtKeyPointPairFileRoot.getText());
					properties.setProperty("PinPoints", btnPinPoints.getSelection() ? "true" : "false");
					properties.setProperty("UseColorMasks", btnUseColorMasks.getSelection() ? "true" : "false");
					properties.setProperty("UseImageMaxWeight", btnUseImageMaxWeight.getSelection() ? "true" : "false");
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
	
	public boolean validateInput() {
		File f = new File(txtImagesRoot.getText());
		if (!f.isDirectory())
			return false;

		f = new File(txtKeyPointFileRoot.getText());
		f.mkdirs();
		if (!f.isDirectory())
			return false;
		
		f = new File(txtKeyPointPairFileRoot.getText());
		f.mkdirs();
		if (!f.isDirectory())
			return false;
		
		return true;
	}
	
	Properties properties;
	
	public static boolean getBooleanProperty(Properties properties, String propertyName) {
		String val = properties.getProperty(propertyName, "false");
		return !"false".equalsIgnoreCase(val);
	}
	
	public boolean open(Properties properties) {
		this.properties = properties;
		String userHomeRoot = System.getProperty("user.home");
		txtImagesRoot.setText(properties.getProperty("ImagesRoot", userHomeRoot));
		txtKeyPointFileRoot.setText(properties.getProperty("KeyPointFileRoot", userHomeRoot));
		txtKeyPointPairFileRoot.setText(properties.getProperty("KeyPointPairFileRoot", userHomeRoot));
		btnPinPoints.setSelection(getBooleanProperty(properties, "PinPoints"));
		btnUseColorMasks.setSelection(getBooleanProperty(properties, "UseColorMasks"));
		btnUseImageMaxWeight.setSelection(getBooleanProperty(properties, "UseImageMaxWeight"));
		
		result = false;
		shell.open();
		Display display = shell.getDisplay();
		while(!shell.isDisposed()){
			if(!display.readAndDispatch())
				display.sleep();
		}
		return result;
	}	
}
