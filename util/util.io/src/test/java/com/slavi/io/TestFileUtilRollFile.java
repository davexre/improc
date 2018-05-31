package com.slavi.io;

import java.io.FileOutputStream;

import com.slavi.util.file.FileUtil;

public class TestFileUtilRollFile {
	public static void main(String[] args) throws Exception {
		String fileName = "target/tmp.txt";
		FileUtil.rollFile(fileName);
		new FileOutputStream(fileName).close();
		System.out.println("Done.");
	}
}
