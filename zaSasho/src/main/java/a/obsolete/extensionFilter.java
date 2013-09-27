package a.obsolete;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   extensionFilter.java

import java.io.File;
import java.io.FilenameFilter;

public class extensionFilter implements FilenameFilter {

	extensionFilter(String s) {
		extension = "." + s;
	}

	public boolean accept(File file, String s) {
		if (s.endsWith(extension))
			return true;
		else
			return (new File(file, s)).isDirectory();
	}

	String extension;
}
