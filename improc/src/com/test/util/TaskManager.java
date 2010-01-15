package com.test.util;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.slavi.util.ui.SwtUtil;

public class TaskManager {
	
	private static Timer timer = new Timer(true);
	
	Shell shell;
	Table table;
	
	TableColumn columnTID;
	TableColumn columnName;
	TableColumn columnCPU;
	TableColumn columnState;
	TableColumn columnPriority;
	TableColumn columnIsAlive;
	TableColumn columnIsDaemon;
	TableColumn columnIsInterrupted;
	
	private int refreshRateMillis;

	private final ThreadMXBean threadMXBean;
	
	private long lastRefreshNanoTime;

	private int numberOfProcessors;

	private ThreadGroup rootThreadGroup;

	private HashMap<Long, ThreadData> threadData;

	private HashMap<Long, ThreadData> oldThreadData;
	
	Runnable refreshTask;
	
	public TaskManager() {
		rootThreadGroup = Thread.currentThread().getThreadGroup();
		for (ThreadGroup tmp = rootThreadGroup; tmp != null; tmp = rootThreadGroup.getParent()) {
			rootThreadGroup = tmp;
		}
		refreshRateMillis = 1000;
		numberOfProcessors = Runtime.getRuntime().availableProcessors();
		threadMXBean = ManagementFactory.getThreadMXBean();
		lastRefreshNanoTime = System.nanoTime();
		threadData = new HashMap<Long, ThreadData>();
		oldThreadData = new HashMap<Long, ThreadData>();
		refreshTask = new Runnable() {
			public void run() {
				if (shell.isDisposed())
					return;
				Display display = shell.getDisplay();
				if (display.isDisposed())
					return;
				refresh();
				timer.schedule(new RefreshTimerTask(), refreshRateMillis);
			}
		};
	}
	
	private static final int increaseColumnWidth = 35;
	
	void createWidgets() {
		shell = new Shell();
		shell.setLayout(new FillLayout());
		table = new Table(shell, SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
		table.setSortDirection(SWT.DOWN);
		table.setHeaderVisible(true);

		SelectionListener columnListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (e.widget == table.getSortColumn()) {
					/* If the sort column hasn't changed, cycle down -> up -> down. */
					int sortDir = table.getSortDirection();
					table.setSortDirection(sortDir == SWT.UP ? SWT.DOWN : SWT.UP);
				} else {
					table.setSortColumn((TableColumn)e.widget);
					table.setSortDirection(SWT.UP);
				}
				refresh();
			}
		};
		
		TableColumn col;
		col = columnTID = new TableColumn(table, SWT.NONE);
		col.setText("TID");
		col.addSelectionListener(columnListener);
		col.pack();
		col.setWidth(col.getWidth() + increaseColumnWidth);
		
		col = columnName = new TableColumn(table, SWT.NONE);
		col.setText("Thread name");
		col.addSelectionListener(columnListener);
		col.pack();
		col.setWidth(col.getWidth() + increaseColumnWidth);
		
		col = columnCPU = new TableColumn(table, SWT.NONE);
		col.setText("CPU");
		col.addSelectionListener(columnListener);
		col.pack();
		col.setWidth(col.getWidth() + increaseColumnWidth);
		
		col = columnState = new TableColumn(table, SWT.NONE);
		col.setText("State");
		col.addSelectionListener(columnListener);
		col.pack();
		col.setWidth(col.getWidth() + increaseColumnWidth);
		
		col = columnPriority = new TableColumn(table, SWT.NONE);
		col.setText("Priority");
		col.addSelectionListener(columnListener);
		col.pack();
		col.setWidth(col.getWidth() + increaseColumnWidth);
		
		col = columnIsAlive = new TableColumn(table, SWT.NONE);
		col.setText("Alive");
		col.addSelectionListener(columnListener);
		col.pack();
		col.setWidth(col.getWidth() + increaseColumnWidth);
		
		col = columnIsDaemon = new TableColumn(table, SWT.NONE);
		col.setText("Daemon");
		col.addSelectionListener(columnListener);
		col.pack();
		col.setWidth(columnState.getWidth() + increaseColumnWidth);
		
		col = columnIsInterrupted = new TableColumn(table, SWT.NONE);
		col.setText("Interrupted");
		col.addSelectionListener(columnListener);
		col.pack();
		col.setWidth(columnState.getWidth() + increaseColumnWidth);
		
