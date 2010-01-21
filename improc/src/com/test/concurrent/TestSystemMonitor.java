package com.test.concurrent;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.slavi.util.ui.SwtUtil;

public class TestSystemMonitor {

	private void doIt() {
		Shell shell = SwtUtil.makeShell(null);
		shell.setBounds(0, 0, 0, 0);
		shell.open();
		Display display = shell.getDisplay();
		SwtUtil.openTaskManager(shell, true);
		SwtUtil.openWaitDialog(shell, "aaaa", new dummy(), -1);
		while (SwtUtil.isTaskManagerOpened()){
			@SuppressWarnings("unused")
			double d[] = new double[4000];
			if(!display.readAndDispatch())
				display.sleep();
		}
//		shell.close();
	}

	private static class dummy implements Runnable {
		public void run() {
			try {
				while (true) { }
//				Thread.sleep((int) (Math.random() * 5000));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}

	public static void main(String[] args) throws InterruptedException {
		for (int i = 0; i < 4; i++) {
			Thread t = new Thread(new dummy());
			t.setPriority(Thread.MIN_PRIORITY);
			t.start();
		}
		new TestSystemMonitor().doIt();
	}
}
