package com.slavi.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Utility class to process command line parameters.
 */
public class ParseArgs {
	/**
	 * Contains all recognized options (an item starting with a '-' sign) optionally followed by an '='.
	 */
	public Map<String, String> options;
	
	/**
	 * Contains all strings that are not recognized as options. 
	 * The order of the strings is preserved.
	 * See the examples in the class comment.
	 */
	public String remainingArgs[];
	
	private ParseArgs() { }
	
	/**
	 * Searches for an option in the options map. If the specified option is not
	 * present in the options map or its value is null or empty string, the 
	 * default value is returned.
	 * <p>Example:<br>
	 * <code>
	 * Input string array:<br>
	 * "string1", "-option1=someValue", "-option2="<br>
	 * getOption("-option1", "defaultValue1") -> returns "someValue" 
	 * getOption("-option2", "defaultValue2") -> returns "defaultValue2"
	 * </code>
	 * 
	 * @param option
	 * @param defaultValue
	 * @return
	 */
	public String getOption(String option, String defaultValue) {
		String result = options.get(option);
		if ((result == null) || (result.equals("")))
			result = defaultValue;
		return result;
	}
	
	/**
	 * Utility method to check the validity of the options.
	 * Returns true if all keys in the options map are present in the validOptions list.
	 * Returns false otherwise.
	 * <p>Example:<br>
	 * <code>
	 * ParseArgs args = ...
	 * if (!args.isOptionsValid("-id", "-d", "-vp")) {
	 *   <show error message> // The only valid options are -id, -d and/or -vp
	 * }
	 * </code>
	 * 
	 * @param validOptions
	 * @return
	 */
	public boolean isOptionsValid(String...validOptions) {
		Arrays.sort(validOptions, null);
		for (String key : options.keySet()) {
			int indx = Arrays.binarySearch(validOptions, key, null);
			if (indx < 0) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean isStringInList(String value, String...validValues) {
		return indexOf(value, validValues) >= 0;
	}
	
	/**
	 * Returns the index of the value in the valuesList or -1 if the value is not found. 
	 * The comparison is NOT case sensitive.
	 */
	public static int indexOf(String value, String...valuesList) {
		for (int i = 0; i < valuesList.length; i++)
			if (valuesList[i].equalsIgnoreCase(value)) 
				return i;
		return -1;
	}

	/**
	 * Processes command line parameters expecting each option to be preceded with a "-" sign.
	 * <p>
	 * Example 1:<br>
	 * <code>
	 * Input string array:<br>
	 * "string1", "-option1=option1", "-option2", "=", "option2", "string2", "-option3", "=option3", "string3"<br>
	 * Output args map: <br>
	 *     "-option1" -> "option1"<br>
	 *     "-option2" -> "option2"<br>
	 *     "-option3" -> "option3"<br>
	 * Output remainingArgs: "string1", "string2", "string3"
	 * </code>
	 * <p>
	 * Example 2:<br>
	 * <code>
	 * Input string array:<br>
	 * "-option1=", "A", "string1", "string1", "-option1=B", "string1", "-option1" "C"<br>
	 * Output args map: <br>
	 *     "-option1" -> "C"<br>
	 * Output remainingArgs: "string1", "string1", "string1"
	 * </code>
	 */
	public static ParseArgs parse(String[] params) {
		ParseArgs result = new ParseArgs();
		result.options = new HashMap<String, String>();
		if (params == null) {
			result.remainingArgs = new String[0];
			return result;
		}

		String key = null;
		boolean hadEqual = false;
		String param = null;
		Vector<String> remaining = new Vector<String>();
		
		for (int i = 0; i < params.length; i++) {
			param = params[i];

			int indx = param.indexOf('=');
			if (param.startsWith("-")) {
				if (key != null) {
					result.options.put(key, "");
				} 
				if (indx > 0) {
					if (indx == param.length() - 1) {
						key = param.substring(0, indx);
						hadEqual = true;
					} else {
						result.options.put(
								param.substring(0, indx),
								param.substring(indx+1));
						key = null;
					}
				} else {
					key = param;
				}
			} else if (key == null) {
				remaining.add(param);
			} else if ("=".equals(param)) {
				hadEqual = true;
			} else if (hadEqual) {
				result.options.put(key, param);
				hadEqual = false;
				key = null;
			} else if (indx == 0) {
				result.options.put(key, param.substring(1));
				key = null;
			} else {
				result.options.put(key, param);
				key = null;
			}
		}
		if (key != null) {
			result.options.put(key, "");
		}
		result.remainingArgs = remaining.toArray(new String[0]);
		return result;
	}

	/**
	 * Processes command line parameters.
	 * <p>
	 * Example 1:<br>
	 * <code>
	 * Input string array:<br>
	 * "string1", "option1=opt1", "option2", "=", "opt2", "string2", "option3", "=opt3", "string3"<br>
	 * Output args map: <br>
	 *     "option1" -> "opt1"<br>
	 *     "option2" -> "opt2"<br>
	 *     "option3" -> "opt3"<br>
	 * Output remainingArgs: "string1", "string2", "string3"
	 * </code>
	 * <p>
	 * Example 2:<br>
	 * <code>
	 * Input string array:<br>
	 * "option1=", "A", "string1", "string1", "option1=B", "string1", "option1" "=C"<br>
	 * Output args map: <br>
	 *     "option1" -> "C"<br>
	 * Output remainingArgs: "string1", "string1", "string1"
	 * </code>
	 */
	public static ParseArgs parse2(String[] params) {
		ParseArgs result = new ParseArgs();
		result.options = new HashMap<String, String>();
		if (params == null) {
			result.remainingArgs = new String[0];
			return result;
		}

		String key = null;
		boolean hadEqual = false;
		String param = null;
		Vector<String> remaining = new Vector<String>();
		
		for (int i = 0; i < params.length; i++) {
			param = params[i];

			int indx = param.indexOf('=');
			if (key == null) {
				if (indx > 0) {
					result.options.put(
						param.substring(0, indx),
						param.substring(indx+1));
				} else {
					key = param;
				}
			} else if (hadEqual) {
				result.options.put(key, param);
				hadEqual = false;
				key = null;
			} else if (indx == 0) {
				if (param.length() == 1) {
					hadEqual = true;
				} else {
					result.options.put(key, param.substring(1));
					key = null;
				}
			} else {
				remaining.add(key);
				if (indx > 0) {
					result.options.put(
						param.substring(0, indx),
						param.substring(indx+1));
					key = null;
				} else {
					key = param;
				}					
			}
		}
		if (key != null) {
			result.options.put(key, "");
		}
		result.remainingArgs = remaining.toArray(new String[0]);
		return result;
	}
}
