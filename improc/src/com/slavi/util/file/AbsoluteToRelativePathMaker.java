package com.slavi.util.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.slavi.util.Util;

/**
 * This class is used for generating file paths relative to
 * a specified root (base) directory.
 * <p>Example:
 * <p>A project file is located at /work/myproject/project1.prj
 * <p>This project references an external file located at
 * /work/myproject/project1_files/file1.bin
 * <p>The location of the file will be nice to be stored in the
 * project1.prj file as a relative path, so if the project's
 * root folder is changed the referenced files will be 
 * automatically located.
 * <p>The code to do this:
 * <pre>
 * File theProject = new File("/work/myproject/project1.prj");
 * File file1 = new File("/work/myproject/project1_files/file1.bin"); 
 * AbsoluteToRelativePathMaker arpm = 
 *     new AbsoluteToRelativePathMaker(theProject.getParent());
 * String relativePath = arpm.getRelativePath(file1);
 * // The value of relativePath will be "./project1_files/file1.bin"
 * String absolutePath = arpm.getFullPath(relativePath);
 * // The value of absolute Path will be "/work/myproject/project1_files/file1.bin"
 * </pre>
 */
public class AbsoluteToRelativePathMaker {
	private String rootDir;
	
	private ArrayList<String>elements = new ArrayList<String>();
	
	/**
	 * This constructor creates an AbsoluteToRelativePathMaker with
	 * root (base) directory set to the current directory for the
	 * application.
	 */
	public AbsoluteToRelativePathMaker() {
		this(Util.getCurrentDir());
	}
	
	public AbsoluteToRelativePathMaker(String rootDir) {
		setRootDir(rootDir);
	}
	
	/**
	 * Sets the root (base) directory according to which the file
	 * paths are relative. If the directory is not absolute path, i.e.
	 * if a relative path for the root folder is specified then the
	 * relative path is converted to absolute using the current 
	 * directory of the application.  
	 * @param rootDir		the root directory
	 */
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
	
	/**
	 * Gets the root directory that is used as base directory for
	 * the relative file paths. 
	 */
	public String getRootDir() {
		return rootDir;
	}

	/**
	 * Returns a relative path of the "aPath" according
	 * to the specified root directory. The 
	 */
	public String getRelativePath(String aPath) {
		return getRelativePath(aPath, true);
	}

	/**
	 * Returns a relative path of the "aPath" according
	 * to the specified root directory.
	 */
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
		StringBuilder trimmed = new StringBuilder();
		String prefix = "";
		while (f != null) {
			if (elementIndex >= elements.size()) {
				elementIndex = 0;
				trimmed.insert(0, prefix);
				trimmed.insert(0, f.getName());
				//trimmed = f.getName() + prefix + trimmed;
				prefix = "/";
				File parent = f.getParentFile();
				if (parent == null) {
					String str = f.toString();
					if (str.indexOf('\\') != 0) {
						trimmed.insert(0, str, 0, str.lastIndexOf('\\'));
					} else {
						trimmed.insert(0, f);
					}
					return trimmed.toString();
				}
				f = parent;
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
		if (f != null) {
			prefix = "../";
			for (int i = elementIndex - 1; i >= 0; i--)
				trimmed.insert(0, prefix);
		}
		return trimmed.toString();
	}
	
	public File getFullPathFile(String aRelativePath) {
		File f = new File(aRelativePath);
		if (!f.isAbsolute()) {
			f = new File(rootDir + "/" + aRelativePath);
			try {
				f = f.getCanonicalFile();
			} catch (IOException e) {
				// Ignore
			}
		}
		return f;
	}
	
	public String getFullPath(String aRelativePath) {
		return getFullPathFile(aRelativePath).getPath();
	}
}
