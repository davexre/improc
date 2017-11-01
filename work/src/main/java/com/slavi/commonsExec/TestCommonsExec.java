package com.slavi.commonsExec;

import com.slavi.util.file.FileUtil;

public class TestCommonsExec {
	
	void doIt() throws Exception {
		ProcessBuilder pb = new ProcessBuilder("java", "-cp", System.getProperty("java.class.path"), DummyJob.class.getName(), "-t", "1", "-n", "10000");
		//ProcessBuilder pb = new ProcessBuilder("/home/slavian/S/git/webscrap/webscrap/src/test/java/org/velobg/webscrap/test/myBashCmd.sh", "-cp", System.getProperty("java.class.path"), DummyJob.class.toString());
		
		final Process p = pb.start();
		FileUtil.copyStreamAsynch(p.getInputStream(), System.out, true, true);
		FileUtil.copyStreamAsynch(p.getErrorStream(), System.err, true, true);
//		FileUtil.copyStreamAsynch(new ByteArrayInputStream("This is a dummy string".getBytes()), p.getOutputStream(), true, true);
		
		Thread thread1 = new Thread(new Runnable() {
			public void run() {
				try {
					System.out.println("Sleep for 5 sec");
					Thread.sleep(5000);
					System.out.println("killing process");
					p.destroy();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		thread1.start();
		Thread thread2 = new Thread(new Runnable() {
			public void run() {
				System.out.println("Process should be killed");
				while (true)
					try {
						//p.exitValue();
						System.out.println(Integer.toString(p.waitFor()));
					} catch (IllegalThreadStateException | InterruptedException e) {
						//e.printStackTrace();
						break;
					}
				System.out.println("Wait finished");
			}
		});
		thread2.start();
		p.getOutputStream().close();
		
		System.out.println("Join thread 1");
		thread1.join();
		System.out.println(thread2.isDaemon());
		System.out.println("Join thread 2");
		thread2.join();
		System.out.println("Ready to quit");
	}

	public static void main(String[] args) throws Exception {
		new TestCommonsExec().doIt();
		System.out.println("Done.");
	}
}
