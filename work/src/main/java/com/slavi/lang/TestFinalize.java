package com.slavi.lang;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TestFinalize {

	static ConcurrentLinkedQueue<GarbageClass> queue = new ConcurrentLinkedQueue<>();

	public static class GarbageClass {
		int collectionCount = 0;

		public void finalize() {
			System.out.println("GarbageClass.finalize, collectionCount=" + collectionCount);
			if (collectionCount++ < 3) {
				queue.add(this);
			}
		}
	}

	void doIt() throws Exception {
		new GarbageClass();
		for (int i = 0; i < 4; i++) {
			try {
				ArrayList dummy = new ArrayList();
				System.out.println("Queue.poll=" + queue.poll());
				dummy.clear();
				while (dummy.size() >= 0) {
					dummy.add(new byte[1_000_000]);
				}
			} catch (OutOfMemoryError e) {
				System.out.println("OOM i=" + i);
			}
		}

	}

	public static void main(String[] args) throws Exception {
		new TestFinalize().doIt();
		System.out.println("Done.");
	}
}
