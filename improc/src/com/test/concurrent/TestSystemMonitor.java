package com.test.concurrent;

import com.slavi.util.ui.SwtUtil;

public class TestSystemMonitor {

	private void doIt() {
		SwtUtil.openTaskManager(null, true);
		SwtUtil.openWaitDialog(null, "aaaa", new dummy(), 100);
	}

	private static class dummy implements Runnable {
		public void run() {
			try {
				long last = System.currentTimeMillis();
				while (!Thread.currentThread().isInterrupted()) { 
					long now = System.currentTimeMillis();
					if (now - last > 200) {
						int newVal = (int) (Math.random() * 100);
						SwtUtil.activeWaitDialogSetStatus(null, newVal);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}

	public static void main(String[] args) throws InterruptedException {
//		for (int i = 0; i < 4; i++) {
//			Thread t = new Thread(new dummy());
//			t.setPriority(Thread.MIN_PRIORITY);
//			t.start();
//		}
		new TestSystemMonitor().doIt();
	}
}
