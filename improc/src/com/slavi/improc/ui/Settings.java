package com.slavi.improc.ui;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import com.slavi.util.file.AbsoluteToRelativePathMaker;

public class Settings {

	public String imagesRootStr;
	public String keyPointFileRootStr; 
	public String keyPointPairFileRootStr; 

	private Settings() {
	}

	public static Settings getSettings() {
		Properties properties = new Properties();
		String userHomeRootStr = System.getProperty("user.home");
		AbsoluteToRelativePathMaker userHomeRootBase = new AbsoluteToRelativePathMaker(userHomeRootStr);

		String propertiesFile = userHomeRootBase.getFullPath("ImageProcess.xproperties");
		try {
			properties.loadFromXML(new FileInputStream(propertiesFile));
		} catch (Exception e) {
		}

		SettingsDialog settings = new SettingsDialog(null);
		if (!settings.open(properties))
			return null;
		
		Settings result = new Settings();
		result.imagesRootStr = properties.getProperty("ImagesRoot", userHomeRootStr);
		result.keyPointFileRootStr = properties.getProperty("KeyPointFileRoot", userHomeRootStr);
		result.keyPointPairFileRootStr = properties.getProperty("KeyPointPairFileRoot", userHomeRootStr);
		
		try {
			properties.storeToXML(new FileOutputStream(propertiesFile), "Image Process configuration file");
		} catch (Exception e) {
		}
		return result;
	}
	
	public static Settings getConstSettings() {
		Properties properties = new Properties();
		String userHomeRootStr = System.getProperty("user.home");
		AbsoluteToRelativePathMaker userHomeRootBase = new AbsoluteToRelativePathMaker(userHomeRootStr);

		String propertiesFile = userHomeRootBase.getFullPath("ImageProcess.xproperties");
		try {
			properties.loadFromXML(new FileInputStream(propertiesFile));
		} catch (Exception e) {
		}
		
		Settings result = new Settings();
		result.imagesRootStr = properties.getProperty("ImagesRoot", userHomeRootStr);
		result.keyPointFileRootStr = properties.getProperty("KeyPointFileRoot", userHomeRootStr);
		result.keyPointPairFileRootStr = properties.getProperty("KeyPointPairFileRoot", userHomeRootStr);
		
		try {
			properties.storeToXML(new FileOutputStream(propertiesFile), "Image Process configuration file");
		} catch (Exception e) {
		}
		return result;
	}
}
