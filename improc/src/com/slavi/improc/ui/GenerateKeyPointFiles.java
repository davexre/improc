package com.slavi.improc.ui;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

import com.slavi.improc.KeyPointList;
import com.slavi.improc.KeyPointListSaver;
import com.slavi.util.file.AbsoluteToRelativePathMaker;
import com.slavi.util.ui.SwtUtl;

public class GenerateKeyPointFiles implements Callable<Void> {

	List<String> images;
	AbsoluteToRelativePathMaker imagesRoot;
	AbsoluteToRelativePathMaker keyPointFileRoot;

	public GenerateKeyPointFiles(List<String> images,
			AbsoluteToRelativePathMaker imagesRoot,
			AbsoluteToRelativePathMaker keyPointFileRoot) {
		this.images = images;
		this.imagesRoot = imagesRoot;
		this.keyPointFileRoot = keyPointFileRoot;
	}
	
	public Void call() throws Exception {
		for (int i = 0; i < images.size(); i++) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			String fileName = images.get(i); 
			String statusMessage = (i + 1) + "/" + images.size() + " " + fileName;
			System.out.print(statusMessage);
			SwtUtl.activeWaitDialogSetStatus(statusMessage, i);
//			KeyPointList.updateKeyPointFileIfNecessary(imagesRoot, keyPointFileRoot, new File(fileName));
			KeyPointList kpl = KeyPointListSaver.readKeyPointFile(imagesRoot, keyPointFileRoot, new File(fileName));
			System.out.println(" (" + kpl.getSize() + " key points)");
		}
		return null;
	}
}
