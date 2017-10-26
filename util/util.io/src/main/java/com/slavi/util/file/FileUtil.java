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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.comparator.CompositeFileComparator;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.comparator.SizeFileComparator;

public class FileUtil {
	public static boolean areFilesIdentical(File file1, File file2, boolean compareFileContent) throws IOException {
		if ((file1 == null) || (file2 == null))
			return false;
		if ((!file1.exists()) || (!file2.exists()))
			return false;

		if ((!file1.isDirectory()) && (!file2.isDirectory())) {
			// Compare real files
			if ((!file1.getName().equals(file2.getName())) || (file1.length() != file2.length()) ||
				(Math.abs(file1.lastModified() - file2.lastModified()) > 2000))
				return false;
			if (compareFileContent) {
				FileInputStream fs1 = null;
				FileInputStream fs2 = null;
				try {
					fs1 = new FileInputStream(file1);
					fs2 = new FileInputStream(file2);
					byte buf1[] = new byte[256];
					byte buf2[] = new byte[256];
					int len1;
					while ((len1 = fs1.read(buf1)) >= 0) {
						int len2;
						while ((len1 > 0) && ((len2 = fs2.read(buf2, 0, len1)) >= 0)) {
							len1 -= len2;
							for (int i = 0; i < len2; i++) {
								if (buf1[i] != buf2[i])
									return false;
							}
						}
					}
				} finally {
					if (fs1 != null)
						fs1.close();
					if (fs2 != null)
						fs2.close();
				}
			}
			return true;
		} else if (file1.isDirectory() && file2.isDirectory()) {
			// Compare folders
			File[] list1 = file1.listFiles();
			File[] list2 = file2.listFiles();
			if ((list1 == null) || (list2 == null) || (list1.length != list2.length))
				return false;
			for (int i = 0; i < list1.length; i++) {
				File f1 = list1[i];
				String f1Name = f1.getName();
				File f2 = null;
				for (int j = 0; j < list2.length; j++) {
					File tmp = list2[j];
					if (f1Name.equals(tmp.getName())) {
						f2 = tmp;
						break;
					}
				}
				if (!areFilesIdentical(f1, f2, compareFileContent)) {
					return false;
				}
			}
			return true;
		} else {
			// One is file the other is folder
			return false;
		}
	}

	public static class RedirectStream implements Runnable {
		InputStream is;
		OutputStream os;
		boolean logErrors;
		boolean closeStreams;
		
		public RedirectStream(InputStream is, OutputStream os, boolean closeStreams, boolean logErrors) {
			this.is = is;
			this.os = os;
			this.logErrors = logErrors;
			this.closeStreams = closeStreams;
		}
		
		public void run() {
			try {
				byte buf[] = new byte[256];
				int len;
				while ((len = is.read(buf)) >= 0) {
					os.write(buf, 0, len);
				}
			} catch (IOException e) {
				if (logErrors)
					e.printStackTrace();
			}
			if (closeStreams) {
				try {
					is.close();
				} catch (IOException e) {
					if (logErrors)
						e.printStackTrace();
				}
				try {
					os.close();
				} catch (IOException e) {
					if (logErrors)
						e.printStackTrace();
				}
			}
		}
	}

	public static void copyStreamAsynch(InputStream is, OutputStream os, boolean closeStreams, boolean logErrors) {
		Thread thread = new Thread(new RedirectStream(is, os, closeStreams, logErrors), "Asynch stream copy");
		//thread.setDaemon(true);
		thread.start();
	}
	
	public static CompositeFileComparator getFileComparator() {
		return new CompositeFileComparator(
				new LastModifiedFileComparator(),
				new SizeFileComparator());
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
		FileUtils.copyFile(fromFile, toFile, true);
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
					IOUtils.copy(zin, fou);
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
	    if (file.isDirectory()) {
			File[] files = file.listFiles();
			if ((files == null) || (files.length == 0)) {
		        // Add a fake entry in order to produce a valid zip file
				ZipEntry entry = new ZipEntry("./");
				zos.putNextEntry(entry);
				zos.closeEntry();
				root = null;
			} else {
				root = file.toURI();
			}
		} else
			root = file.getParentFile().toURI();
		if (root != null)
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
			IOUtils.copy(is, zos);
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
