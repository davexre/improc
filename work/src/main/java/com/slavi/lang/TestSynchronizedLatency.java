package com.slavi.lang;

import com.slavi.util.Marker;

public class TestSynchronizedLatency {

	volatile boolean _true = true;

	public synchronized void dummySynced() {
		if (_true)
			return;
		System.out.println("Should not happen");
	}

	public void dummyNotSynced() {
		if (_true)
			return;
		System.out.println("Should not happen");
	}

	public void doIt(String[] args) throws Exception {
		int max = 1_000_000_000;
		Marker.mark("NOT synchronized method");
		for (int i = 0; i < max; i++) {
			dummyNotSynced();
		}
		Marker.releaseAndMark("synchronized method");
		for (int i = 0; i < max; i++) {
			dummySynced();
		}
		Marker.release();
	}

	public static void main(String[] args) throws Exception {
		new TestSynchronizedLatency().doIt(args);
		System.out.println("Done.");
	}
}
