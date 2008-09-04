package com.slavi.util.ui;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * This class contains utility functions for creating user interface using the
 * Standard SWING library
 */
public class SwingUtl {
	/**
	 * Opens the standard SWING directory chooser dialog.
	 * @see #getDirectory(Component)
	 */
	public static String getDirectory() {
		return getDirectory(null);
	}
	
	/**
	 * Opens the standard SWING directory chooser dialog.
	 * <p>
	 * Returns the selected directory or if
	 * canceled returns an EMPTY string "" not a null.
	 */
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
	
	/**
	 * Opens the standard SWING file chooser dialog.
	 * @see #getFileName(Component)
	 */
	public static String getFileName() {
		return getFileName(null);
	}
	
	/**
	 * Opens the standard SWING file chooser dialog.
	 * <p>
	 * Returns the selected file or if
	 * canceled returns an EMPTY string "" not a null. The
	 * file MAY be a new one and MIGHT NOT exist.
	 */
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

	/**
	 * Opens the standard SWING JOptionPane dialog.
	 * <p>
	 * The default selected options is the first object in the list.
	 * Returns the selected object or null is the dialog is canceled.
	 */
	public static Object getUIInput(Object ... values) {
		Object selected = JOptionPane.showInputDialog(null, "Choose one", "Input", JOptionPane.INFORMATION_MESSAGE,
				null, values, values[0]);
		return selected;
	}
}
