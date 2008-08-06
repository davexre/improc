package com.test.util;

import java.io.File;

import com.slavi.util.file.FindFileIterator;

public class TestFindFileIterator {
	public static void testFindFileIterator(String filePattern) {
		//FindFileIterator fi = new FindFileIterator(patternStr, true, true);
		FindFileIterator fi = FindFileIterator.makeWithWildcard(filePattern, true, true); 
		File item;
		while ((item = fi.next()) != null) {
			System.out.println(item);
		}
	}
	
	public static void main(String[] args) {
		//File f = new File(FindFileIterator.class.getResource("FindFileIterator.class").getFile());
		//testFindFileIterator(f.getParentFile().getPath() + "/.*class");
		testFindFileIterator("../images/*.jpg");
	}
}
