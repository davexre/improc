package com.slavi.utils;

import java.io.File;
import java.util.Iterator;
import java.util.Stack;
import java.util.regex.Pattern;

public class FindFileIterator implements Iterator<File> {
	private String patternStr;
	
	private String startDir;
	
	public boolean recurseDirs;
	
	public boolean filesOnly;
	
	private static class FileBookmark {
		public File files[];
		
		public int itemsCount = 0;
		
		public int atIndex = 0;
		
		public int atDirIndex = 0;
	}
	
	private FileBookmark cur;
	
	private Stack<FileBookmark>dirstack = new Stack<FileBookmark>();
	
	private Pattern pattern;
	
	private File nextFile;
	
	private FindFileIterator() { }
	
	public static FindFileIterator makeWithRegexp(String filePattern, boolean recurseDirs, boolean filesOnly) {
		FindFileIterator fi = new FindFileIterator();
		File f = new File(filePattern);
		fi.recurseDirs = recurseDirs;
		fi.filesOnly = filesOnly;
		fi.startDir = f.getParent();
		if ((fi.startDir == null) || (fi.equals("")))
			fi.startDir = "";
		fi.patternStr = f.getName();
		fi.reset();
		return fi;
	}
	
	public static FindFileIterator makeWithWildcard(String filePattern, boolean recurseDirs, boolean filesOnly) {
		FindFileIterator fi = new FindFileIterator();
		File f = new File(filePattern);
		fi.recurseDirs = recurseDirs;
		fi.filesOnly = filesOnly;
		fi.startDir = f.getParent();
		if ((fi.startDir == null) || (fi.equals("")))
			fi.startDir = ".";
		fi.patternStr = Utl.toRegexpStr(f.getName());
		fi.reset();
		return fi;
	}
	
	public void reset() {
		dirstack.empty();
		File f = new File(startDir);
		pattern = Pattern.compile(patternStr);
		cur = new FileBookmark();
		cur.files = f.listFiles();
		cur.itemsCount = cur.files == null ? 0 : cur.files.length;
		nextFile = null;
	}
	
	private File getNext() {
		while (cur != null) {
			File candidate = null;
			if (cur.atIndex < cur.itemsCount) {
				candidate = cur.files[cur.atIndex++];
			} else if (recurseDirs && (cur.atDirIndex < cur.itemsCount)) {
				File aDir = cur.files[cur.atDirIndex++];
				if (aDir.isDirectory()) {
					FileBookmark tmp = new FileBookmark();
					tmp.files = aDir.listFiles();
					if (tmp.files != null) {
						tmp.itemsCount = tmp.files.length;
						dirstack.push(cur);
						cur = tmp;
					}
				}
			} else {
				if ((dirstack == null) || (dirstack.isEmpty()))
					cur = null;
				else
					cur = dirstack.pop();
			}
			
			if (candidate != null) {
				if (!(filesOnly && candidate.isDirectory())) {
					if (pattern.matcher(candidate.getName()).matches())
						return candidate;
				}
			}
		}
		return null;
	}
	
	public boolean hasNext() {
		if (nextFile == null) {
			nextFile = getNext();
		}
		return nextFile != null;
	}
	
	public File next() {
		File result = nextFile;
		nextFile = null;
		if (result == null) {
			result = getNext();
		}
		return result;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}
}
