package com.slavi.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class ListClassesInPackage {

	private static void listClassFiles(int pathItemAbsolutePathLength, File dir, boolean recurseSubPackages, ArrayList<String>result) {
		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				if (recurseSubPackages)
					listClassFiles(pathItemAbsolutePathLength, file, recurseSubPackages, result);
			} else {
				String aFileName = file.getAbsolutePath();
				if (aFileName.endsWith(".class")) {
					String className = aFileName.substring(pathItemAbsolutePathLength, aFileName.length() - 6);
					className = className.replace(File.separator, ".");
					result.add(className);
				}
			}
		}
	}
	
	private static void listClassFiles(String location, String packagePath, int packagePathLength, boolean recurseSubPackages, ArrayList<String>result) {
		File pathFile = new File(location);
		if (pathFile.isFile()) {
			JarInputStream jarFile = null;
			try {
				jarFile = new JarInputStream(new FileInputStream(pathFile));
				JarEntry jarEntry;
				while ((jarEntry = jarFile.getNextJarEntry()) != null) {
					String aFileName = jarEntry.getName();
					if (!recurseSubPackages && (aFileName.lastIndexOf("/") > packagePathLength))
						continue;
					if (aFileName.startsWith(packagePath) && aFileName.endsWith(".class")) {
						String className = aFileName.substring(0, aFileName.length() - 6);
						className = className.replace("/", ".");
						result.add(className);
					}
				}
			} catch (Exception e) {
			} finally {
				if (jarFile != null)
					try {
						jarFile.close();
					} catch (IOException e) {
					}
			}
		} else {
			File packageDir = new File(location + "/" + packagePath);
			if (packageDir.exists()) { 
				int pathItemAbsolutePathLength = pathFile.getAbsolutePath().length() + File.separator.length();
				listClassFiles(pathItemAbsolutePathLength, packageDir, recurseSubPackages, result);
			}
		}			
	}

	/**
	 * Searches the specified location for classes located in the package packageName and returns a list of fully qualified class names. 
	 * Does NOT enumerate classes in JRE System libraries.
	 * If a jar file can not be opened no exception is thrown but the error is ignored. 
	 * @param packageName 			Package name is specified in the format "org.jdom".
	 * @param recurseSubPackages	If set to true will return all classes in the specified package and its sub-packages.
	 */
	public static List<String> getClassNamesInPackage(String location, String packageName, boolean recurseSubPackages) {
		String packagePath = packageName.replace(".", "/");
		int packagePathLength = packagePath.length() + 1;
		ArrayList<String>result = new ArrayList<String>();
		listClassFiles(location, packagePath, packagePathLength, recurseSubPackages, result);
		return result;
	}
	
	/**
	 * Searches the class path for classes located in the package packageName and returns a list of fully qualified class names. 
	 * Does NOT enumerate classes in JRE System libraries.
	 * If a jar file can not be opened no exception is thrown but the error is ignored. 
	 * @param packageName 			Package name is specified in the format "org.jdom".
	 * @param recurseSubPackages	If set to true will return all classes in the specified package and its sub-packages.
	 */
	public static List<String> getClassNamesInPackage(String packageName, boolean recurseSubPackages) {
		String packagePath = packageName.replace(".", "/");
		int packagePathLength = packagePath.length() + 1;
		ArrayList<String>result = new ArrayList<String>();

		String path = System.getProperty("java.class.path");
		StringTokenizer pathTokens = new StringTokenizer(path, File.pathSeparator);
		while (pathTokens.hasMoreTokens()) {
			String pathItem = pathTokens.nextToken();
			listClassFiles(pathItem, packagePath, packagePathLength, recurseSubPackages, result);
		}
		return result;
	}

	/**
	 * Returns the absolute path to the root folder for the specified class. 
	 */
	public static String getClassRootLocation(Class<?> theClass) {
		String result = theClass.getResource("").getFile();
		if (result.startsWith("file:/")) {
			int index = result.indexOf(".jar!/");
			if (index >= 0)
				result = result.substring(6, index + 4);
			else
				result = result.substring(6);
		} else {
			int count = (new StringTokenizer(theClass.getName(), ".")).countTokens() - 1;
			File f = new File(result);
			for (int i = 0; i < count; i++) {
				File tmp = f.getParentFile();
				if (tmp != null)
					f = tmp;
			}
			result = f.getAbsolutePath();
		}
		return result;
	}
}
