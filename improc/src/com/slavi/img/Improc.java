package com.slavi.img;

import java.io.File;
import java.io.IOException;

import com.slavi.utils.AbsoluteToRelativePathMaker;
import com.slavi.utils.FindFileIterator;

public class Improc {

	public AbsoluteToRelativePathMaker rootImagesDir;
	public AbsoluteToRelativePathMaker rootSPfileDir;

	public static void updateKeyPointFiles(String rootImagesDirStr, String rootSPfileDirStr) {
		FindFileIterator ffi = FindFileIterator.makeWithWildcard(rootImagesDirStr + "/*.jpg", true, true);
		AbsoluteToRelativePathMaker rootImagesDir = new AbsoluteToRelativePathMaker(rootImagesDirStr);
		AbsoluteToRelativePathMaker rootSPfileDir = new AbsoluteToRelativePathMaker(rootSPfileDirStr);
		while (ffi.hasNext()) {
			File image = ffi.next();
			try {
				System.out.println("Processing file " + image.getPath());
				KeyPointList.updateScalePointFileIfNecessary(rootImagesDir, rootSPfileDir, image);
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Failed processing file " + image.getPath());
			}
		}
	}
	
	public static void main(String[] args) {
		updateKeyPointFiles("../images/test", "../images/spfiles/");
		System.out.println("Done!");
	}
}
