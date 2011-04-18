package com.slavi.util.file;

import java.io.File;

import org.junit.Test;

import com.slavi.TestUtils;
import com.slavi.util.file.AbsoluteToRelativePathMaker;

public class AbsoluteToRelativePathMakerTest {
	
	@Test
	public void testAbsoluteToRelativePath() throws Exception {
		String vals[][] = {
				// Root dir, File location, expected relative path
				{"c:/data/sample/test", "c:\\data\\sample\\test\\somefile.txt", "somefile.txt"},	
				{"c:/data/sample/test", "c:\\data\\sample\\somefile.txt", "../somefile.txt"},	
				{"c:/data/sample/test", "c:\\data\\sample\\test123\\somefile.txt", "../test123/somefile.txt"},
				{"c:/data/sample/test", "c:/data/sample/somefile.txt", "../somefile.txt"},	
				{"c:/data/sample/test", "c:/data123/sample/somefile.txt", "../../../data123/sample/somefile.txt"},	
				{"c:/data/sample/test", "d:/data/somefile.txt", "D:/data/somefile.txt"},
				{"/var/data/", "/var/data/stuff/xyz.dat", "stuff/xyz.dat"},
				{"/a/x/y/", "/a/b/c", "../../b/c"},
				{"/m/n/o/a/x/y/", "/m/n/o/a/b/c", "../../b/c"},
				{"C:\\Windows\\Speech\\Common\\sapisvr.exe", "C:\\Windows\\Boot\\Fonts\\chs_boot.ttf", "../../../Boot/Fonts/chs_boot.ttf"},
				{"C:\\Windows\\Speech\\Common", "C:\\Windows\\Boot\\Fonts\\chs_boot.ttf", "../../Boot/Fonts/chs_boot.ttf"},
				{"C:\\Java\\workspace\\AcceptanceTests\\Standard test data\\geo\\", "D:\\sources\\recovery\\RecEnv.exe", "D:/sources/recovery/RecEnv.exe"}
		};
		for (String[] item : vals) {
			AbsoluteToRelativePathMaker am = new AbsoluteToRelativePathMaker(item[0]);
			String rel = am.getRelativePath(item[1], false);
			TestUtils.assertEqualIgnoreCase("", rel, item[2]);
			File f1 = new File(item[1]);
			File f2 = am.getFullPathFile(rel);
			TestUtils.assertEqualIgnoreCase("comparing full paths", f1.getAbsolutePath(), f2.getAbsolutePath());
		}
	}
}
