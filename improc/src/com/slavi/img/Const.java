package com.slavi.img;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Const {

	public static final String tempDir;
	
	public static final String workDir;
	
	public static final String sourceImage;

	public static final String sourceImage2;
	
	public static final String smallImage;
	
	public static final String outputImage;
	
	/*
	 * Sample property file
	 * 

<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
<properties>
<comment>zxc</comment>
<entry key="workDir">d:/temp</entry>
<entry key="sourceImage">D:/Users/s/Images/2008 Offroad Chiprovtsi/DSC00601.JPG</entry>
<entry key="sourceImage2">D:/Users/s/Images/2008 Offroad Chiprovtsi/DSC00602.JPG</entry>
<entry key="smallImage">D:/Users/s/kayak/me in the kayak.jpg</entry>
<entry key="outputImage">D:/temp/ttt.bmp</entry>
</properties>

	 */
	static {
		tempDir = System.getProperty("java.io.tmpdir", ".");
		String properyFile = System.getProperty("user.home") + "/java.const.xproperties";
		Properties props = new Properties();
		try {
			props.loadFromXML(new FileInputStream(properyFile));
			workDir = props.getProperty("workDir");
			sourceImage = props.getProperty("sourceImage");
			sourceImage2 = props.getProperty("sourceImage2");
			smallImage = props.getProperty("smallImage");
			outputImage = props.getProperty("outputImage");
		} catch (IOException e) {
			throw new RuntimeException("User consts file not found or incomplete");
		}
	};
	
	public static void main(String[] args) {
		System.out.println("Constants file seems to be correct.");
	}
}
