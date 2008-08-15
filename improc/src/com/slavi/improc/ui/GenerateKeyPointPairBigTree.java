package com.slavi.improc.ui;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

import com.slavi.improc.KeyPoint;
import com.slavi.improc.KeyPointList;
import com.slavi.improc.KeyPointPairBigTree;
import com.slavi.util.file.AbsoluteToRelativePathMaker;
import com.slavi.util.ui.SwtUtl;

public class GenerateKeyPointPairBigTree implements Callable<KeyPointPairBigTree> {

	List<String> images;
	AbsoluteToRelativePathMaker imagesRoot;
	AbsoluteToRelativePathMaker keyPointFileRoot;

	public GenerateKeyPointPairBigTree(List<String> images,
			AbsoluteToRelativePathMaker imagesRoot,
			AbsoluteToRelativePathMaker keyPointFileRoot) {
		this.images = images;
		this.imagesRoot = imagesRoot;
		this.keyPointFileRoot = keyPointFileRoot;
	}
	
	public KeyPointPairBigTree call() throws Exception {
		KeyPointPairBigTree result = new KeyPointPairBigTree();
		for (int i = 0; i < images.size(); i++) {
			String image = images.get(i); 
			String statusMessage = (i + 1) + "/" + images.size() + " " + image;
			System.out.println(statusMessage);
			SwtUtl.activeWaitDialogSetStatus(statusMessage, i);
			KeyPointList l = KeyPointList.readKeyPointFile(imagesRoot, keyPointFileRoot, new File(image));
			result.keyPointLists.add(l);
			for (KeyPoint kp : l.kdtree) {
				result.kdtree.add(kp);
			}
		}
		SwtUtl.activeWaitDialogSetStatus("Balancing the tree", 0);
		System.out.println("Tree size        : " + result.kdtree.getSize());
		System.out.println("Tree depth before: " + result.kdtree.getTreeDepth());
		result.kdtree.balanceIfNeeded();
		System.out.println("Tree depth after : " + result.kdtree.getTreeDepth());
		
		return result;
	}
}
