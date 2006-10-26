package com.slavi.utils;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class Utl {

	public static String getDirectory() {
		return getDirectory(null);
	}
	
	public static String getDirectory(Component parent) {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setDialogTitle("Select a folder");
		int retval = chooser.showOpenDialog(parent);
		if (retval == JFileChooser.APPROVE_OPTION) {
			File f = chooser.getSelectedFile();
			return f == null ? "" : f.getAbsolutePath();
		}
		return "";
	}
	
	public static String getFileName() {
		return getFileName(null);
	}
	
	public static String getFileName(Component parent) {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setDialogTitle("Select a file");
		int retval = chooser.showOpenDialog(parent);
		if (retval == JFileChooser.APPROVE_OPTION) {
			File f = chooser.getSelectedFile();
			return f == null ? "" : f.getAbsolutePath();
		}
		return "";
	}

	public static Object getUIInput(Object[] values) {
		Object selected = JOptionPane.showInputDialog(null, "Choose one", "Input", JOptionPane.INFORMATION_MESSAGE,
				null, values, values[0]);
		return selected;
	} 
	
	public static void main(String[] args) {
		//System.out.println(getDirectory());
		Object[] values = {"draw mode", "paint mode" };
		System.out.println(getUIInput(values));
	}
}
