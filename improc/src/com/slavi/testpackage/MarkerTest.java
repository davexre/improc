package com.slavi.testpackage;

import com.slavi.utils.Marker;

public class MarkerTest {
	public static void main(String[] args) throws Exception {
		Marker.mark();
		//double a[] = new double[10000];
		Thread.sleep(1234);
		//a = null;
		//System.gc();
		Marker.release();
	}
}
