package com.slavi.util.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

public class TaskManager extends Composite {

	TabFolder tabFolder;
	TabItem systemMonitorTab;
	TabItem taskManagerTab;
	
	SystemMonitor systemMonitor;
	ProcessManager processManager;
		
	public TaskManager(Composite parent, int style) {
		super(parent, style);
		createWidgets();
	}

	private void createWidgets() {
		setLayout(new FillLayout());
		tabFolder = new TabFolder(this, SWT.NONE);
		systemMonitor = new SystemMonitor(tabFolder, SWT.NONE);
		processManager = new ProcessManager(tabFolder, SWT.NONE);

		systemMonitorTab = new TabItem(tabFolder, SWT.NONE);
		systemMonitorTab.setText("&General");
		systemMonitorTab.setControl(systemMonitor);
		
		taskManagerTab = new TabItem(tabFolder, SWT.NONE);
		taskManagerTab.setText("&Threads");
		taskManagerTab.setControl(processManager);
	}
}
