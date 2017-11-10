package com.slavi.util.file;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.concurrent.CompletableFuture;

public class TestCopyStreamAsynch {

	void doIt() throws Exception {
		PipedInputStream pis = new PipedInputStream();
		PipedOutputStream pos = new PipedOutputStream(pis);
		
		CompletableFuture cf1 = CompletableFuture.runAsync(() -> {
			PrintStream out = new PrintStream(pos);
			for (int i = 0; i < 10; i++) {
				String line = "I " + i;
				out.println(line);
//				System.out.println("Sent " + line);
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
				}
			}
			out.close();
			System.out.println("Sender done.");
		});
		
		CompletableFuture cf2 = CompletableFuture.runAsync(() -> {
			BufferedReader in = new BufferedReader(new InputStreamReader(pis));
			try {
				String line;
				while ((line = in.readLine()) != null) {
					System.out.println("Got  " + line);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Reader done.");
		});

		cf1.join();
		cf2.join();
	}

	public static void main(String[] args) throws Exception {
		new TestCopyStreamAsynch().doIt();
		System.out.println("Done.");
	}
}
