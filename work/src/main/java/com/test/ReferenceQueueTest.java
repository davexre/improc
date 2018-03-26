package com.test;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.concurrent.atomic.AtomicInteger;

public class ReferenceQueueTest {

	static class MyObj {
		static AtomicInteger count = new AtomicInteger();
		
		String id = Integer.toString(count.incrementAndGet());
		
		public String toString() {
			return id;
		}
	}
	
	ReferenceQueue<MyObj> rq = new ReferenceQueue<>();
	
	public void doIt(String[] args) throws Exception {
		MyObj o = new MyObj();
		Reference<? extends MyObj> ref = new SoftReference(o, rq);
		o = null;
		System.gc();
		o = ref.get();
		System.out.println(o);
		o = null;
		ref = rq.poll();
		if (ref != null) {
			o = ref.get();
			System.out.println(o);
		}
	}

	public static void main(String[] args) throws Exception {
		new ReferenceQueueTest().doIt(args);
		System.out.println("Done.");
	}
}
