package com.slavi.util.file;

import java.io.File;

import org.junit.Test;

import com.slavi.util.file.AbsoluteToRelativePathMaker;
import com.unitTest.TestUtils;

public class AbsoluteToRelativePathMakerTest {
	
	@Test
	public void testAbsoluteToRelativePath() throws Exception {
		String curDir = "c:/data/sample/test";
		String prev = ".." + File.separator;
		String vals[][] = {
				{"c:\\data\\sample\\test\\somefile.txt", "somefile.txt"},	
				{"c:\\data\\sample\\somefile.txt", prev + "somefile.txt"},	
				{"c:\\data\\sample\\test123\\somefile.txt", prev + "test123" + File.separator + "somefile.txt"},
				{"c:/data/sample/somefile.txt", prev + "somefile.txt"},	
				{"c:/data123/sample/somefile.txt", prev + prev + prev + "data123" + File.separator + "sample" + File.separator + "somefile.txt"},	
				{"d:/data/somefile.txt", "D:" + File.separator + "data" + File.separator + "somefile.txt"},
		};
//		File f = new File(curDir);
//		System.out.println("Cur dir is: " + f.getCanonicalPath());
		AbsoluteToRelativePathMaker am = new AbsoluteToRelativePathMaker(curDir);
		for (String[] item : vals) {
			String rel = am.getRelativePath(item[0], false);
			TestUtils.assertEqual("", rel, item[1]);
			File f1 = new File(item[0]);
			File f2 = am.getFullPathFile(rel);
			TestUtils.assertEqual("comparing full paths", f1.getCanonicalPath(), f2.getCanonicalPath());
		}
	}
}
