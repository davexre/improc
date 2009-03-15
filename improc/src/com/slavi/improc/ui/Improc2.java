package com.slavi.improc.ui;

import java.util.ArrayList;
import java.util.HashMap;

import com.slavi.improc.KeyPointList;
import com.slavi.improc.KeyPointPair;
import com.slavi.improc.KeyPointPairBigTree;
import com.slavi.improc.KeyPointPairList;
import com.slavi.improc.PanoList;
import com.slavi.improc.PanoPairList;
import com.slavi.improc.myadjust.MyAdjustTask;
import com.slavi.improc.myadjust.MyGeneratePanoramas;
import com.slavi.improc.myadjust.MyPanoPairTransformer3;
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
//		images = null;
		bigTree = null;
		
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		for (KeyPointPairList l : kppl) {
			for (KeyPointPair p : l.items.values()) {
				int sid = p.sourceSP.id;
				int tid = p.targetSP.id;
				map.put(sid, tid);
				map.put(tid, sid);
			}
		}
		
		System.out.println("---------- Generating key point BIG tree");
		bigTree = SwtUtil.openWaitDialog("Generating key point BIG tree", 
				new GenerateKeyPointPairBigTree(images, imagesRoot, keyPointFileRoot), 
				images.size() - 1);
		System.out.println("Tree size  : " + bigTree.getSize());
		System.out.println("Tree depth : " + bigTree.getTreeDepth());
		System.out.println("Tree size          : " + bigTree.getSize());
		System.out.println("Tree depth         : " + bigTree.getTreeDepth());
		System.out.println("Perfect tree depth : " + bigTree.getPerfectTreeDepth());
		
		System.out.println("---------- Generating key point pairs from BIG tree");
		kppl = SwtUtil.openWaitDialog("Generating key point pairs from BIG tree", 
				new GenerateKeyPointPairsFromBigTree(bigTree),
				images.size() - 1);
		
		for (KeyPointPairList l : kppl) {
			for (KeyPointPair p : l.items.values()) {
				int sid = p.sourceSP.id;
				int tid = p.targetSP.id;
				Integer oldtid = map.get(sid);
				if (tid != oldtid)
					System.out.println(tid + "\t" + oldtid);
			}
		}
		
		if (map != null)
			return;
		
		
		
		
		
/*
		System.out.println("---------- Executing MyAdjust");
		MyPanoPairTransformer3 tr = SwtUtil.openWaitDialog("Executing MyAdjust", 
				new MyAdjustTask(kppl), 1);
		ArrayList<KeyPointList> imagesKPL = new ArrayList<KeyPointList>();
		imagesKPL.add(tr.origin);
//		imagesKPL.addAll(tr.images);
*/
		
		System.out.println("---------- Keypoint pairs results");
		for (KeyPointPairList l : kppl) {
			System.out.println(Integer.toString(l.items.size()) + "\t" + 
					l.source.imageFileStamp.getFile().getName() + "\t" + 
					l.target.imageFileStamp.getFile().getName());
			if (l.getGoodCount() < 10) {
				System.out.println("NOT ENOUGH GOOD POINT PAIRS");
			}				
		}
/*
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
					
//			for (PanoPair pp : ppl.items) {
//				System.out.println(
//						Double.toString(pp.sx) + "\t" +
//						Double.toString(pp.sy) + "\t" +
//						Double.toString(pp.tx) + "\t" +
//						Double.toString(pp.ty) + "\t" +
//						Double.toString(pp.discrepancy) + "\t" +
//						Double.toString(pp.distance1) + "\t" +
//						Double.toString(pp.distance2)
//						);
//			}
//			System.out.println("--------------");
		}

		System.out.println("---------- Executing MyAdjust");
		MyPanoPairTransformer3 tr = SwtUtil.openWaitDialog("Executing MyAdjust", 
				new MyAdjustTask(kppl), 1);
		ArrayList<KeyPointList> imagesKPL = new ArrayList<KeyPointList>();
		imagesKPL.add(tr.origin);
		imagesKPL.addAll(tr.images);
		
/*		
		System.out.println("---------- Executing MyAdjust");
		ArrayList<KeyPointList> imagesKPL = SwtUtil.openWaitDialog("Executing MyAdjust", 
				new MyAdjustTask2(panoList), 1);
*/
		System.out.println("---------- Generating panorama images");
		SwtUtil.openWaitDialog("Generating panorama images", 
				new MyGeneratePanoramas(imagesKPL, kppl, keyPointFileRoot), -1);
		
/*		
		System.out.println("---------- Generating panorama images");
		SwtUtil.openWaitDialog("Generating panorama images", 
				new GeneratePanoramas(panoList, keyPointFileRoot), panoList.items.size());
*/
		System.out.println("Done.");
	}
	
	public static void main(String[] args) throws Exception {
		Improc2 application = new Improc2();
		application.doTheJob();
	}
}
