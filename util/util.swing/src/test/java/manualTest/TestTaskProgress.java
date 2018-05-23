package manualTest;

import com.slavi.util.swing.SwingUtil;

public class TestTaskProgress {

	public void doIt(String[] args) throws Exception {
		SwingUtil.swingInit();
		
		final int maxIter = 10;
		Runnable task = new Runnable() {
			public void run() {
				try {
					for (int i = 0; i < maxIter; i++) {
						System.out.println("thread step " + i);
						SwingUtil.activeWaitDialogSetStatus("thread step " + i, i + 1);
						Thread.sleep(1000);
					}
				} catch (InterruptedException e) {
					System.out.println("Thread interrupted");
				}
			}
		};
		System.out.println(SwingUtil.openWaitDialog("Title", task, maxIter));
	}

	public static void main(String[] args) throws Exception {
		new TestTaskProgress().doIt(args);
		System.out.println("Done.");
	}
}
