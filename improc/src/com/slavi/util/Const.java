package com.slavi.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Const {

	public static class PropertiesWithModCounter extends Properties {
		private static final long serialVersionUID = 3260018309030864911L;
		private int modCount = 0;
		
		public synchronized Object put(Object key, Object value) {
			modCount++;
			return super.put(key, value);
		}
		
		public synchronized Object remove(Object key) {
			modCount++;
			return super.remove(key);			
		}
		
		public synchronized void clear() {
			modCount++;
			super.clear();			
		}
	}
	
	public static final String tempDir;
	
	public static final String workDir;
	
	public static final String sourceImage;

	public static final String sourceImage2;
	
	public static final String smallImage;
	
	public static final String outputImage;
	
	public static final String imagesDir;
	
	public static final String properyFileName;
	
	public static final Properties properties;
	
	private static final int propertiesModCount;
	
	
	/*
	 * Sample property file
	 * 

<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
<properties>
<comment>zxc</comment>
<entry key="workDir">d:\temp</entry>
<entry key="sourceImage">D:\Users\s\Images\2008 Offroad Chiprovtsi\DSC00601.JPG</entry>
<entry key="sourceImage2">D:\Users\s\Images\2008 Offroad Chiprovtsi\DSC00602.JPG</entry>
<entry key="smallImage">D:\Users\s\kayak\me in the kayak.jpg</entry>
<entry key="outputImage">D:\temp\ttt.bmp</entry>
<entry key="imagesDir">D:\Users\s\Java\Images\Panoramas</entry>
</properties>

	 */
	static {
		tempDir = System.getProperty("java.io.tmpdir", ".");
		properyFileName = System.getProperty("user.home") + "/java.const.xproperties";
		final PropertiesWithModCounter prop = new PropertiesWithModCounter();
		properties = prop;
		try {
			properties.loadFromXML(new FileInputStream(properyFileName));
			workDir = properties.getProperty("workDir");
			sourceImage = properties.getProperty("sourceImage");
			sourceImage2 = properties.getProperty("sourceImage2");
			smallImage = properties.getProperty("smallImage");
			outputImage = properties.getProperty("outputImage");
			imagesDir = properties.getProperty("imagesDir");
		} catch (IOException e) {
			throw new RuntimeException("User consts file not found or incomplete");
		}
		propertiesModCount = prop.modCount;
		
		Runtime.getRuntime().addShutdownHook(
			new Thread(Const.class.getName() + " writer shutdown hook") {
				public void run() {
					if (prop.modCount == propertiesModCount)
						return;
					FileOutputStream fou = null;
					try {
						fou = new FileOutputStream(properyFileName);
						properties.storeToXML(fou, Const.class.getName() + " properties");
					} catch (Throwable t) {
						t.printStackTrace();
					} finally {
						try {
							if (fou != null)
								fou.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			});
	}
	
	public static void main(String[] args) {
		System.out.println("OK");
	}
}
