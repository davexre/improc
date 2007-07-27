package com.slavi.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class AbsoluteToRelativePathMaker {
	private String rootDir;
	
	private ArrayList<String>elements = new ArrayList<String>();
		
	public AbsoluteToRelativePathMaker() {
		this(getCurrentDir());
	}
	
	public AbsoluteToRelativePathMaker(String rootDir) {
		setRootDir(rootDir);
	}
	
	public static String getCurrentDir() {
		String result = ".";
		try {
			result = (new File(".")).getCanonicalPath();
		} catch (Exception e) {
		}
		return result;
	}
	
	public void setRootDir(String rootDir) {
		elements.clear();
		File f;
		try {
			f = (new File(rootDir)).getCanonicalFile();
		} catch (IOException e) {
			f = new File(rootDir);
		}
		this.rootDir = f.getPath(); 
		while (f != null) {
			elements.add(f.getPath());
			f = f.getParentFile();
		}
	}
	
	public String getRootDir() {
		return rootDir;
	}

	public String getRelativePath(String aPath) {
		return getRelativePath(aPath, true);
	}

	public String getRelativePath(File file) {
		return getRelativePath(file, true);
	}	
	
	public String getRelativePathIgnoreCase(String aPath) {
		return getRelativePath(aPath, false);
	}

	public String getRelativePathIgnoreCase(File file) {
		return getRelativePath(file, false);
	}
	
	public String getRelativePath(String aPath, boolean useCaseSensitiveCompare) {
		return getRelativePath(new File(aPath), useCaseSensitiveCompare);
	}
	
	public String getRelativePath(File file, boolean useCaseSensitiveCompare) {
		String fname;
		try {
			fname = file.getCanonicalPath();
		} catch (IOException e) {
			fname = file.getPath();
		}

		File f = new File(fname);
		int elementIndex = 0;
		String trimmed = "";
		String prefix = "";
		while (f != null) {
			if (elementIndex >= elements.size()) {
				elementIndex = 0;
				trimmed = f.getName() + prefix + trimmed;
				prefix = File.separator;
				f = f.getParentFile();
				fname = (f == null) ? "" : f.getPath();
			}
			if (useCaseSensitiveCompare) {
				if (fname.equals(elements.get(elementIndex)))
					break;
			} else {
				if (fname.equalsIgnoreCase(elements.get(elementIndex)))
					break;
			} 
			elementIndex++;
		}
		if (f == null) {
			return file.getPath();
		} else {
			String fn = "";
			for (int i = elementIndex - 1; i >= 0; i--)
				fn = fn + ".." + File.separator;
			return fn + trimmed;
		}
	}
	
	public File getFullPathFile(String aRelativePath) {
		return new File(rootDir + File.separator + aRelativePath);
	}
	
	public String getFullPath(String aRelativePath) {
		return rootDir + File.separator + aRelativePath;
	}
}
