package com.slavi.improc.ui;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.slavi.util.ui.SwtUtil;

public class SettingsDialog {
	
	String[] strAdjustMethods = {
			"Helmert transformation",
			"Affine transformation",
			"SpherePanoTransformLearner", 
			"SpherePanoTransformLearner2",
			"MyPanoPairTransformLearner (XYZ)",
			"MyPanoPairTransformZYXLearner",
			"MyPanoPairTransformZYZLearner",
			"ZYZ 7 params Learner"
			};
	String[] strAdjustClasses = {
			"com.slavi.improc.myadjust.HelmertPanoTransformLearner",
			"com.slavi.improc.myadjust.AffinePanoTransformLearner",
			"com.slavi.improc.myadjust.sphere.SpherePanoTransformLearner",
			"com.slavi.improc.myadjust.sphere2.SpherePanoTransformLearner2",
			"com.slavi.improc.myadjust.xyz.MyPanoPairTransformLearner",
			"com.slavi.improc.myadjust.zyx.MyPanoPairTransformZYXLearner",
			"com.slavi.improc.myadjust.zyz.MyPanoPairTransformZYZLearner",
			"com.slavi.improc.myadjust.zyz7params.ZYZ_7ParamsLearner"
	};
	
	Shell shell;

	// Control references
	Text txtImagesRoot;
	Text txtKeyPointFileRoot;
	Text txtOutputDir;
	Combo optAdjustMethod;
	Button btnPinPoints;
	Button btnUseColorMasks;
	Button btnUseImageMaxWeight;
	
	Button btnCancel;
	Button btnNext;
	boolean result;
		
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
		shellLayoutData.horizontalAlignment = SWT.FILL;
		shellLayoutData.grabExcessHorizontalSpace = true;
		group.setLayoutData(shellLayoutData);
		
		Label label;
		Button button;
		
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
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
				String result = SwtUtil.browseForFolder(shell, "Images root folder", txtImagesRoot.getText());
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
				String result = SwtUtil.browseForFolder(shell, "Key point files root folder", txtKeyPointFileRoot.getText());
				if (result != null)
					txtKeyPointFileRoot.setText(result); 
			}
		});

		//////////////
		
		label = new Label(group, SWT.RIGHT);
		label.setText("Output files folder");
		txtOutputDir = new Text(group, SWT.BORDER);
		txtOutputDir.setLayoutData(gridData);
		
		button = new Button(group, SWT.PUSH);
		button.setText("Browse");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String result = SwtUtil.browseForFolder(shell, "Output files folder", txtOutputDir.getText());
				if (result != null)
					txtOutputDir.setText(result); 
			}
		});
		
		//////////////
		
		group = new Group(shell, SWT.NONE);
		group.setText("Rendering options");
		GridLayout lay = new GridLayout(1, true);
		lay.marginLeft = 0;
		lay.marginTop = 0;
		group.setLayout(lay);
		shellLayoutData = new GridData();
		shellLayoutData.horizontalAlignment = SWT.FILL;
		shellLayoutData.grabExcessHorizontalSpace = true;
		group.setLayoutData(shellLayoutData);

		/////////////
		
		optAdjustMethod = new Combo(group, SWT.READ_ONLY);
		optAdjustMethod.setItems(strAdjustMethods);
		
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
		shellLayoutData.horizontalAlignment = SWT.END;
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
					settings.adjustMethodClassName = strAdjustClasses[optAdjustMethod.getSelectionIndex()];
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
	
	public boolean validateInput() {
		File f = new File(txtImagesRoot.getText());
		if (!f.isDirectory())
			return false;

		f = new File(txtKeyPointFileRoot.getText());
		f.mkdirs();
		if (!f.isDirectory())
			return false;
		
		f = new File(txtOutputDir.getText());
		f.mkdirs();
		if (!f.isDirectory())
			return false;
		
		return true;
	}
	
	Settings settings;
	
	public boolean open(Settings settings) {
		this.settings = settings;
		txtImagesRoot.setText(settings.imagesRootStr);
		txtKeyPointFileRoot.setText(settings.keyPointFileRootStr);
		txtOutputDir.setText(settings.outputDirStr);

		int selectedItemIndex = 0;
		for (int i = 0; i < strAdjustClasses.length; i++) {
			String item = strAdjustClasses[i];
			if (item != null && item.equalsIgnoreCase(settings.adjustMethodClassName)) {
				selectedItemIndex = i;
				break;
			}				
		}
		optAdjustMethod.select(selectedItemIndex);
		btnPinPoints.setSelection(settings.pinPoints);
		btnUseColorMasks.setSelection(settings.useColorMasks);
		btnUseImageMaxWeight.setSelection(settings.useImageMaxWeight);
		
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
