package com.slavi.improc.test;

import java.io.File;
import java.io.IOException;

import com.slavi.improc.KeyPointList;
import com.slavi.util.Const;
import com.slavi.util.Marker;

public class TestKeyPointList {
	public static void main(String[] args) throws IOException {
		File kplFile = new File(Const.tempDir + "/ttt.kpl");
		File image = new File(Const.sourceImage);
		System.out.println("Source image is " + Const.sourceImage);
		
		Marker.mark("Single threaded");
		KeyPointList l1 = KeyPointList.buildKeyPointFileSingleThreaded(kplFile, image);
		Marker.release();
		Marker.mark("Multithreaded");
		KeyPointList l2 = KeyPointList.buildKeyPointFileMultiThreaded(kplFile, image);
		Marker.release();
		
		System.out.println("----------------------- Comparing -----------------");
		l1.compareToList(l2);

	}
}
