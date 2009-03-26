package com.slavi.improc.ui;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.slavi.improc.KeyPointBigTree;
import com.slavi.improc.KeyPointPairList;
import com.slavi.improc.myadjust.CalculatePanoramaParams;
import com.slavi.improc.myadjust.ValidateKeyPointPairList;
import com.slavi.util.file.AbsoluteToRelativePathMaker;
import com.slavi.util.file.FindFileIterator;
import com.slavi.util.ui.SwtUtil;

public class Improc {
	
	public void doTheJob(ExecutorService exec) throws Exception {
		Settings settings = Settings.getSettings();
		if (settings == null)
			return;
		AbsoluteToRelativePathMaker imagesRoot = new AbsoluteToRelativePathMaker(settings.imagesRootStr);
		AbsoluteToRelativePathMaker keyPointFileRoot = new AbsoluteToRelativePathMaker(settings.keyPointFileRootStr);
		
		FindFileIterator imagesIterator = FindFileIterator.makeWithWildcard(imagesRoot.getFullPath("*.jpg"), true, true);
		ArrayList<String> images = SwtUtil.openWaitDialog("Searching for images", new EnumerateImageFiles(imagesIterator), -1);
		SwtUtil.openWaitDialog("Generating key point files", 
				new GenerateKeyPointFiles(exec, images, imagesRoot, keyPointFileRoot), images.size() - 1);
		
		System.out.println("---------- Generating key point BIG tree");
		KeyPointBigTree bigTree = SwtUtil.openWaitDialog("Generating key point BIG tree", 
				new GenerateKeyPointPairBigTree(exec, images, imagesRoot, keyPointFileRoot), 
				images.size() - 1);
		System.out.println("Tree size  : " + bigTree.getSize());
		System.out.println("Tree depth : " + bigTree.getTreeDepth());
		System.out.println("Tree size          : " + bigTree.getSize());
		System.out.println("Tree depth         : " + bigTree.getTreeDepth());
		System.out.println("Perfect tree depth : " + bigTree.getPerfectTreeDepth());
		
		System.out.println("---------- Generating key point pairs from BIG tree");
		ArrayList<KeyPointPairList> kppl = SwtUtil.openWaitDialog("Generating key point pairs from BIG tree", 
				new GenerateKeyPointPairsFromBigTree(exec, bigTree),
				images.size() - 1);
		images = null;
		bigTree = null;

		System.out.println("---------- Validating key point pairs");
		ArrayList<KeyPointPairList> validkppl = SwtUtil.openWaitDialog("Validating key point pairs", 
				new ValidateKeyPointPairList(exec, kppl),
				-1);
		kppl = null;

/*		if (true) 
			return;
*/		
		
		System.out.println("---------- Generating panorama images");
		SwtUtil.openWaitDialog("Generating panorama images", 
				new CalculatePanoramaParams(validkppl, keyPointFileRoot), -1);
		
		System.out.println("Done.");
	}
	
	public static void main(String[] args) throws Exception {
		Runtime runtime = Runtime.getRuntime();
		int numberOfProcessors = runtime.availableProcessors();
		ExecutorService exec = Executors.newFixedThreadPool(numberOfProcessors + 1);

		try {
			Improc application = new Improc();
			application.doTheJob(exec);
		} finally {
			exec.shutdown();
		}
	}
}
