package com.slavi.improc.old.test;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.slavi.improc.KeyPoint;
import com.slavi.improc.KeyPointList;
import com.slavi.improc.KeyPointListSaver;
import com.slavi.improc.KeyPointListSaver.ListenerImpl;
import com.slavi.improc.old.singletreaded.DImageMap;
import com.slavi.improc.old.singletreaded.DLoweDetector;
import com.slavi.util.Const;
import com.slavi.util.Marker;
import com.slavi.util.file.AbsoluteToRelativePathMaker;

public class TestKeyPointList {
	public static KeyPointList buildKeyPointFileSingleThreaded(File image) throws Exception {
		DImageMap img = new DImageMap(image);
		KeyPointList result = new KeyPointList();
		result.imageSizeX = img.getSizeX();
		result.imageSizeY = img.getSizeY();
		DLoweDetector d = new DLoweDetector();
		d.hook = new ListenerImpl(result);
		d.DetectFeatures(img, 3, 32);
		return result;
	}
	
	public static void main2(String[] args) throws Exception {
		File image = new File(Const.sourceImage);
		System.out.println("Source image is " + Const.sourceImage);
		
		Marker.mark("Single threaded");
		KeyPointList l1 = buildKeyPointFileSingleThreaded(image);
		Marker.release();
		Marker.mark("Multithreaded");
		Runtime runtime = Runtime.getRuntime();
		int numberOfProcessors = runtime.availableProcessors();
		ExecutorService exec = Executors.newFixedThreadPool(numberOfProcessors + 1);
		try {
			KeyPointList l2 = KeyPointListSaver.buildKeyPointFileMultiThreaded(exec, image);
			Marker.release();
			
			System.out.println("----------------------- Comparing -----------------");
			l1.compareToList(l2);
		} finally {
			exec.shutdown();
		}
	}
	
	public static void main(String[] args) throws Exception {
		String imagesRootStr = "C:/Users/S/ImageProcess/Images/Image data/Perperikon/";
		String kpRoot = "C:/Users/S/ImageProcess/work/";
		String images[] = {"HPIM0336.JPG", "HPIM0337.JPG"};
		
		Runtime runtime = Runtime.getRuntime();
		int numberOfProcessors = runtime.availableProcessors();
		ExecutorService exec = Executors.newFixedThreadPool(numberOfProcessors + 1);
		try {
			AbsoluteToRelativePathMaker imagesRoot = new AbsoluteToRelativePathMaker(imagesRootStr);
			AbsoluteToRelativePathMaker keyPointFileRoot = new AbsoluteToRelativePathMaker(kpRoot);
			KeyPointList l1 = KeyPointListSaver.readKeyPointFile(exec, imagesRoot, keyPointFileRoot, new File(imagesRootStr + images[0]));
			KeyPointList l2 = KeyPointListSaver.readKeyPointFile(exec, imagesRoot, keyPointFileRoot, new File(imagesRootStr + images[1]));
			
			int equalsCount = 0;
			for (KeyPoint p2 : l2.items) {
				for (KeyPoint p1 : l1.items) {
					if (p1 == p2)
						continue;
					if (p1.equalsFeatureVector(p2)) {
						equalsCount++;
					}
				}			
			}
			System.out.println(equalsCount);
		} finally {
			exec.shutdown();
		}
	}
}