		table.setSortColumn(columnTID);
		table.setSortDirection(SWT.UP);
		table.pack();
		shell.pack();
		shell.setSize(350, 600);
		SwtUtil.centerShell(shell);
		refreshTask.run();
	}

	private static class ThreadData {
		public long id;
		public long lastCpuTime;
		public int cpuUsage;
		public String threadName;
		public int priority;
		public String state;
		public boolean isAlive;
		public boolean isDaemon;
		public boolean isInterrupted;
	}
	
	private class RefreshTimerTask extends TimerTask {
		public void run() {
			if (shell.isDisposed())
				return;
			Display display = shell.getDisplay();
			if (display.isDisposed())
				return;
			display.asyncExec(refreshTask);
		}
	}
	
	private Comparator columnSort = new Comparator<ThreadData>() {
		public int compare(ThreadData o1, ThreadData o2) {
			TableColumn sortCol = table.getSortColumn();
			if (table.getSortDirection() == SWT.DOWN) {
				ThreadData tmp = o1;
				o1 = o2;
				o2 = tmp;
			}			
			int result = 0;

			if (sortCol == columnCPU) {
				result = o1.cpuUsage == o2.cpuUsage ? 0 : o1.cpuUsage < o2.cpuUsage ? -1 : 1;
			} else if (sortCol == columnName) {
				result = o1.threadName.compareTo(o2.threadName);
			} else if (sortCol == columnState) {
				result = o1.state.compareTo(o2.state);
			} else if (sortCol == columnPriority) {
				result = o1.priority == o2.priority ? 0 : o1.priority < o2.priority ? -1 : 1;
			} else if (sortCol == columnIsAlive) {
				result = o1.isAlive == o2.isAlive ? 0 : o1.isAlive ? 1 : -1;
			} else if (sortCol == columnIsDaemon) {
				result = o1.isDaemon == o2.isDaemon ? 0 : o1.isDaemon ? 1 : -1;
			} else if (sortCol == columnIsInterrupted) {
				result = o1.isInterrupted == o2.isInterrupted ? 0 : o1.isInterrupted ? 1 : -1;
			} // if (sortCol == columnTID)
			
			if (result == 0)
				result = o1.id == o2.id ? 0 : o1.id < o2.id ? -1 : 1;
			return result;
		}
	};
	
	private void refresh() {
		TableItem items[] = table.getItems();
		TableItem selected[] = table.getSelection();
		long selectedTid = selected.length <= 0 ? -1 : ((ThreadData) selected[0].getData()).id;  
		
		oldThreadData.clear();
		for (TableItem item : items) {
			ThreadData data = (ThreadData) item.getData(); 
			oldThreadData.put(data.id, data);
		}
		long[] tids = threadMXBean.getAllThreadIds();
		long curNanoTime = System.nanoTime();
		long div = (numberOfProcessors * (curNanoTime - lastRefreshNanoTime)) / 100;
		if (div == 0)
			div = 1;
		
		threadData.clear();
		for (long tid : tids) {
			long cpuTime = threadMXBean.getThreadCpuTime(tid);
			ThreadData d = oldThreadData.get(tid);
			if (d == null) {
				d = new ThreadData();
				d.id = tid;
				d.threadName = "n/a";
				d.state = "n/a";
				d.priority = 0;
				d.isAlive = false;
				d.isDaemon = false;
				d.isInterrupted = false;
				d.lastCpuTime = cpuTime;
			}
			d.cpuUsage = (int)((cpuTime - d.lastCpuTime) / div);
			d.lastCpuTime = cpuTime;
			threadData.put(tid, d);
		}

		int activeCount = rootThreadGroup.activeCount();
		Thread threads[] = new Thread[activeCount];
		int maxThreads = rootThreadGroup.enumerate(threads, true);
		for (int i = 0; i < maxThreads; i++) {
			Thread thread = threads[i];
			long tid = thread.getId();
			ThreadData data = threadData.get(tid);
			if (data == null)
				continue;
			data.threadName = thread.getName();
			data.priority = thread.getPriority();
			data.state = thread.getState().name();
			data.isAlive = thread.isAlive();
			data.isDaemon = thread.isDaemon();
			data.isInterrupted = thread.isInterrupted();
		}
		
		lastRefreshNanoTime = curNanoTime;
		ThreadData data[] = threadData.values().toArray(new ThreadData[0]);
		Arrays.sort(data, columnSort);
		table.clearAll();
		table.removeAll();
		int sel = -1;
		for (int i = 0; i < data.length; i++) {
			ThreadData d = data[i];
			TableItem item = new TableItem(table, SWT.NONE);
			item.setData(d);
			item.setText(0, Long.toString(d.id));
			item.setText(1, d.threadName);
			item.setText(2, Long.toString(d.cpuUsage));
			item.setText(3, d.state);
			item.setText(4, Integer.toString(d.priority));
			item.setText(5, d.isAlive ? "true" : "false");
			item.setText(6, d.isDaemon ? "true" : "false");
			item.setText(7, d.isInterrupted ? "true" : "false");
			if (d.id == selectedTid)
				sel = i;
		}
		table.setSelection(sel);
	}
	
	public int getRefreshRateMillis() {
		return refreshRateMillis;
	}
	
	public void setRefreshRateMillis(int refreshRateMillis) {
		this.refreshRateMillis = refreshRateMillis;
	}
	
	
	void doit() {
		shell.open();
		Display display = shell.getDisplay();
		while(!shell.isDisposed()){
			if(!display.readAndDispatch())
				display.sleep();
		}
		shell.dispose();
	}

	public static class DummyJob extends Thread {
		public void run() {
			while (true);
		}
	}
		
	public static void main(String[] args) throws Exception {
		for (int i = 0; i < 5; i++) {
			Thread t = new DummyJob();
			t.setDaemon(true);
			t.setPriority(Thread.MIN_PRIORITY);
			t.start();			
		}
		TaskManager d = new TaskManager();
		d.createWidgets();
		d.doit();
	}
}
