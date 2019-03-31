package com.slavi.util.concurrent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import com.slavi.util.Util;

public class CloseableBlockingQueueTest {

	void doIt1() throws Exception {
		CloseableBlockingQueue<Integer> qu = new CloseableBlockingQueue<>(2);
		Thread thread = new Thread(() -> {
			System.out.println("Consumer started");
			int counter = 0;
			try {
				Integer i;
				while ((i = qu.take()) != null) {
					if (i != counter)
						throw new Error("Message out of order: got " + i + ", expected " + counter);
					System.out.println("Consumed message " + i + " capacity " + qu.getCapacity() + " size " + qu.size());
					Thread.sleep(1000);
					counter++;
				}
			} catch (Throwable t) {
				t.printStackTrace();
				qu.close();
			}
			System.out.println("Consumer stopped");
		});
		thread.setDaemon(false);
		thread.start();

		for (int i = 0; i < 10; i++) {
			System.out.println("Produced message " + i);
			if (i == 4) {
				qu.setCapacity(3);
			}
			if (i == 7) {
				qu.setCapacity(2);
			}
			qu.put(i);
		}

		System.out.println("Closing queue");
		qu.close();
		thread.join();
	}

	void doIt() throws Exception {
		CloseableBlockingQueue<Integer> qu = new CloseableBlockingQueue<>(2);
		int nThreads = 4;
		ExecutorService exec = Util.newBlockingThreadPoolExecutor(nThreads);
		TaskSet task = new TaskSet(exec);
		int messagesToSend = 20;

		task.add(() -> {
			for (int i = 0; i < messagesToSend; i++) {
				System.out.println("Produced message " + i);
				if (i == 4) {
					qu.setCapacity(3);
				}
				if (i == 7) {
					qu.setCapacity(2);
				}
				qu.put(i);
			}
			System.out.println("Closing queue");
			qu.close();
			return null;
		});

		Map<Integer, String> processed = new ConcurrentHashMap();

		for (int tt = 1; tt < nThreads; tt++) {
			task.add(() -> {
				System.out.println("Consumer started");
				try {
					Integer i;
					while ((i = qu.take()) != null) {
						if (processed.containsKey(i))
							throw new Error("Message already processed: got " + i);
						System.out.println("Consumed message " + i + " capacity " + qu.getCapacity() + " size " + qu.size());
						processed.put(i, "");
						Thread.sleep(100);
					}
				} catch (Throwable t) {
					t.printStackTrace();
					qu.close();
				}
				System.out.println("Consumer stopped");
				return null;
			});
		}

		task.run().get();
		System.out.println("All done.");
		if (processed.size() != messagesToSend)
			throw new Exception("Did not receive all messages.");
		exec.shutdownNow();
	}


	public static void main(String[] args) throws Exception {
		new CloseableBlockingQueueTest().doIt();
		System.out.println("Done.");
	}
}
