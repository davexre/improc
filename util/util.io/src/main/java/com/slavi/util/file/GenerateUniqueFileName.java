package com.slavi.util.file;

import java.io.File;

public class GenerateUniqueFileName {
	private static GenerateUniqueFileName instance = null;
	
	public synchronized static GenerateUniqueFileName getInstance() {
		if (instance == null)
			instance = new GenerateUniqueFileName();
		return instance;
	}
	
	public boolean fileExists(String fileName) {
		return new File(fileName).exists();
	}
	
	public String getUniqueFileName(String baseFileName) {
		if (!fileExists(baseFileName))
			return baseFileName;
		File basef = new File(baseFileName);
		File parent = basef.getParentFile();
		String path = parent == null ? "" : parent.getPath();
		String name = basef.getName();
		int lastdot = name.lastIndexOf(".");
		String ext;
		if (lastdot >= 0) {
			ext = name.substring(lastdot);
			name = name.substring(0, lastdot);
		} else {
			ext = "";
		}
		String result;
		int count = 1;
		do {
			result = path + File.separator + name + "_" + (count++) + ext;
		} while (fileExists(result));
		return result;
	}
}
