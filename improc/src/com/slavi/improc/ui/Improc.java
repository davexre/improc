package com.slavi.improc.ui;

import java.util.ArrayList;

import com.slavi.improc.PanoList;
import com.slavi.util.file.AbsoluteToRelativePathMaker;
import com.slavi.util.file.FindFileIterator;
import com.slavi.util.ui.SwtUtl;

public class Improc {
	public void doTheJob() throws Exception {
		Settings settings = Settings.getSettings();
		AbsoluteToRelativePathMaker imagesRoot = new AbsoluteToRelativePathMaker(settings.imagesRootStr);
		AbsoluteToRelativePathMaker keyPointFileRoot = new AbsoluteToRelativePathMaker(settings.keyPointFileRootStr);
		AbsoluteToRelativePathMaker keyPointPairFileRoot = new AbsoluteToRelativePathMaker(settings.keyPointPairFileRootStr);
		
		FindFileIterator imagesIterator = FindFileIterator.makeWithWildcard(imagesRoot.getFullPath("*.jpg"), true, true);
		ArrayList<String> images = SwtUtl.openWaitDialog("Searching for images", new EnumerateImageFiles(imagesIterator), -1);
		SwtUtl.openWaitDialog("Generating key point files", 
				new GenerateKeyPointFiles(images, imagesRoot, keyPointFileRoot), images.size() - 1);
			
		System.out.println("---------- Generating key point pair files");
		SwtUtl.openWaitDialog("Generating key point pair files", 
				new GenerateKeyPointPairFiles(images, imagesRoot, keyPointFileRoot, keyPointPairFileRoot), 
				(images.size() - 1) * images.size() - 1);
		
		System.out.println("---------- Generating pano pair files");
		PanoList panoList = SwtUtl.openWaitDialog("Generating pano pair files", 
				new GeneratePanoPairFiles(images, imagesRoot, keyPointFileRoot, keyPointPairFileRoot), 
				(images.size() - 1) * images.size() - 1);
		
		System.out.println("---------- Generating panorama (PTO) files");
		SwtUtl.openWaitDialog("Generating panorama (PTO) files", 
				new GeneratePanoramaFiles(panoList, keyPointPairFileRoot), panoList.items.size());
		
		System.out.println("Done.");
	}
	
	public static void main(String[] args) throws Exception {
		Improc application = new Improc();
		application.doTheJob();
	}
}
