package com.slavi.improc.ui;

import java.util.ArrayList;

import com.slavi.improc.KeyPointPairBigTree;
import com.slavi.improc.KeyPointPairList;
import com.slavi.improc.PanoList;
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
		
//		System.out.println("---------- Find image pairs in BIG tree");
//		SwtUtl.openWaitDialog("Find image pairs in BIG tree", 
//				new FindImagePairsInBigTree(bigTree), 
//				bigTree.getSize());

		System.out.println("---------- Generating key point pairs from BIG tree");
		ArrayList<KeyPointPairList> kppl = SwtUtil.openWaitDialog("Generating key point pairs from BIG tree", 
				new GenerateKeyPointPairsFromBigTree(bigTree),
				images.size() - 1);
		images = null;
		bigTree = null;
		
		System.out.println("---------- Generating pano pairs from key point pairs");
		PanoList panoList = SwtUtil.openWaitDialog("Generating pano pairs from key point pairs", 
				new GeneratePanoPairFromBigTree(kppl),
				kppl.size() - 1);

		System.out.println("---------- Generating panorama (PTO) files");
		SwtUtil.openWaitDialog("Generating panorama (PTO) files", 
				new GeneratePanoramaFiles(panoList, keyPointFileRoot), panoList.items.size());
		
		System.out.println("Done.");
	}
	
	public static void main(String[] args) throws Exception {
		Improc2 application = new Improc2();
		application.doTheJob();
	}
}
