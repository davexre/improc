package com.slavi.util.swt;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TimerTask;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class ProcessManager extends Composite {

	Table table;

	TableColumn columnTID;
	TableColumn columnName;
	TableColumn columnCPU;
	TableColumn columnState;
	TableColumn columnPriority;
	TableColumn columnIsDaemon;
	TableColumn columnIsInterrupted;
	TableColumn columnIsSuspended;

	private static final Runnable refreshTask;

	private static int refreshRateMillis;

	private static final ThreadMXBean threadMXBean;

	private static final int numberOfProcessors;

	private static final ThreadGroup rootThreadGroup;

	private static final HashMap<Long, ThreadData> threadData;

	private static final ArrayList<ProcessManager> refreshListeners;

	private static long lastRefreshNanoTime;

	static {
		ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
		for (ThreadGroup tmp = threadGroup; tmp != null; tmp = threadGroup.getParent()) {
			threadGroup = tmp;
		}
		rootThreadGroup = threadGroup;
		numberOfProcessors = Runtime.getRuntime().availableProcessors();
		threadMXBean = ManagementFactory.getThreadMXBean();
		threadData = new HashMap<Long, ThreadData>();
		refreshListeners = new ArrayList<ProcessManager>();
		lastRefreshNanoTime = System.nanoTime();
		refreshRateMillis = 1000;
		refreshTask = new Runnable() {
			public void run() {
				refreshAllListeners();
				synchronized (refreshListeners) {
					if (refreshListeners.size() == 0)
						return;
				}
				SwtUtil.timer.schedule(new RefreshTimerTask(), refreshRateMillis);
			}
		};
	}

	private static class ThreadData {
		public long id = -1;
		public long lastCpuTime = 0;
		public int cpuUsage = 0;
		public String threadName = "n/a";
		public int priority = 0;
		public String state = "n/a";
		public boolean isDaemon = false;
		public boolean isInterrupted = false;
		public boolean isSuspended = false;
	}

	private static class RefreshTimerTask extends TimerTask {
		public void run() {
			synchronized (refreshListeners) {
				for (int i = refreshListeners.size() - 1; i >= 0; i--) {
					ProcessManager tm = refreshListeners.get(i);
					if (tm.isDisposed()) {
						continue;
					}
					Display display = tm.getDisplay();
					display.asyncExec(refreshTask);
					return;
				}
			}
		}
	}

	public ProcessManager(Composite parent, int style) {
		super(parent, style);
		createWidgets();
		synchronized (refreshListeners) {
			refreshListeners.add(this);
			if (refreshListeners.size() == 1) {
				SwtUtil.timer.schedule(new RefreshTimerTask(), refreshRateMillis);
			}
		}
		refreshAllListeners();
	}

	private void createWidgets() {
		final ProcessManager that = this;
		setLayout(new GridLayout());
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				synchronized(refreshListeners) {
					refreshListeners.remove(that);
				}
			}
		});

		ToolBar toolbar = new ToolBar(this, SWT.HORIZONTAL | SWT.FLAT | SWT.WRAP);
		toolbar.setLayout(new RowLayout(SWT.HORIZONTAL));
		toolbar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		ToolItem btn;

		btn = new ToolItem(toolbar, SWT.PUSH);
		btn.setText("&+");
		btn.setToolTipText("Increase thread priority");
		btn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				Thread thread = getSelectedThread();
				if (thread == null)
					return;
				int priority = thread.getPriority() + 1;
				if (priority > Thread.MAX_PRIORITY)
					return;
				thread.setPriority(priority);
				refreshAllListeners();
			}
		});

		btn = new ToolItem(toolbar, SWT.PUSH);
		btn.setText("&-");
		btn.setToolTipText("Decrease thread priority");
		btn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				Thread thread = getSelectedThread();
				if (thread == null)
					return;
				int priority = thread.getPriority() - 1;
				if (priority < Thread.MIN_PRIORITY)
					return;
				thread.setPriority(priority);
				refreshAllListeners();
			}
		});

		btn = new ToolItem(toolbar, SWT.PUSH);
		btn.setText("&Interrupt");
		btn.setToolTipText("Interrupt");
		btn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				Thread thread = getSelectedThread();
				if (thread == null)
					return;
				thread.interrupt();
				refreshAllListeners();
			}
		});

		btn = new ToolItem(toolbar, SWT.PUSH);
		btn.setText("Sto&p");
		btn.setToolTipText("Stop");
		btn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				Thread thread = getSelectedThread();
				if (thread == null)
					return;
				thread.stop();
				refreshAllListeners();
			}
		});

		btn = new ToolItem(toolbar, SWT.PUSH);
		btn.setText("&Suspend/Resume");
		btn.setToolTipText("Suspend/Resume");
		btn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				Thread thread = getSelectedThread();
				if (thread == null)
					return;
				long tid = thread.getId();
				ThreadInfo info = threadMXBean.getThreadInfo(tid, 0);
				if (info == null || info.isSuspended())
					thread.resume();
				else
					thread.suspend();
				refreshAllListeners();
			}
		});

		table = new Table(this, SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
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
				refreshAllListeners();
			}
		};

		TableColumn col;
		col = columnTID = new TableColumn(table, SWT.NONE);
		col.setText("TID");
		col.addSelectionListener(columnListener);
		col.pack();
		int increaseColumnWidth = col.getWidth();
		table.setSortColumn(col);
		col.pack();
		increaseColumnWidth = col.getWidth() - increaseColumnWidth;
