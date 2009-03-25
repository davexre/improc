package com.slavi.improc.ui;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.slavi.improc.KeyPointBigTree;
import com.slavi.improc.KeyPointList;
import com.slavi.improc.KeyPointPairList;
import com.slavi.improc.myadjust.MyAdjustTask;
import com.slavi.improc.myadjust.MyGeneratePanoramas;
import com.slavi.improc.myadjust.MyPanoPairTransformLearner3;
import com.slavi.improc.myadjust.MyPanoPairTransformer3;
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
		
		for (KeyPointList image : bigTree.keyPointLists) {
			image.rx = 0.0;
			image.ry = 0.0;
			image.rz = 0.0;
			image.cameraOriginX = image.imageSizeX / 2.0;
			image.cameraOriginY = image.imageSizeY / 2.0;
			image.cameraScale = 1.0 / Math.max(image.imageSizeX, image.imageSizeY);
			image.scaleZ = MyPanoPairTransformLearner3.defaultCameraFOV_to_ScaleZ;
		}		
		
		System.out.println("---------- Generating key point pairs from BIG tree");
		ArrayList<KeyPointPairList> kppl = SwtUtil.openWaitDialog("Generating key point pairs from BIG tree", 
				new GenerateKeyPointPairsFromBigTree(exec, bigTree),
				images.size() - 1);
		images = null;
		bigTree = null;

//		for (KeyPointPairList l : kppl) {
//			for (KeyPointPair p : l.items.values()) {
//				System.out.println(
//						MathUtil.d4(p.distanceToNearest) + "\t" + 
//						MathUtil.d4(p.distanceToNearest2) + "\t" + 
//						MathUtil.d4(MyPanoPairTransformLearner3.getWeight(p)));
//				int unmatching = p.getUnmatchingCount();
//				p.weight = 1.0 / (unmatching + 1);
//				p.bad = unmatching > 10;

//				p.weight = p.distanceToNearest < 1 ? 1.0 : 10 / p.distanceToNearest;
//				p.weight = p.weight < 1 ? 1 : 1/p.weight;
//				p.bad = p.distanceToNearest > 1000;
//				p.bad = p.distanceToNearest > maxDist;
//			}
//		}		

		System.out.println("---------- Validating key point pairs");
		ArrayList<KeyPointPairList> validkppl = SwtUtil.openWaitDialog("Validating key point pairs", 
				new ValidateKeyPointPairList(exec, kppl),
				-1);
		kppl = null;
/*		if (true) 
			return;
*/		
		
		System.out.println("---------- Executing MyAdjust");
		MyPanoPairTransformer3 tr = SwtUtil.openWaitDialog("Executing MyAdjust", 
				new MyAdjustTask(validkppl), 1);
		
		System.out.println("---------- Keypoint pairs results");
		for (KeyPointPairList l : validkppl) {
			int goodCount = l.getGoodCount();
			System.out.println(Integer.toString(goodCount) + "/" + Integer.toString(l.items.size()) + "\t" + 
					l.source.imageFileStamp.getFile().getName() + "\t" + 
					l.target.imageFileStamp.getFile().getName());
			if (goodCount < 10) {
				System.out.println("NOT ENOUGH GOOD POINT PAIRS");
			}				
		}
		
		ArrayList<KeyPointList> imagesKPL = new ArrayList<KeyPointList>();
		imagesKPL.add(tr.origin);
		imagesKPL.addAll(tr.images);

		System.out.println("---------- Generating panorama images");
		SwtUtil.openWaitDialog("Generating panorama images", 
				new MyGeneratePanoramas(imagesKPL, validkppl, keyPointFileRoot), -1);
		
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
