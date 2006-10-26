package com.slavi.example;

import java.io.File;

import javax.swing.JFileChooser;

public class BrowseForDirectory {
	public static void main(String[] args) {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int retval = chooser.showOpenDialog(null);
		if (retval != JFileChooser.APPROVE_OPTION) 
			System.out.println("CANCEL");
		else {
			File f = chooser.getSelectedFile();
			if (f != null) {
				if (f.isDirectory())
					System.out.println("Folder selected: " + f.getName());
				else
					System.out.println("File selected: " + f.getName());
			}
		}
	}
}
