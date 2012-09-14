package com.test.ui;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.slavi.ui.TaskProgress;
import com.slavi.util.ui.SwtUtil;

public class TestWaitDialog2 {

	public static class TaskList extends ScrolledComposite {

		ArrayList<TaskProgress> tasks = new ArrayList<TaskProgress>();
		Composite container;
		
		public TaskList(Composite parent) {
			super(parent, SWT.H_SCROLL | SWT.V_SCROLL);
			container = new Composite(this, SWT.NONE);
			container.setLayout(new GridLayout());
			setContent(container);
			setExpandHorizontal(true);
			setExpandVertical(true);
		}
		
		public TaskProgress submitTask(final Runnable task) {
			final TaskProgress tp = new TaskProgress(container, SWT.BORDER, task);
			tasks.add(tp);
			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
			gd.minimumHeight = 50;
//			gd.minimumWidth = 100;
			tp.setLayoutData(gd);
			tp.addListener(TaskProgress.TaskFinished, new Listener() {
				public void handleEvent(Event event) {
					System.out.println("Done with " + task);
					Display display = getDisplay();
					if (!display.isDisposed()) {
						display.asyncExec(new Runnable() {
							public void run() {
								tasks.remove(tp);
								tp.dispose();
								setMinSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT));
								layout(true, true);
							}
						});
					}
				}
			});
			
			setMinSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			layout(true, true);
			return tp;
		}
	}
	
	Shell shell;
	Display display;
	
	public TestWaitDialog2() {
		shell = SwtUtil.makeShell(null, SWT.DIALOG_TRIM | SWT.RESIZE);
		shell.setLayout(new FillLayout());
		display = shell.getDisplay();
	}
	
	public void test1() {
		final TaskList tl = new TaskList(shell);

		class Task implements Runnable {
			int id;
			TaskProgress tp;
			Task(int id) {
				this.id = id;
			}
			
			public void run() {
				try {
					System.out.println("Started " + this);
					for (int i = 0; i < 100; i++) {
//						tp.setStatusAndProgressThreadsafe("Task completed " + i + "%", i);
						tp.setProgressThreadsafe(i);
						Thread.sleep(100);
					}
					System.out.println("Finished " + this);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			public String toString() {
				return "task " + id;
			}
		}
		
		TaskProgress tp = tl.submitTask(new Runnable() {
			public void run() {
				for (int i = 0; i < 15; i++) {
					if (display.isDisposed()) {
						break;
					}
					final int id = i;
					display.syncExec(new Runnable() {
						public void run() {
							System.out.println("Submit task " + id);
							Task task = new Task(id);
							TaskProgress tp1 = tl.submitTask(task);
							task.tp = tp1;
							tp1.setTitle("Task " + id);
							tp1.setProgressMaximum(100);
							tp1.startTask();
						}
					});
					try {
						Thread.sleep(1000);
					} catch (Exception e) {
						e.printStackTrace();
						break;
					}
				}
			}
		});
		tp.setTitle("Task spawn");
		tp.startTask();
	}

	public void loop() {
		shell.pack();
		shell.setSize(300, 400);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}
	
	void test2() {
		final ScrolledComposite sc = new ScrolledComposite(shell, SWT.H_SCROLL | SWT.V_SCROLL);
		final Composite parent = new Composite(sc, SWT.NONE);
//		parent.setLayout(new FillLayout(SWT.VERTICAL));
		parent.setLayout(new GridLayout());
		parent.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event) {
				sc.setMinSize(parent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			}
		});
		sc.setContent(parent);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		for (int i = 0; i < 10; i++) {
			Button btn = new Button(parent, SWT.PUSH);
			btn.setText("Button " + i);
			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
			gd.minimumHeight = 50;
			btn.setLayoutData(gd);
		}
	}
	
	public static void main(String[] args) {
		TestWaitDialog2 d = new TestWaitDialog2();
		d.test1();
		d.loop();
		System.out.println("Done.");
	}

}
