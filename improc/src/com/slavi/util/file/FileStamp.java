package com.slavi.util.file;

import java.io.File;
import java.util.Date;
import java.util.StringTokenizer;

import org.jdom.Element;

import com.slavi.util.XMLHelper;

public class FileStamp {
	
	private AbsoluteToRelativePathMaker rootDir;
	
	private File file;
	
	private long lastModified;
	
	private long length;
	
	private FileStamp() {}
	
	public FileStamp(String relativeFileName) {
		this(relativeFileName, null);
	}

	public FileStamp(String relativeFileName, AbsoluteToRelativePathMaker rootDir) {
		this.rootDir = rootDir;
		if (rootDir == null)
			file = new File(relativeFileName);
		else
			file = rootDir.getFullPathFile(relativeFileName);
		resetStamp();
	}
	
	public void resetStamp() {
		try {
			this.length = file.length();
			this.lastModified = file.lastModified();
		} catch (Exception e) {
			this.length = -1;
			this.lastModified = 0;
		}
	}
	
	public File getFile() {
		return file;
	}
	
	public long getLastModified() {
		return lastModified;		
	}
	
	public long getLength() {
		return length;
	}
	
	public AbsoluteToRelativePathMaker getRootDir() {
		return rootDir;
	}
	
	public boolean isModified() {
		return !(
			(length >= 0) && 
			(file.isFile()) &&
			(file.length() == length) &&
			(file.lastModified() == lastModified)
		);
	}
	
	public String toString() {
		Date d = new Date(lastModified);
		return (rootDir == null ? file.getPath() : rootDir.getRelativePath(file)) +
			"\t" + length + "\t" + lastModified + "\t(" + d.toString() + ")"; 
	}
	
	public static FileStamp fromString(String str) {
		return fromString(str, null);
	}

	public static FileStamp fromString(String str, AbsoluteToRelativePathMaker rootDir) {
		StringTokenizer st = new StringTokenizer(str, "\t");
		FileStamp result = new FileStamp();
		result.rootDir = rootDir;
		String fileName = st.nextToken();
		result.file = rootDir == null ? (new File(fileName)) : rootDir.getFullPathFile(fileName);
		result.length = Long.parseLong(st.nextToken());
		result.lastModified = Long.parseLong(st.nextToken());
		return result;
	}
	
	public void toXML(Element dest) {
		dest.addContent(XMLHelper.makeAttrEl("File", rootDir == null ? file.getPath() : rootDir.getRelativePath(file)));
		dest.addContent(XMLHelper.makeAttrEl("Size", Long.toString(length)));
		dest.addContent(XMLHelper.makeAttrEl("Date", Long.toString(lastModified)));
		dest.addContent(XMLHelper.makeAttrEl("DateText", (new Date(lastModified)).toString()));
	}
	
	public static FileStamp fromXML(Element source, AbsoluteToRelativePathMaker rootDir) {
		FileStamp result = new FileStamp();
		result.rootDir = rootDir;
		String fileName = XMLHelper.getAttrEl(source, "File", "");
		result.file = rootDir == null ? (new File(fileName)) : rootDir.getFullPathFile(fileName);
		result.length = Long.parseLong(XMLHelper.getAttrEl(source, "Size", "-1"));
		result.lastModified = Long.parseLong(XMLHelper.getAttrEl(source, "Date", ""));
		return result;
	}
}
