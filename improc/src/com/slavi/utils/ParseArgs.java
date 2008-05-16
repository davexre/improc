package com.slavi.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class ParseArgs {
	public Map<String, String> args;
	
	public String remainingArgs[];
	
	private ParseArgs() { }
	
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
		result.args = new HashMap<String, String>();
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
					result.args.put(key, "");
				} 
				if (indx > 0) {
					if (indx == param.length() - 1) {
						key = param.substring(0, indx);
						hadEqual = true;
					} else {
						result.args.put(
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
				result.args.put(key, param);
				hadEqual = false;
				key = null;
			} else if (indx == 0) {
				result.args.put(key, param.substring(1));
				key = null;
			} else {
				result.args.put(key, param);
				key = null;
			}
		}
		if (key != null) {
			remaining.add(key);
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
		result.args = new HashMap<String, String>();
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
					result.args.put(
						param.substring(0, indx),
						param.substring(indx+1));
				} else {
					key = param;
				}
			} else if (hadEqual) {
				result.args.put(key, param);
				hadEqual = false;
				key = null;
			} else if (indx == 0) {
				if (param.length() == 1) {
					hadEqual = true;
				} else {
					result.args.put(key, param.substring(1));
					key = null;
				}
			} else {
				remaining.add(key);
				if (indx > 0) {
					result.args.put(
						param.substring(0, indx),
						param.substring(indx+1));
					key = null;
				} else {
					key = param;
				}					
			}
		}
		if (key != null) {
			remaining.add(key);
		}
		result.remainingArgs = remaining.toArray(new String[0]);
		return result;
	}
}
