package com.slavi.improc.ui;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

import com.slavi.improc.PanoList;
import com.slavi.improc.PanoPairList;
import com.slavi.util.file.AbsoluteToRelativePathMaker;
import com.slavi.util.ui.SwtUtil;

public class GeneratePanoPairFiles implements Callable<PanoList> {
	List<String> images;
	AbsoluteToRelativePathMaker imagesRoot;
	AbsoluteToRelativePathMaker keyPointFileRoot;
	AbsoluteToRelativePathMaker keyPointPairFileRoot;

	public GeneratePanoPairFiles(List<String> images,
			AbsoluteToRelativePathMaker imagesRoot,
			AbsoluteToRelativePathMaker keyPointFileRoot,
			AbsoluteToRelativePathMaker keyPointPairFileRoot) {
		this.images = images;
		this.imagesRoot = imagesRoot;
		this.keyPointFileRoot = keyPointFileRoot;
		this.keyPointPairFileRoot = keyPointPairFileRoot;
	}
	
	public PanoList call() throws Exception {
		PanoList result = new PanoList();
		for (int i = 0, pairsCount = 0; i < images.size(); i++) {
			for (int j = i + 1; j < images.size(); j++, pairsCount++) {
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}
				String fileName1 = images.get(i); 
				String fileName2 = images.get(j);
				File image1 = new File(fileName1);
				File image2 = new File(fileName2);
				File kpplFile = PanoPairList.getFile(imagesRoot, keyPointPairFileRoot, image1, image2); 
				String statusMessage = (i + 1) + "/" + (j + 1) + "/" + images.size() + " " + kpplFile.getPath();
				System.out.print(statusMessage);
				SwtUtil.activeWaitDialogSetStatus(statusMessage, pairsCount);
				PanoPairList ppl = PanoPairList.readPanoPairFile(imagesRoot, keyPointFileRoot, keyPointPairFileRoot, image1, image2);
				result.addItem(ppl);
				System.out.println(" (" + ppl.items.size() + " pano paris)");
			}
		}
		return result;
	}
}
