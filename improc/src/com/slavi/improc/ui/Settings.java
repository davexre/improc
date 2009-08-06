package com.slavi.improc.ui;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class Settings {

	public String imagesRootStr;
	public String keyPointFileRootStr; 
	public String outputDirStr;
	public boolean pinPoints;
	public boolean useColorMasks;
	public boolean useImageMaxWeight;

	private Settings() {
	}

	private static boolean getBooleanProperty(Properties properties, String propertyName) {
		String val = properties.getProperty(propertyName, "false");
		return !"false".equalsIgnoreCase(val);
	}
	
	private static String getDefaultPropertiesFileName() {
		return System.getProperty("user.home") + "/ImageProcess.xproperties";
	}
	
	private static Settings readProperties(String propertiesFile) {
		Properties properties = new Properties();
		FileInputStream fin = null; 
		try {
			fin = new FileInputStream(propertiesFile);
			properties.loadFromXML(fin);
		} catch (Exception e) {
		}
		try {
			if (fin != null)
				fin.close();
		} catch (Exception e) {
		}

		String userHomeRootStr = System.getProperty("user.home");
		Settings result = new Settings();
		result.imagesRootStr = properties.getProperty("ImagesRoot", userHomeRootStr);
		result.keyPointFileRootStr = properties.getProperty("KeyPointFileRoot", userHomeRootStr);
		result.outputDirStr = properties.getProperty("OutputDir", userHomeRootStr);
		result.pinPoints = getBooleanProperty(properties, "PinPoints");
		result.useColorMasks = getBooleanProperty(properties, "UseColorMasks");
		result.useImageMaxWeight = getBooleanProperty(properties, "UseImageMaxWeight");
		return result;
	}
	
	private void writeProperties(String propertiesFile) {
		Properties properties = new Properties();

		properties.setProperty("ImagesRoot", imagesRootStr);
		properties.setProperty("KeyPointFileRoot", keyPointFileRootStr);
		properties.setProperty("OutputDir", outputDirStr);
		properties.setProperty("PinPoints", pinPoints ? "true" : "false");
		properties.setProperty("UseColorMasks", useColorMasks ? "true" : "false");
		properties.setProperty("UseImageMaxWeight", useImageMaxWeight ? "true" : "false");

		FileOutputStream fou = null;
		try {
			fou = new FileOutputStream(propertiesFile);
			properties.storeToXML(fou, "Image Process configuration file");
		} catch (Exception e) {
		}
		try {
			if (fou != null)
				fou.close();
		} catch (Exception e) {
		}
	}
	
	public static Settings getSettings() {
		String propertiesFile = getDefaultPropertiesFileName();
		Settings result = readProperties(propertiesFile);
		SettingsDialog settingsDialog = new SettingsDialog(null);
		if (!settingsDialog.open(result))
			return null;
		result.writeProperties(propertiesFile);
		return result;
	}
	
	public static Settings getConstSettings() {
		String propertiesFile = getDefaultPropertiesFileName();
		Settings result = readProperties(propertiesFile);
		return result;
	}

	public static void main(String[] args) {
		Settings.getSettings();
		System.out.println("Done.");
	}
}
