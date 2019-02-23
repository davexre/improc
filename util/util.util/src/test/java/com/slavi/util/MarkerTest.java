package com.slavi.util;

public class MarkerTest {

	void doIt() throws Exception {
		Marker.mark();
		new String("asdqwe");
		Marker.releaseAndMark("my marker");
		new String("asdqweqweqwe");
		System.gc();
		try (Marker m = new Marker()) {
			new String("asdqweqweqwe");
		}
		Marker.release();
	}

	public static void main(String[] args) throws Exception {
		new MarkerTest().doIt();
		System.out.println("Done.");
	}
}
