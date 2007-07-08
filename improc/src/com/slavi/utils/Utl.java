package com.slavi.utils;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class Utl {

	public static String chageFileExtension(String fileName, String newExtension) {
		int lastIndex = fileName.lastIndexOf(".");
		if (lastIndex < 0)
			return fileName + "." + newExtension;
		return fileName.substring(0, lastIndex) + "." + newExtension; 
	}

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
	
	/**
	 * The code bellow is borrowed from WedSphinx
	 * http://www.cs.cmu.edu/~rcm/websphinx
	 * and slightly modified 
	 * 
	 * Gets a wildcard pattern and returns a Regexp equivalent.  
	 * 
	 * Wildcards are similar to sh-style file globbing.
	 * A wildcard pattern is implicitly anchored, meaning that it must match the entire string.
	 * The wildcard operators are:
	 * <PRE>
	 *    ? matches one arbitrary character
	 *    * matches zero or more arbitrary characters
	 *    [xyz] matches characters x or y or z
	 *    {foo,bar,baz}   matches expressions foo or bar or baz
	 *    ()  grouping to extract fields
	 *    \ escape one of these special characters
	 * </PRE>
	 * Escape codes (like \n and \t) and Perl5 character classes (like \w and \s) may also be used.
	 */
	public static String toRegexpStr(String wildcard) {
		String s = wildcard;

		int inAlternative = 0;
		int inSet = 0;
		boolean inEscape = false;

		StringBuffer output = new StringBuffer();

		int len = s.length();
		for (int i = 0; i < len; ++i) {
			char c = s.charAt(i);
			if (inEscape) {
				output.append(c);
				inEscape = false;
			} else {
				switch (c) {
				case '\\':
					output.append(c);
					inEscape = true;
					break;
				case '?':
					output.append('.');
					break;
				case '*':
					output.append(".*");
					break;
				case '[':
					output.append(c);
					++inSet;
					break;
				case ']':
					// FIX: handle [] case properly
					output.append(c);
					--inSet;
					break;
				case '{':
					output.append("(?:");
					++inAlternative;
					break;
				case ',':
					if (inAlternative > 0)
						output.append("|");
					else
						output.append(c);
					break;
				case '}':
					output.append(")");
					--inAlternative;
					break;
				case '^':
					if (inSet > 0) {
						output.append(c);
					} else {
						output.append('\\');
						output.append(c);
					}
					break;
				case '$':
				case '.':
				case '|':
				case '+':
					output.append('\\');
					output.append(c);
					break;
				default:
					output.append(c);
					break;
				}
			}
		}
		if (inEscape)
			output.append('\\');

		return output.toString();
	}
}
