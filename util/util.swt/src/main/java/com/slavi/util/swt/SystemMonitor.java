package com.slavi.util.swt;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimerTask;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;

import com.slavi.util.Marker;
import com.slavi.util.Util;

public class SystemMonitor extends Composite {

	// Control references
	long startedAt;
	Label lblStartedAt;
	Label lblTimeElapsed;
	Label lblMemoryInit;
	Label lblMemoryUsed;
	Label lblMemoryCommitted;
	Label lblMemoryMax;
	Label lblGCCounter;
	Label lblActiveThreadsCount;
	ProgressBar progressBar;

	private volatile int refreshRateMillis = 200;
	
	private Runnable refreshTask = new Runnable() {
		public void run() {
			if (isDisposed())
				return;
			Display d = getDisplay();
			if (d == null || d.isDisposed())
				return;
			try {
				refreshData();
				SwtUtil.timer.schedule(new RefreshTimerTask(), refreshRateMillis);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	};
	
	private class RefreshTimerTask extends TimerTask {
		public void run() {
			if (isDisposed())
				return;
			Display d = getDisplay();
			if (d == null || d.isDisposed())
				return;
			d.asyncExec(refreshTask);
		}
	};
	
	public int getRefreshRateMillis() {
		return refreshRateMillis;
	}
	
	public void setRefreshRateMillis(int refreshRateMillis) {
		this.refreshRateMillis = refreshRateMillis;
	}
	
	public SystemMonitor(Composite parent, int style) {
		super(parent, style);
		createWidgets();
		refreshTask.run();
	}

	private static final long memoryUsageDivisor = 100000;
	private void refreshData() {
		MemoryUsage memoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
		lblTimeElapsed.setText(Util.getFormatedMilliseconds(System.currentTimeMillis() - startedAt));
		lblMemoryInit.setText(Util.getFormatBytes(memoryUsage.getInit()));
		lblMemoryUsed.setText(Util.getFormatBytes(memoryUsage.getUsed()));
		lblMemoryCommitted.setText(Util.getFormatBytes(memoryUsage.getCommitted()));
		lblMemoryMax.setText(Util.getFormatBytes(memoryUsage.getMax()));
		lblGCCounter.setText(Integer.toString(Marker.getGarbageCollectionCounter()));
		lblActiveThreadsCount.setText(Integer.toString(getAllActiveThreadsCount()));
		progressBar.setMaximum((int)(memoryUsage.getMax() / memoryUsageDivisor));
		progressBar.setSelection((int)(memoryUsage.getUsed() / memoryUsageDivisor));
	}

	public static int getAllActiveThreadsCount() {
		ThreadGroup root = Thread.currentThread().getThreadGroup();
		for (ThreadGroup tmp = root; tmp != null; tmp = root.getParent())
			root = tmp;
		return root.activeCount();
	}
	
	private void createWidgets() {
		setLayout(new FillLayout());
		Group group = new Group(this, SWT.NONE);
		group.setText("Memory Info");

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		
		Label label;
		
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.widthHint = 120;

		startedAt = System.currentTimeMillis();
		label = new Label(group, SWT.RIGHT);
		label.setText("Started at");
		lblStartedAt = new Label(group, SWT.LEFT);
		lblStartedAt.setLayoutData(gridData);
		SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		lblStartedAt.setText(df.format(new Date(startedAt)));
		
		label = new Label(group, SWT.RIGHT);
		label.setText("Time elapsed");
		lblTimeElapsed = new Label(group, SWT.LEFT);
		lblTimeElapsed.setLayoutData(gridData);
		
		label = new Label(group, SWT.RIGHT);
		label.setText("Initial memory");
		lblMemoryInit = new Label(group, SWT.LEFT);
		lblMemoryInit.setLayoutData(gridData);

		label = new Label(group, SWT.RIGHT);
		label.setText("Used memory");
		lblMemoryUsed = new Label(group, SWT.LEFT);
		lblMemoryUsed.setLayoutData(gridData);
		
		label = new Label(group, SWT.RIGHT);
		label.setText("Committed memory");
		lblMemoryCommitted = new Label(group, SWT.LEFT);
		lblMemoryCommitted.setLayoutData(gridData);
		
		label = new Label(group, SWT.RIGHT);
		label.setText("Max memory");
		lblMemoryMax = new Label(group, SWT.LEFT);
		lblMemoryMax.setLayoutData(gridData);
		
		label = new Label(group, SWT.RIGHT);
		label.setText("Garbage collection counter");
		lblGCCounter = new Label(group, SWT.LEFT);
		lblGCCounter.setLayoutData(gridData);
		
		label = new Label(group, SWT.RIGHT);
		label.setText("Active threads count");
		lblActiveThreadsCount = new Label(group, SWT.LEFT);
		lblActiveThreadsCount.setLayoutData(gridData);

		label = new Label(group, SWT.RIGHT);
		label.setText("Memory usage");
		
		progressBar = new ProgressBar(group, SWT.HORIZONTAL | SWT.SMOOTH);
		progressBar.setMinimum(0);
		progressBar.setMaximum(100);
		progressBar.setSelection(0);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 2;
		progressBar.setLayoutData(gridData);
	}
}
