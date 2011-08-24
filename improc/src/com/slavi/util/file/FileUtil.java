package com.slavi.util.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FileUtil {
	/**
	 * Remove a directory and all of its contents.
	 * 
	 * The results of executing File.delete() on a File object that represents a
	 * directory seems to be platform dependent. This method removes the
	 * directory and all of its contents.
	 * 
	 * Code borrowed from:
	 * http://www.java2s.com/Tutorial/Java/0180__File/Removeadirectoryandallofitscontents.htm
	 * 
	 * @return true if the complete directory was removed, false if it could not
	 *         be. If false is returned then some of the files in the directory
	 *         may have been removed.
	 */
	public static boolean removeDirectory(File directory) {
		if (directory == null)
			return false;
		if (!directory.exists())
			return true;
		if (!directory.isDirectory()) {
			return directory.delete(); // This is a file. Delete it.
		}

		File[] list = directory.listFiles();
		// On error continue removing files - remove as many as possible files
		boolean result = true; 
		// Some JVMs return null for File.list() when the
		// directory is empty.
		if (list != null) {
			for (int i = 0; i < list.length; i++) {
				File entry = list[i];
				if (entry.isDirectory()) {
					if (!removeDirectory(entry)) {
						result = false;
					}
				} else if (!entry.delete()) {
					result = false;
				}
			}
		}

		result &= directory.delete();
		return result;
	}
	
	public static void copyStream(InputStream is, OutputStream os) throws IOException {
		byte buf[] = new byte[256];
		int len;
		while ((len = is.read(buf)) >= 0) {
			os.write(buf, 0, len);
		}
	}

	public static void copyFile(File fromFile, File toFile) throws IOException {
		copyFile(fromFile, toFile, true);
	}
	
	public static void copyFile(File fromFile, File toFile, boolean overrideIfFileExists) throws IOException {
		if (toFile.isDirectory())
			toFile = new File(toFile, fromFile.getName());

		if (toFile.exists() && (!overrideIfFileExists))
			return;
		File parent = toFile.getParentFile();
		if ((parent != null) && (!parent.exists())) {
			 parent.mkdirs();
		}

		FileInputStream from = null;
		FileOutputStream to = null;
		try {
			from = new FileInputStream(fromFile);
			to = new FileOutputStream(toFile);
			copyStream(from, to);
		} finally {
			try {
				if (from != null)
					from.close();
			} finally {
				if (to != null)
					to.close();
			}
		}
		toFile.setLastModified(fromFile.lastModified());
	}

	public static void copyFileIfDifferent(File fromFile, File toFile) throws IOException {
		if (toFile.isDirectory())
			toFile = new File(toFile, fromFile.getName());
		if (fromFile.exists() && 
			toFile.exists() && 
			fromFile.lastModified() == toFile.lastModified() && 
			fromFile.length() == toFile.length()) {
			return;
		}
		copyFile(fromFile, toFile, true);
	}


	/**
	 * Returns the absolute path of the current directory.
	 */
	public static String getCurrentDir() {
		String result = ".";
		try {
			result = (new File(".")).getCanonicalPath();
		} catch (Exception e) {
		}
		return result;
	}
	
	/**
	 * Replaces the extension of fileName with the newExtension.
	 * <p>
	 * Example:
	 * <p>
	 * <table border=1>
	 * <tr><th>fileName</th><th>newExtension</th><th>result</th></tr>
	 * <tr><td>c:\temp\somefile.log</td><td>.txt</td><td>c:\temp\somefile.txt</td></tr>
	 * <tr><td>c:\temp\somefile.log</td><td>&nbsp;</td><td>c:\temp\somefile</td></tr>
	 * <tr><td>c:\temp\somefile</td><td>.txt</td><td>c:\temp\somefile.txt</td></tr>
	 * <tr><td>c:\temp.tmp\somefile.log</td><td>.txt</td><td>c:\temp.tmp\somefile.txt</td></tr>
	 * <tr><td><b>c:\temp.tmp\somefile</b></td><td><b>.txt</b></td><td><b>c:\temp.tmp\somefile.txt</b></td></tr>
	 * </table>
	 */
	public static String changeFileExtension(String fileName, String newExtension) {
		int lastPath = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
		int lastIndex = fileName.lastIndexOf('.');
		if ((lastPath > lastIndex) || (lastIndex < 0))
			return fileName + newExtension;
		return fileName.substring(0, lastIndex) + newExtension; 
	}
	
	public static void unzipToFolder(String zip, String destinationFolder) throws IOException {
		FileInputStream fin = new FileInputStream(zip);
		try {
			unzipToFolder(fin, new File(destinationFolder));
		} finally {
			fin.close();
		}
	}

	public static void unzipToFolder(InputStream is, File destinationFolder) throws IOException {
		ZipInputStream zin = new ZipInputStream(is);
		ZipEntry entry = null;
		while ((entry = zin.getNextEntry()) != null) {
			File f = new File(destinationFolder, entry.getName());
			File dir = entry.isDirectory() ? f : f.getParentFile();
			if (!dir.mkdirs())
				throw new IOException("Could not create directory " + dir.getPath());

			if (!entry.isDirectory()) {
				FileOutputStream fou = new FileOutputStream(f);
				try {
					copyStream(zin, fou);
				} finally {
					fou.close();
				}
			}
		}
	}

	public static void zipTo(OutputStream os, File file) throws IOException {
		file = file.getCanonicalFile();
		ZipOutputStream zos = new ZipOutputStream(os);
		URI root;
		if (file.isDirectory())
			root = file.toURI();
		else
			root = file.getParentFile().toURI();
		recursiveZipFile(zos, root, file);
		zos.finish();
	}

	private static void recursiveZipFile(ZipOutputStream zos, URI root, File file) throws IOException {
		URI fileURI = file.toURI();
		URI rel = root.relativize(fileURI);
		String relativeFileName = rel.getPath();

		if (file.isDirectory()) {
			if (!"".equals(relativeFileName)) {
				// An empty relativeFileName means archive root
				ZipEntry entry = new ZipEntry(relativeFileName);
				zos.putNextEntry(entry);
				zos.closeEntry();
			}
			for (File f : file.listFiles()) {
				recursiveZipFile(zos, root, f);
			}
			return;
		}

		InputStream is = null;
		try {
			ZipEntry entry = new ZipEntry(relativeFileName);
			zos.putNextEntry(entry);
			is = new FileInputStream(file);
			copyStream(is, zos);
			zos.closeEntry();
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}
		  
	/**
	 * Replaces a set of files in a zip file in one zip copy operation.
	 * If zipfin = null then a new zip file is generated in zipfou.
	 */
	public static void replaceFilesInZip(InputStream zipfin, OutputStream zipfou, Map<String, InputStream> filesToReplace) throws IOException {
		byte buf[] = new byte[1024];
		ZipInputStream zin = null;
		ZipOutputStream zou = new ZipOutputStream(zipfou);
		try {
			if (zipfin != null) {
				zin = new ZipInputStream(zipfin);
				ZipEntry entryIn = null;
				while ((entryIn = zin.getNextEntry()) != null) {
					if (!filesToReplace.containsKey(entryIn.getName())) {
						ZipEntry entryOut = (ZipEntry) entryIn.clone();
						entryOut.setCompressedSize(-1);
						zou.putNextEntry(entryOut);
						while (zin.available() > 0) {
							int len = zin.read(buf);
							if (len > 0)
								zou.write(buf, 0, len);
						}
						zou.closeEntry();
					}
				}
				zin.close();
				zin = null;
			}
	        for (Map.Entry<String, InputStream> item : filesToReplace.entrySet()) {
	        	InputStream itemfin = item.getValue();
	        	if (itemfin == null)
	        		continue;
				ZipEntry entryOut = new ZipEntry(item.getKey());
				entryOut.setCompressedSize(-1);
				zou.putNextEntry(entryOut);
				itemfin.reset();
				while (itemfin.available() > 0) {
					int len = itemfin.read(buf);
					if (len > 0)
						zou.write(buf, 0, len);
				}
				zou.closeEntry();
			}
		} finally {
			if (zin != null)
				zin.close();
			zou.close();
		}
	}
}
