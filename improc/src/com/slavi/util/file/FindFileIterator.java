package com.slavi.util.file;

import java.io.File;
import java.util.Iterator;
import java.util.Stack;
import java.util.regex.Pattern;

import com.slavi.util.Utl;

/**
 * This class implements a file iterator based on a wildcard search
 * or search with regular expressions. The search may be recurse in
 * subfolders. The files returned might include folder names or not.
 * <p>Usage:
 * <pre>
 * FindFileIterator iter = FindFileIterator.makeWithWildcard("/temp/*.tmp", true, true);
 * while (iter.hasNext()) {
 *   File f = iter.getNext();
 *   ...
 * }
 * iter.reset();
 * while (iter.hasNext()) {
 *   File f = iter.getNext();
 *   ...
 * }
 * </pre>
 */
public class FindFileIterator implements Iterator<File> {

	private static class FileBookmark {
		public File files[];
		
		public int itemsCount = 0;
		
		public int atIndex = 0;
		
		public int atDirIndex = 0;
	}
	
	private String patternStr;
	
	private String startDir;
	
	private boolean recurseDirs;
	
	private boolean filesOnly;
	
	private FileBookmark cur;
	
	private Stack<FileBookmark>dirstack = new Stack<FileBookmark>();
	
	private Pattern pattern;
	
	private File nextFile;
	
	/**
	 * If true the iterator will recurse in subdirectories.
	 */
	public boolean getRecurseDirs() {
		return recurseDirs;
	}
	
	/**
	 * If true the iterator will return only files, false will
	 * return directories also.
	 */
	public boolean getFilesOnly() {
		return filesOnly;
	}

	private FindFileIterator() { }
	
	/**
	 * Returns a FindFileIterator created with a search pattern, specified 
	 * as a regular expression.
	 * @param filePattern	the regexp search pattern.
	 * @param recurseDirs	true will recurse in subdirectories.
	 * @param filesOnly		true will return only files, false will 
	 * 						return directories also. 
	 */
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
	
	/**
	 * Returns a FindFileIterator created with a search pattern, specified 
	 * with wildcard like "*.txt", "a?b*.*" or "*.*" etc.
	 * @param filePattern	the regexp search pattern
	 * @param recurseDirs	true will recurse in subdirectories
	 * @param filesOnly		true will return only files, false will 
	 * 						return directories also 
	 */
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
	
	/**
	 * Resets the iterator, i.e. the next call to getNext() will return the first file.
	 * <p><b>Warning: </b>Iterating through all files the iterator returns, 
	 * invoking reset() and then iterating through all the files again <b>MAY NOT</b> 
	 * produce the same result. Meanwile if there are added or deleted files they <b>WILL</b>
	 * affect the iterator.
	 */
	public void reset() {
		dirstack.empty();
		File f = new File(startDir);
		pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
		cur = new FileBookmark();
		cur.files = f.listFiles();
		cur.itemsCount = cur.files == null ? 0 : cur.files.length;
		nextFile = null;
	}
	
	/**
	 * Returns the next File matching the search criteria and null if there is no match.
	 */
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

	/**
	 * This mothod is not implemented and throws an UnsupportedOperationException. 
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
