package com.slavi.improc.ui;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

import com.slavi.improc.KeyPointPairList;
import com.slavi.util.file.AbsoluteToRelativePathMaker;
import com.slavi.util.ui.SwtUtl;

public class GenerateKeyPointPairFiles implements Callable<Void> {

	List<String> images;
	AbsoluteToRelativePathMaker imagesRoot;
	AbsoluteToRelativePathMaker keyPointFileRoot;
	AbsoluteToRelativePathMaker keyPointPairFileRoot;

	public GenerateKeyPointPairFiles(List<String> images,
			AbsoluteToRelativePathMaker imagesRoot,
			AbsoluteToRelativePathMaker keyPointFileRoot,
			AbsoluteToRelativePathMaker keyPointPairFileRoot) {
		this.images = images;
		this.imagesRoot = imagesRoot;
		this.keyPointFileRoot = keyPointFileRoot;
		this.keyPointPairFileRoot = keyPointPairFileRoot;
	}
	
	public Void call() throws Exception {
		for (int i = 0, pairsCount = 0; i < images.size(); i++) {
			for (int j = i + 1; j < images.size(); j++, pairsCount++) {
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}
				String fileName1 = images.get(i); 
				String fileName2 = images.get(j); 
				File image1 = new File(fileName1);
				File image2 = new File(fileName2);
				File kpplFile = KeyPointPairList.getFile(imagesRoot, keyPointPairFileRoot, image1, image2); 
				String statusMessage = (i + 1) + "/" + (j + 1) + "/" + images.size() + " " + kpplFile.getPath();
				System.out.print(statusMessage);
				SwtUtl.activeWaitDialogSetStatus(statusMessage, pairsCount);
//				KeyPointPairList.updateKeyPointPairFileIfNecessary(imagesRoot, keyPointFileRoot, keyPointPairFileRoot, image1, image2);
				KeyPointPairList kppl = KeyPointPairList.readKeyPointPairFile(imagesRoot, keyPointFileRoot, keyPointPairFileRoot, image1, image2);
				System.out.println(" (" + kppl.items.size() + " key point pairs)");
			}
		}
		return null;
	}
}
