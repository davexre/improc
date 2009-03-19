package com.slavi.improc.ui;

import java.util.ArrayList;

import com.slavi.improc.KeyPointList;
import com.slavi.improc.KeyPointPair;
import com.slavi.improc.KeyPointPairBigTree;
import com.slavi.improc.KeyPointPairList;
import com.slavi.improc.myadjust.KeyPointHelmertTransformLearner;
import com.slavi.improc.myadjust.MyAdjustTask;
import com.slavi.improc.myadjust.MyGeneratePanoramas;
import com.slavi.improc.myadjust.MyPanoPairTransformer3;
import com.slavi.util.file.AbsoluteToRelativePathMaker;
import com.slavi.util.file.FindFileIterator;
import com.slavi.util.ui.SwtUtil;

public class Improc {
	public void doTheJob() throws Exception {
		Settings settings = Settings.getSettings();
		if (settings == null)
			return;
		AbsoluteToRelativePathMaker imagesRoot = new AbsoluteToRelativePathMaker(settings.imagesRootStr);
		AbsoluteToRelativePathMaker keyPointFileRoot = new AbsoluteToRelativePathMaker(settings.keyPointFileRootStr);
		
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

		for (KeyPointPairList l : kppl) {
			for (KeyPointPair p : l.items.values()) {
//				System.out.println(
//						MathUtil.d4(p.distanceToNearest) + "\t" + 
//						MathUtil.d4(p.distanceToNearest2) + "\t" + 
//						MathUtil.d4(MyPanoPairTransformLearner3.getWeight(p)));
//				int unmatching = p.getUnmatchingCount();
//				p.weight = 1.0 / (unmatching + 1);
//				p.bad = unmatching > 10;

				p.weight = p.distanceToNearest < 1 ? 1.0 : 10 / p.distanceToNearest;
//				p.weight = p.weight < 1 ? 1 : 1/p.weight;
//				p.bad = p.distanceToNearest > 1000;
//				p.bad = p.distanceToNearest > maxDist;
			}
			
			KeyPointHelmertTransformLearner learner = new KeyPointHelmertTransformLearner(l.items.values());
			boolean res = false;
			learner.calculateOne();
			learner.calculateOne();
			l.leaveGoodElements(200);
			for (int i = 0; i < 2; i++) {
				res = learner.calculateOne();
				if (res) {
					break;
				}
			}
			System.out.println(l.getGoodCount() + "/" + l.items.size() + "\t" +
					l.source.imageFileStamp.getFile().getName() + "\t" + 
					l.target.imageFileStamp.getFile().getName() + "\t");

		}
		
/*		if (true) 
			return;
*/		
		
		System.out.println("---------- Keypoint pairs results");
		for (KeyPointPairList l : kppl) {
			int goodCount = l.getGoodCount();
			System.out.println(Integer.toString(goodCount) + "/" + Integer.toString(l.items.size()) + "\t" + 
					l.source.imageFileStamp.getFile().getName() + "\t" + 
					l.target.imageFileStamp.getFile().getName());
			if (goodCount < 10) {
				System.out.println("NOT ENOUGH GOOD POINT PAIRS");
			}				
		}
		
		System.out.println("---------- Executing MyAdjust");
		MyPanoPairTransformer3 tr = SwtUtil.openWaitDialog("Executing MyAdjust", 
				new MyAdjustTask(kppl), 1);
		
		System.out.println("---------- Keypoint pairs results");
		for (KeyPointPairList l : kppl) {
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
/*		
		for (KeyPointPairList l : kppl) {
			for (KeyPointPair p : l.items.values()) {
				System.out.println(
						p.bad + "\t" +
						p.sourceSP.getNumberOfNonZero() + "\t" +
						p.targetSP.getNumberOfNonZero() + "\t" +
						p.getUnmatchingCount() + "\t" +
						MathUtil.d4(p.discrepancy) + "\t" +
						MathUtil.d4(p.distanceToNearest) + "\t" +
						MathUtil.d4(p.distanceToNearest2) + "\t" +
						MathUtil.d4(MyPanoPairTransformLearner3.getWeight(p))
						);
			}
		}
*/		
		System.out.println("---------- Generating panorama images");
		SwtUtil.openWaitDialog("Generating panorama images", 
				new MyGeneratePanoramas(imagesKPL, kppl, keyPointFileRoot), -1);
		
		System.out.println("Done.");
	}
	
	public static void main(String[] args) throws Exception {
		Improc application = new Improc();
		application.doTheJob();
	}
}
