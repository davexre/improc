package com.test.util;

import java.io.File;

import com.slavi.util.file.AbsoluteToRelativePathMaker;
import com.slavi.util.file.FileStamp;

public class TestFileStamp {

	public static void main(String[] args) {
		String rootDir = "c:/users/s/imageprocess/images/";
		AbsoluteToRelativePathMaker am = new AbsoluteToRelativePathMaker(rootDir);
		String fullFN = am.getFullPath("SampleData/HPIM0336.JPG");
		String fn = am.getRelativePath(fullFN);
		FileStamp fs = new FileStamp(fn, am);
		File f = fs.getFile();
		System.out.println(fn);
		System.out.println(f.getPath());
	}

}
