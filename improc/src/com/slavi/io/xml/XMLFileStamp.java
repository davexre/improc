package com.slavi.io.xml;

import java.util.Date;

import org.jdom.Element;

import com.slavi.util.file.AbsoluteToRelativePathMaker;
import com.slavi.util.file.FileStamp;
import com.slavi.util.xml.XMLHelper;

public class XMLFileStamp {

	public static final XMLFileStamp instance = new XMLFileStamp();
	
	public void toXML(FileStamp item, Element dest) {
		dest.addContent(XMLHelper.makeAttrEl("File", 
				item.getRootDir() == null ? 
						item.getFile().getPath() : 
						item.getRootDir().getRelativePath(item.getFile())));
		dest.addContent(XMLHelper.makeAttrEl("Size", Long.toString(item.getLength())));
		dest.addContent(XMLHelper.makeAttrEl("Date", Long.toString(item.getLastModified())));
		dest.addContent(XMLHelper.makeAttrEl("DateText", (new Date(item.getLastModified())).toString()));
	}
	
	public static FileStamp fromXML(Element source, AbsoluteToRelativePathMaker rootDir) {
		String fileName = XMLHelper.getAttrEl(source, "File", "");
		FileStamp result = new FileStamp(fileName, rootDir);
		result.setLength(Long.parseLong(XMLHelper.getAttrEl(source, "Size", "-1")));
		result.setLastModified(Long.parseLong(XMLHelper.getAttrEl(source, "Date", "")));
		return result;
	}
}
