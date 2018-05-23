package com.slavi.util.swing;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class TaskProgress extends JPanel {
	public enum TaskState {
		NOTSTARTED,
		RUNNING,
		ABORTING,
		ABORTED,
		FINISHED
	}

	JLabel title, status;
	JButton btnAbort;
	JProgressBar pbar;
	Thread thread;
	Runnable task;
	volatile TaskState taskState;

	public TaskProgress(Runnable task) {
		this.task = task;
		taskState = TaskState.NOTSTARTED;
		thread = null;

		setLayout(new BorderLayout(3, 3));
		add(title = new JLabel(), BorderLayout.NORTH);
		add(pbar = new JProgressBar(), BorderLayout.CENTER);
		add(btnAbort = new JButton("Abort"), BorderLayout.EAST);
		add(status = new JLabel(), BorderLayout.SOUTH);
		
		title.setFont(title.getFont().deriveFont(Font.BOLD));
		btnAbort.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				abortTask();
			}
		});
		setTitle(" ");
		setStatus("Not started");
	}
	
	public String getTitle() {
		return this.title.getText();
	}

	public void setTitle(String title) {
		this.title.setText(title);
	}
	
	public String getStatus() {
		return this.status.getText();
	}

	public void setStatus(String status) {
		this.status.setText(status);
	}
	
	public void setProgress(int taskCompleted) {
		pbar.setValue(taskCompleted);
	}

	public void setProgressMax(int maxValue) {
		if (maxValue > 0) {
			pbar.setIndeterminate(false);
			pbar.setMaximum(maxValue);
		} else
			pbar.setIndeterminate(true);
	}
	
	public synchronized void abortTask() {
		btnAbort.setEnabled(false);
		if (taskState == TaskState.NOTSTARTED) {
			setStatus("Aborted and not run");
			setProgress(-1);
			taskState = TaskState.ABORTED;
		} else if (taskState == TaskState.RUNNING) {
			setStatus("Aborting...");
			setProgress(-1);
			taskState = TaskState.ABORTING;
			if (thread != null)
				thread.interrupt();
		}
		task = null;
	}
	
	public synchronized void startTask() {
		if (taskState == TaskState.NOTSTARTED) {
			thread = new Thread(new Runnable() {
				public void run() {
					try {
						Runnable r = null;
						synchronized (this) {
							r = task;
						}
						if (r != null)
							r.run();
					} catch (Throwable t) {
						taskState = TaskState.ABORTED;
						t.printStackTrace();
					} finally {
						if (taskState == TaskState.RUNNING)
							taskState = TaskState.FINISHED;
					}
				}
			});
			thread.setPriority(Thread.MIN_PRIORITY);
			setStatus("Running...");
			setProgress(-1);
			taskState = TaskState.RUNNING;
			thread.start();
		}
	}

	public boolean isAborted() {
		return (taskState == TaskState.ABORTED) || (taskState == TaskState.ABORTING);
	}
	
	public TaskState getTaskState() {
		return taskState;
	}
}
