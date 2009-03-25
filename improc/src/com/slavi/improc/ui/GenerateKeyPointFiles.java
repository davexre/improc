package com.slavi.improc.ui;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import com.slavi.improc.KeyPointList;
import com.slavi.improc.KeyPointListSaver;
import com.slavi.util.file.AbsoluteToRelativePathMaker;
import com.slavi.util.ui.SwtUtil;

public class GenerateKeyPointFiles implements Callable<Void> {

	ExecutorService exec;	
	List<String> images;
	AbsoluteToRelativePathMaker imagesRoot;
	AbsoluteToRelativePathMaker keyPointFileRoot;

	public GenerateKeyPointFiles(
			ExecutorService exec,
			List<String> images,
			AbsoluteToRelativePathMaker imagesRoot,
			AbsoluteToRelativePathMaker keyPointFileRoot) {
		this.exec = exec;
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
			SwtUtil.activeWaitDialogSetStatus(statusMessage, i);
			KeyPointList kpl = KeyPointListSaver.readKeyPointFile(exec, imagesRoot, keyPointFileRoot, new File(fileName));
			System.out.println(" (" + kpl.items.size() + " key points)");
		}
		return null;
	}
}
