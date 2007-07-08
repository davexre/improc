package com.slavi.testpackage;

import java.io.File;

import com.slavi.utils.AbsoluteToRelativePathMaker;

public class AbsoluteToRelativePathMakerTest {
	public static void main(String[] args) throws Exception {
		String curDir = "../images/test";
		File f = new File(curDir);
		System.out.println("Cur dir is: " + f.getCanonicalPath());
		AbsoluteToRelativePathMaker am = new AbsoluteToRelativePathMaker(curDir);
		System.out.println(am.getRelativePath("./jsift/asd", false));
		System.out.println(am.getRelativePath("c:\\uzers\\s\\4cd\\Kill.exe"));
		System.out.println(am.getRelativePath("c:\\uZers\\4cd\\Kill.exe", true));
		System.out.println(am.getRelativePath("c:\\uZers\\4cd\\Kill.exe", false));
		System.out.println(am.getRelativePath("d:\\topomaps-bg"));
	}
}
