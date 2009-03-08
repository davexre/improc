package com.slavi.improc.ui;

import java.util.ArrayList;

import com.slavi.improc.KeyPointPairBigTree;
import com.slavi.improc.KeyPointPairList;
import com.slavi.improc.PanoList;
import com.slavi.improc.PanoPairList;
import com.slavi.improc.myadjust.MyAdjustTask;
import com.slavi.improc.myadjust.MyAdjustTask2;
import com.slavi.util.file.AbsoluteToRelativePathMaker;
import com.slavi.util.file.FindFileIterator;
import com.slavi.util.ui.SwtUtil;

public class Improc2 {
	public void doTheJob() throws Exception {
		Settings settings = Settings.getSettings();
		if (settings == null)
			return;
		AbsoluteToRelativePathMaker imagesRoot = new AbsoluteToRelativePathMaker(settings.imagesRootStr);
		AbsoluteToRelativePathMaker keyPointFileRoot = new AbsoluteToRelativePathMaker(settings.keyPointFileRootStr);
//		AbsoluteToRelativePathMaker keyPointPairFileRoot = new AbsoluteToRelativePathMaker(settings.keyPointPairFileRootStr);
		
		FindFileIterator imagesIterator = FindFileIterator.makeWithWildcard(imagesRoot.getFullPath("*.jpg"), true, true);
		ArrayList<String> images = SwtUtil.openWaitDialog("Searching for images", new EnumerateImageFiles(imagesIterator), -1);
		SwtUtil.openWaitDialog("Generating key point files", 
				new GenerateKeyPointFiles(images, imagesRoot, keyPointFileRoot), images.size() - 1);
			
		System.out.println("---------- Generating key point BIG tree");
		KeyPointPairBigTree bigTree = SwtUtil.openWaitDialog("Generating key point BIG tree", 
				new GenerateKeyPointPairBigTree(images, imagesRoot, keyPointFileRoot), 
				images.size() - 1);
		System.out.println("Tree size  : " + bigTree.getSize());
		System.out.println("Tree depth : " + bigTree.getTreeDepth());
		System.out.println("Tree size          : " + bigTree.getSize());
		System.out.println("Tree depth         : " + bigTree.getTreeDepth());
		System.out.println("Perfect tree depth : " + bigTree.getPerfectTreeDepth());
		
		System.out.println("---------- Generating key point pairs from BIG tree");
		ArrayList<KeyPointPairList> kppl = SwtUtil.openWaitDialog("Generating key point pairs from BIG tree", 
				new GenerateKeyPointPairsFromBigTree(bigTree),
				images.size() - 1);
		images = null;
		bigTree = null;
/*
		System.out.println("---------- Executing MyAdjust");
		SwtUtil.openWaitDialog("Executing MyAdjust", 
				new MyAdjustTask(kppl), 1);
*/
		
/*
		System.out.println("---------- Keypoint pairs results");
		for (KeyPointPairList l : kppl) {
			System.out.println(Integer.toString(l.items.size()) + "\t" + 
					l.source.imageFileStamp.getFile().getName() + "\t" + 
					l.target.imageFileStamp.getFile().getName());
		}
		String fname = Const.tempDir + "keypoints.txt";
		PrintStream out = new PrintStream(fname);
		for (KeyPointPairList l : kppl) {
			String sourceFile = l.source.imageFileStamp.getFile().getName();
			String targetFile = l.target.imageFileStamp.getFile().getName();
			for (KeyPointPair p : l.items.values()) {
				out.println(
						Double.toString(p.distanceToNearest) + "\t" +
						Double.toString(p.distanceToNearest2) + "\t" +
						Integer.toString(p.sourceSP.getNumberOfNonZero()) + "\t" +
						Integer.toString(p.targetSP.getNumberOfNonZero()) + "\t" +
						Integer.toString(p.getUnmatchingCount()) + "\t" +
						Integer.toString(p.getMaxDifference()) + "\t" +
						sourceFile + "\t" +
						targetFile);
			}
		}		
		out.close();
*/
		
		System.out.println("---------- Generating pano pairs from key point pairs");
		PanoList panoList = SwtUtil.openWaitDialog("Generating pano pairs from key point pairs", 
				new GeneratePanoPairFromBigTree(kppl),
				kppl.size() - 1);

		System.out.println("---------- Pano pairs results");
		for (PanoPairList ppl : panoList.items) {
			System.out.println(Integer.toString(ppl.items.size()) + "\t" + 
					ppl.sourceImage + "\t" + ppl.targetImage);
		}
		
		System.out.println("---------- Executing MyAdjust");
		SwtUtil.openWaitDialog("Executing MyAdjust", 
				new MyAdjustTask2(panoList), 1);
		
		System.out.println("---------- Generating panorama images");
		SwtUtil.openWaitDialog("Generating panorama images", 
				new GeneratePanoramas(panoList, keyPointFileRoot), panoList.items.size());

		System.out.println("Done.");
	}
	
	public static void main(String[] args) throws Exception {
		Improc2 application = new Improc2();
		application.doTheJob();
	}
}
