package com.slavi.improc.test;

import java.io.File;
import java.io.IOException;

import com.slavi.improc.DImageMap;
import com.slavi.improc.singletreaded.DLoweDetector;
import com.slavi.util.Const;
import com.slavi.util.Marker;

public class TestDLoweDetectorSingleThreaded {
	public static void main(String[] args) throws IOException {
		File image = new File(Const.sourceImage);
		System.out.println("Processing image " + image);
		DImageMap img = new DImageMap(image);
		DLoweDetector d = new DLoweDetector();
		Marker.mark("started");
		d.DetectFeatures(img, 3, 32);
		Marker.release();
		System.out.println("DONE.");
	}
}