//		col.setWidth(col.getWidth() + increaseColumnWidth);

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
		col.setWidth(col.getWidth());

		col = columnIsDaemon = new TableColumn(table, SWT.NONE);
		col.setText("Daemon");
		col.addSelectionListener(columnListener);
		col.pack();
		col.setWidth(col.getWidth());

		col = columnIsInterrupted = new TableColumn(table, SWT.NONE);
		col.setText("Interrupted");
		col.addSelectionListener(columnListener);
		col.pack();
		col.setWidth(col.getWidth());

		col = columnIsSuspended = new TableColumn(table, SWT.NONE);
		col.setText("Suspended");
		col.addSelectionListener(columnListener);
		col.pack();
		col.setWidth(col.getWidth());

		table.setFocus();
		table.setSortColumn(columnTID);
		table.setSortDirection(SWT.UP);
		table.pack();
	}

	private Comparator<ThreadData> columnSort = new Comparator<ThreadData>() {
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
			} else if (sortCol == columnIsDaemon) {
				result = o1.isDaemon == o2.isDaemon ? 0 : o1.isDaemon ? 1 : -1;
			} else if (sortCol == columnIsInterrupted) {
				result = o1.isInterrupted == o2.isInterrupted ? 0 : o1.isInterrupted ? 1 : -1;
			} else if (sortCol == columnIsSuspended) {
				result = o1.isSuspended == o2.isSuspended ? 0 : o1.isSuspended ? 1 : -1;
			} // if (sortCol == columnTID)

			if (result == 0)
				result = o1.id == o2.id ? 0 : o1.id < o2.id ? -1 : 1;
			return result;
		}
	};

	private long getSelectedTID() {
		TableItem selected[] = table.getSelection();
		return selected.length <= 0 ? -1 : ((ThreadData) selected[0].getData()).id;
	}

	private Thread getSelectedThread() {
		long tid = getSelectedTID();
		if (tid == -1)
			return null;
		int activeCount = rootThreadGroup.activeCount();
		Thread threads[] = new Thread[activeCount];
		int maxThreads = rootThreadGroup.enumerate(threads, true);
		for (int i = 0; i < maxThreads; i++) {
			Thread thread = threads[i];
			if (tid == thread.getId())
				return thread;
		}
		return null;
	}

	private static void refreshAllListeners() {
		synchronized (refreshListeners) {
			ThreadData data[] = getThreadData();
			for (int i = refreshListeners.size() - 1; i >= 0; i--) {
				ProcessManager tm = refreshListeners.get(i);
				if (tm.isDisposed()) {
					refreshListeners.remove(i);
					continue;
				}
				tm.updateTable(data);
			}
		}
	}

	private static ThreadData[] getThreadData() {
		long[] tids = threadMXBean.getAllThreadIds();
		Arrays.sort(tids);
		Iterator<Long> tdata = threadData.keySet().iterator();
		for (; tdata.hasNext(); ) {
			Long tid = tdata.next();
			if (Arrays.binarySearch(tids, tid) < 0)
				tdata.remove();
		}

		long curNanoTime = System.nanoTime();
		long div = (numberOfProcessors * (curNanoTime - lastRefreshNanoTime)) / 100;
		if (div == 0)
			div = 1;

		for (long tid : tids) {
			long cpuTime = threadMXBean.getThreadCpuTime(tid);
			ThreadData d = threadData.get(tid);
			if (d == null) {
				d = new ThreadData();
				d.id = tid;
				d.lastCpuTime = cpuTime;
				threadData.put(tid, d);
			}
			if ((cpuTime == -1) || (d.lastCpuTime == -1)) {
				d.cpuUsage = 0;
			} else {
				d.cpuUsage = (int)((cpuTime - d.lastCpuTime) / div);
			}
			d.lastCpuTime = cpuTime;
			ThreadInfo info = threadMXBean.getThreadInfo(tid, 0);
			if (info != null) {
				d.isSuspended = info.isSuspended();
			}
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
			data.isDaemon = thread.isDaemon();
			data.isInterrupted = thread.isInterrupted();
		}

		lastRefreshNanoTime = curNanoTime;
		ThreadData data[] = threadData.values().toArray(new ThreadData[0]);
		return data;
	}

	private void updateTable(ThreadData data[]) {
		long selectedTid = getSelectedTID();
		Arrays.sort(data, columnSort);
		table.setItemCount(data.length);
		TableItem items[] = table.getItems();

		int sel = -1;
		for (int i = 0; i < data.length; i++) {
			ThreadData d = data[i];
			TableItem item = items[i];
			item.setData(d);
			int col = 0;
			item.setText(col++, Long.toString(d.id));
			item.setText(col++, d.threadName);
			item.setText(col++, Long.toString(d.cpuUsage));
			item.setText(col++, d.state);
			item.setText(col++, Integer.toString(d.priority));
			item.setText(col++, d.isDaemon ? "true" : "false");
			item.setText(col++, d.isInterrupted ? "true" : "false");
			item.setText(col++, d.isSuspended ? "true" : "false");
			if (d.id == selectedTid)
				sel = i;
		}
		table.setSelection(sel);
	}

	public static int getRefreshRateMillis() {
		return refreshRateMillis;
	}

	public static void setRefreshRateMillis(int newRefreshRateMillis) {
		refreshRateMillis = newRefreshRateMillis;
	}
}
