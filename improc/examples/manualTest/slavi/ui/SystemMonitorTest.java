package manualTest.slavi.ui;

import com.slavi.util.ui.SwtUtil;

public class SystemMonitorTest {

	private static class MyTask implements Runnable {
		public void run() {
			try {
				long next = System.currentTimeMillis();
				while (!Thread.currentThread().isInterrupted()) { 
					long now = System.currentTimeMillis();
					if (now <= next) {
						next = now + 200;
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
		SwtUtil.openTaskManager(null, true);
		SwtUtil.openWaitDialog(null, "SwtUtil.activeWaitDialogSetStatus", new MyTask(), 100);
		SwtUtil.closeTaskManager();
	}
}
