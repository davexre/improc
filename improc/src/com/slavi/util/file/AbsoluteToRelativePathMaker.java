package com.slavi.util.file;

import java.io.File;
import java.net.URI;

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
	String rootDir;
	URI root;
	
	/**
	 * This constructor creates an AbsoluteToRelativePathMaker with
	 * root (base) directory set to the current directory for the
	 * application.
	 */
	public AbsoluteToRelativePathMaker() {
		this(FileUtil.getCurrentDir());
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
		this.rootDir = rootDir;
		if (!rootDir.endsWith("/."))
			rootDir += "/.";
		File rootFile = new File(rootDir);
		root = rootFile.toURI();
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
		return getRelativePath(new File(aPath));
	}

	/**
	 * Returns a relative path of the "aPath" according
	 * to the specified root directory.
	 */
	public String getRelativePath(File file) {
		return root.relativize(file.toURI()).getPath();
	}
	
	public File getFullPathFile(String aRelativePath) {
		return new File(root.resolve(aRelativePath));
	}
	
	public String getFullPath(String aRelativePath) {
		return root.resolve(aRelativePath).getPath();
	}
}
