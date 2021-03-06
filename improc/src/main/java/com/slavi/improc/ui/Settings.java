package com.slavi.improc.ui;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

public class Settings {

	public String imagesRelativePathStr;
	public String imagesRootStr;
	public String keyPointFileRootStr; 
	public String outputDirStr;
	public String adjustMethodClassName;
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
		result.imagesRelativePathStr = properties.getProperty("ImagesRelativePath", "");
		result.imagesRootStr = properties.getProperty("ImagesRoot", userHomeRootStr);
		result.keyPointFileRootStr = properties.getProperty("KeyPointFileRoot", userHomeRootStr);
		result.outputDirStr = properties.getProperty("OutputDir", userHomeRootStr);
		result.adjustMethodClassName = properties.getProperty("AdjustMethodClassName");
		result.pinPoints = getBooleanProperty(properties, "PinPoints");
		result.useColorMasks = getBooleanProperty(properties, "UseColorMasks");
		result.useImageMaxWeight = getBooleanProperty(properties, "UseImageMaxWeight");
		return result;
	}
	
	private void writeProperties(String propertiesFile) {
		Properties properties = new Properties();

		properties.setProperty("ImagesRelativePath", imagesRelativePathStr);
		properties.setProperty("ImagesRoot", imagesRootStr);
		properties.setProperty("KeyPointFileRoot", keyPointFileRootStr);
		properties.setProperty("OutputDir", outputDirStr);
		properties.setProperty("AdjustMethodClassName", adjustMethodClassName);
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
	
	public static Settings getSettings(org.eclipse.swt.widgets.Shell parent) {
		String propertiesFile = getDefaultPropertiesFileName();
		Settings result = readProperties(propertiesFile);
		SettingsDialog settingsDialog = new SettingsDialog(parent);
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
		Shell parent = new Shell((Shell) null, SWT.NONE);
		parent.setBounds(-10, -10, 1, 1);
		parent.open();

		Settings.getSettings(parent);
		System.out.println("Done.");
	}
}
