package com.slavi.util.file;

import java.io.File;

import org.junit.Test;

public class AbsoluteToRelativePathMakerTest {
	
	public static void assertEqualIgnoreCase(String msg, String strA, String strB) {
		if ((strA == null) && (strB == null))
			return;
		if ((strA != null) && strA.equalsIgnoreCase(strB))
			return;
		System.out.println("Strings not equal: " + msg);
		System.out.println(strA);
		System.out.println(strB);
		throw new RuntimeException("Failed");
	}

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
			String rel = am.getRelativePath(item[1]);
			assertEqualIgnoreCase("", rel, item[2]);
			File f1 = new File(item[1]);
			File f2 = am.getFullPathFile(rel);
			assertEqualIgnoreCase("comparing full paths", f1.getAbsolutePath(), f2.getAbsolutePath());
		}
	}
}
