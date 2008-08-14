package com.slavi.improc.test;

import java.io.File;

import com.slavi.improc.KeyPointList;
import com.slavi.util.Const;
import com.slavi.util.Marker;

public class TestKeyPointList {
	public static void main(String[] args) throws Exception {
		File image = new File(Const.sourceImage);
		System.out.println("Source image is " + Const.sourceImage);
		
		Marker.mark("Single threaded");
		KeyPointList l1 = KeyPointList.buildKeyPointFileSingleThreaded(image);
		Marker.release();
		Marker.mark("Multithreaded");
		KeyPointList l2 = KeyPointList.buildKeyPointFileMultiThreaded(image);
		Marker.release();
		
		System.out.println("----------------------- Comparing -----------------");
		l1.compareToList(l2);

	}
}
